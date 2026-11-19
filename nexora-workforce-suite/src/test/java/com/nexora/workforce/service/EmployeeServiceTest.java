package com.nexora.workforce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nexora.workforce.dto.EmployeePageResponseDTO;
import com.nexora.workforce.dto.EmployeeRequestDTO;
import com.nexora.workforce.dto.EmployeeResponseDTO;
import com.nexora.workforce.dto.EmployeeTransferRequestDTO;
import com.nexora.workforce.entity.Department;
import com.nexora.workforce.entity.Employee;
import com.nexora.workforce.entity.EmployeeDepartmentHistory;
import com.nexora.workforce.exception.DuplicateResourceException;
import com.nexora.workforce.repository.DepartmentRepository;
import com.nexora.workforce.repository.EmployeeDepartmentHistoryRepository;
import com.nexora.workforce.repository.EmployeeRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeDepartmentHistoryRepository historyRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void createsEmployeeWithoutDepartmentWhenLegacyDepartmentIsBlank() {
        EmployeeRequestDTO request = new EmployeeRequestDTO();
        Department blankDepartment = new Department();
        blankDepartment.setName("");
        request.setDepartment(blankDepartment);
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeResponseDTO savedEmployee = employeeService.createEmployee(request);

        assertNull(savedEmployee.departmentId());
        assertNull(savedEmployee.departmentName());
        verify(employeeRepository).save(any(Employee.class));
        verifyNoInteractions(departmentRepository);
    }

    @Test
    void createsEmployeeWithDepartmentAndReturnsDepartmentSummary() {
        EmployeeRequestDTO request = new EmployeeRequestDTO();
        request.setFirstName("Ada");
        request.setLastName("Lovelace");
        request.setEmail("ada@example.com");
        Department departmentReference = new Department();
        departmentReference.setId(7L);
        request.setDepartment(departmentReference);

        Department department = new Department();
        department.setId(7L);
        department.setName("Engineering");
        when(departmentRepository.findById(7L)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> {
                    Employee employee = invocation.getArgument(0);
                    employee.setId(12L);
                    return employee;
                });

        EmployeeResponseDTO response = employeeService.createEmployee(request);

        assertEquals(12L, response.id());
        assertEquals("Ada", response.firstName());
        assertEquals(7L, response.departmentId());
        assertEquals("Engineering", response.departmentName());
        ArgumentCaptor<EmployeeDepartmentHistory> historyCaptor =
                ArgumentCaptor.forClass(EmployeeDepartmentHistory.class);
        verify(historyRepository).save(historyCaptor.capture());
        assertNull(historyCaptor.getValue().getFromDepartment());
        assertEquals(7L, historyCaptor.getValue().getToDepartment().getId());
    }

    @Test
    void rejectsAnEmployeeIdThatAlreadyExists() {
        EmployeeRequestDTO request = new EmployeeRequestDTO();
        request.setId(7L);
        when(employeeRepository.existsById(7L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> employeeService.createEmployee(request));

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deletesDepartmentHistoryBeforeDeletingEmployee() {
        Employee employee = new Employee();
        employee.setId(12L);
        when(employeeRepository.findById(12L)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(12L);

        InOrder deletionOrder = inOrder(historyRepository, employeeRepository);
        deletionOrder.verify(historyRepository).deleteByEmployee_Id(12L);
        deletionOrder.verify(employeeRepository).delete(employee);
    }

    @Test
    void rejectsAnEmailAlreadyUsedByAnotherEmployee() {
        EmployeeRequestDTO request = new EmployeeRequestDTO();
        request.setEmail("ada@example.com");
        when(employeeRepository.existsByEmailIgnoreCase("ada@example.com"))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> employeeService.createEmployee(request));

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void rejectsAnUpdateWithAnIdThatDoesNotMatchThePath() {
        EmployeeRequestDTO request = new EmployeeRequestDTO();
        request.setId(9L);
        when(employeeRepository.findById(7L)).thenReturn(Optional.of(new Employee()));
        when(employeeRepository.existsById(9L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> employeeService.updateEmployee(7L, request));

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void transfersEmployeeAndRecordsEffectiveDepartmentHistory() {
        Department oldDepartment = new Department();
        oldDepartment.setId(3L);
        oldDepartment.setName("Operations");
        Department newDepartment = new Department();
        newDepartment.setId(7L);
        newDepartment.setName("Engineering");

        Employee employee = new Employee();
        employee.setId(12L);
        employee.setDepartment(oldDepartment);
        employee.setHireDate(LocalDate.of(2020, 1, 1));

        EmployeeTransferRequestDTO request =
                new EmployeeTransferRequestDTO(7L, LocalDate.now().minusDays(1));

        when(employeeRepository.findById(12L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(7L)).thenReturn(Optional.of(newDepartment));
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.findTopByEmployee_IdOrderByEffectiveDateDescIdDesc(12L))
                .thenReturn(Optional.empty());

        EmployeeResponseDTO response = employeeService.transferEmployee(12L, request);

        ArgumentCaptor<EmployeeDepartmentHistory> historyCaptor =
                ArgumentCaptor.forClass(EmployeeDepartmentHistory.class);
        verify(historyRepository).save(historyCaptor.capture());
        EmployeeDepartmentHistory history = historyCaptor.getValue();
        assertEquals(3L, history.getFromDepartment().getId());
        assertEquals(7L, history.getToDepartment().getId());
        assertEquals(request.effectiveDate(), history.getEffectiveDate());
        assertEquals(7L, response.departmentId());
    }

    @Test
    void rejectsFutureEffectiveTransferDates() {
        EmployeeTransferRequestDTO request =
                new EmployeeTransferRequestDTO(7L, LocalDate.now().plusDays(1));

        assertThrows(IllegalArgumentException.class,
                () -> employeeService.transferEmployee(12L, request));

        verifyNoInteractions(employeeRepository, departmentRepository, historyRepository);
    }

    @Test
    void transferringToCurrentDepartmentDoesNotCreateDuplicateHistory() {
        Department department = new Department();
        department.setId(7L);
        Employee employee = new Employee();
        employee.setId(12L);
        employee.setDepartment(department);

        EmployeeTransferRequestDTO request =
                new EmployeeTransferRequestDTO(7L, LocalDate.now());

        when(employeeRepository.findById(12L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(eq(7L))).thenReturn(Optional.of(department));

        EmployeeResponseDTO response = employeeService.transferEmployee(12L, request);

        verify(historyRepository, never()).save(any(EmployeeDepartmentHistory.class));
        verify(employeeRepository, never()).save(any(Employee.class));
        assertEquals(7L, response.departmentId());
    }

    @Test
    void searchesEmployeesWithNormalizedNameAndPageMetadata() {
        Department department = new Department();
        department.setId(7L);
        department.setName("Engineering");
        Employee employee = new Employee();
        employee.setId(15L);
        employee.setFirstName("Ada");
        employee.setDepartment(department);
        Pageable pageable = PageRequest.of(
                1, 1, Sort.by(Sort.Direction.ASC, "id"));
        Page<Employee> employees = new PageImpl<>(
                List.of(employee), pageable, 3);
        when(employeeRepository.searchEmployees(
                eq("Ada"), eq(7L), any(Pageable.class)))
                .thenReturn(employees);

        EmployeePageResponseDTO response =
                employeeService.searchEmployees(" Ada ", 7L, 1, 1);

        assertEquals(1, response.page());
        assertEquals(1, response.size());
        assertEquals(3L, response.totalElements());
        assertEquals(3, response.totalPages());
        assertEquals("Engineering", response.content().get(0).departmentName());
        verify(employeeRepository).searchEmployees(
                eq("Ada"), eq(7L), any(Pageable.class));
    }

    @Test
    void rejectsInvalidSearchPaginationBeforeQueryingRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.searchEmployees(null, null, 0, 101));
        verifyNoInteractions(employeeRepository);
    }
}
