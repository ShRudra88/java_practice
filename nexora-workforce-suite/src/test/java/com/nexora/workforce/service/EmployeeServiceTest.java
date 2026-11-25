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

import com.nexora.workforce.dto.EmployeeDepartmentHistoryResponseDTO;
import com.nexora.workforce.dto.EmployeePageResponseDTO;
import com.nexora.workforce.dto.EmployeeRequestDTO;
import com.nexora.workforce.dto.EmployeeResponseDTO;
import com.nexora.workforce.dto.EmployeeTransferRequestDTO;
import com.nexora.workforce.entity.Department;
import com.nexora.workforce.entity.Employee;
import com.nexora.workforce.entity.EmployeeDepartmentHistory;
import com.nexora.workforce.exception.DuplicateResourceException;
import com.nexora.workforce.exception.ResourceNotFoundException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Mock
    private PasswordEncoder passwordEncoder;

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
    void storesEmployeePasswordsAsEncodedHashes() {
        EmployeeRequestDTO request = new EmployeeRequestDTO();
        request.setEmail("ada@example.com");
        request.setPassword("employee-password");
        when(passwordEncoder.encode("employee-password"))
                .thenReturn("{bcrypt}encoded-password");
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        employeeService.createEmployee(request);

        ArgumentCaptor<Employee> employeeCaptor =
                ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(employeeCaptor.capture());
        assertEquals("{bcrypt}encoded-password",
                employeeCaptor.getValue().getPasswordHash());
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
    void rejectsAnEmployeeIdThatWasProvidedButDoesNotExist() {
        EmployeeRequestDTO request = new EmployeeRequestDTO();
        request.setId(7L);
        when(employeeRepository.existsById(7L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> employeeService.createEmployee(request));

        verify(employeeRepository, never()).save(any(Employee.class));
        verifyNoInteractions(departmentRepository, historyRepository, passwordEncoder);
    }

    @Test
    void looksUpEmployeeByIdAndMapsItsResponse() {
        Employee employee = employee(12L, "ada@example.com");
        when(employeeRepository.findById(12L)).thenReturn(Optional.of(employee));

        EmployeeResponseDTO response = employeeService.getEmployeeById(12L);

        assertEquals(12L, response.id());
        assertEquals("ada@example.com", response.email());
    }

    @Test
    void throwsWhenEmployeeIdDoesNotExist() {
        when(employeeRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.getEmployeeById(404L));
    }

    @Test
    void returnsAllEmployeesAndTotalEmployeeCount() {
        Employee employee = employee(12L, "ada@example.com");
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(employeeRepository.count()).thenReturn(1L);

        List<EmployeeResponseDTO> response = employeeService.getAllEmployees();

        assertEquals(1, response.size());
        assertEquals("ada@example.com", response.get(0).email());
        assertEquals(1L, employeeService.getEmployeeCount());
        verify(employeeRepository).findAll();
        verify(employeeRepository).count();
    }

    @Test
    void returnsEmployeeProfileByEmail() {
        Employee employee = employee(12L, "ada@example.com");
        when(employeeRepository.findByEmailIgnoreCaseAndPasswordHashIsNotNull(
                "ada@example.com"))
                .thenReturn(Optional.of(employee));

        EmployeeResponseDTO response =
                employeeService.getEmployeeByEmail("ada@example.com");

        assertEquals(12L, response.id());
        assertEquals("ada@example.com", response.email());
    }

    @Test
    void throwsWhenEmailHasNoEmployeeAccount() {
        when(employeeRepository.findByEmailIgnoreCaseAndPasswordHashIsNotNull(
                "missing@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.getEmployeeByEmail("missing@example.com"));
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
    void updatesEmployeeAndLeavesPasswordUnchangedWhenNoNewPasswordIsProvided() {
        Employee existing = employee(12L, "ada@example.com");
        existing.setPasswordHash("{bcrypt}existing-hash");
        EmployeeRequestDTO request = employeeRequest("Ada", "Byron", "ada@example.com");
        request.setPhone("555-0100");
        when(employeeRepository.findById(12L)).thenReturn(Optional.of(existing));
        when(employeeRepository.existsByEmailIgnoreCaseAndIdNot("ada@example.com", 12L))
                .thenReturn(false);
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeResponseDTO response = employeeService.updateEmployee(12L, request);

        assertEquals("Byron", response.lastName());
        assertEquals("555-0100", response.phone());
        assertEquals("{bcrypt}existing-hash", existing.getPasswordHash());
        verify(passwordEncoder, never()).encode(any());
        verify(employeeRepository).save(existing);
    }

    @Test
    void updatesEmployeePasswordUsingPasswordEncoder() {
        Employee existing = employee(12L, "ada@example.com");
        EmployeeRequestDTO request = employeeRequest("Ada", "Lovelace", "ada@example.com");
        request.setPassword("new-password");
        when(employeeRepository.findById(12L)).thenReturn(Optional.of(existing));
        when(employeeRepository.existsByEmailIgnoreCaseAndIdNot("ada@example.com", 12L))
                .thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("{bcrypt}new-hash");
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        employeeService.updateEmployee(12L, request);

        assertEquals("{bcrypt}new-hash", existing.getPasswordHash());
    }

    @Test
    void rejectsUpdateWhenEmailBelongsToAnotherEmployee() {
        EmployeeRequestDTO request = employeeRequest("Ada", "Lovelace", "used@example.com");
        when(employeeRepository.findById(12L)).thenReturn(Optional.of(employee(12L, "ada@example.com")));
        when(employeeRepository.existsByEmailIgnoreCaseAndIdNot("used@example.com", 12L))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> employeeService.updateEmployee(12L, request));

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
    void rejectsTransferDateBeforeHireDate() {
        Employee employee = employee(12L, "ada@example.com");
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        Department destination = new Department();
        destination.setId(7L);
        when(employeeRepository.findById(12L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(7L)).thenReturn(Optional.of(destination));

        assertThrows(IllegalArgumentException.class,
                () -> employeeService.transferEmployeeEntity(
                        12L, 7L, LocalDate.of(2019, 12, 31)));

        verify(historyRepository, never()).save(any(EmployeeDepartmentHistory.class));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void mapsDepartmentHistoryForAnEmployee() {
        Employee employee = employee(12L, "ada@example.com");
        Department from = department(3L, "Operations");
        Department to = department(7L, "Engineering");
        EmployeeDepartmentHistory history = new EmployeeDepartmentHistory();
        history.setId(21L);
        history.setEmployee(employee);
        history.setFromDepartment(from);
        history.setToDepartment(to);
        history.setEffectiveDate(LocalDate.of(2024, 1, 2));
        when(employeeRepository.findById(12L)).thenReturn(Optional.of(employee));
        when(historyRepository.findByEmployee_IdOrderByEffectiveDateAscIdAsc(12L))
                .thenReturn(List.of(history));

        List<EmployeeDepartmentHistoryResponseDTO> response =
                employeeService.getDepartmentHistory(12L);

        assertEquals(1, response.size());
        assertEquals(21L, response.get(0).id());
        assertEquals("Operations", response.get(0).fromDepartmentName());
        assertEquals("Engineering", response.get(0).toDepartmentName());
    }

    @Test
    void doesNotQueryHistoryWhenEmployeeDoesNotExist() {
        when(employeeRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.getDepartmentHistory(404L));

        verifyNoInteractions(historyRepository);
    }

    @Test
    void deletesEmployeeAndItsHistory() {
        Employee employee = employee(12L, "ada@example.com");
        when(employeeRepository.findById(12L)).thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(12L);

        verify(historyRepository).deleteByEmployee_Id(12L);
        verify(employeeRepository).delete(employee);
    }

    @Test
    void doesNotDeleteHistoryWhenEmployeeDoesNotExist() {
        when(employeeRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.deleteEmployee(404L));

        verifyNoInteractions(historyRepository);
        verify(employeeRepository, never()).delete(any(Employee.class));
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

    @Test
    void rejectsNegativePageAndZeroPageSizeBeforeQueryingRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.searchEmployees(null, null, -1, 10));
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.searchEmployees(null, null, 0, 0));
        verifyNoInteractions(employeeRepository);
    }

    private Employee employee(Long id, String email) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setFirstName("Ada");
        employee.setLastName("Lovelace");
        employee.setEmail(email);
        return employee;
    }

    private EmployeeRequestDTO employeeRequest(
            String firstName, String lastName, String email) {
        EmployeeRequestDTO request = new EmployeeRequestDTO();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setEmail(email);
        return request;
    }

    private Department department(Long id, String name) {
        Department department = new Department();
        department.setId(id);
        department.setName(name);
        return department;
    }
}
