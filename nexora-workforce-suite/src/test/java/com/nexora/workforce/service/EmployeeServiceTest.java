package com.nexora.workforce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nexora.workforce.dto.EmployeeRequestDTO;
import com.nexora.workforce.dto.EmployeeResponseDTO;
import com.nexora.workforce.entity.Department;
import com.nexora.workforce.entity.Employee;
import com.nexora.workforce.exception.DuplicateResourceException;
import com.nexora.workforce.repository.DepartmentRepository;
import com.nexora.workforce.repository.EmployeeRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

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

        assertNull(savedEmployee.getDepartmentId());
        assertNull(savedEmployee.getDepartmentName());
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

        assertEquals(12L, response.getId());
        assertEquals("Ada", response.getFirstName());
        assertEquals(7L, response.getDepartmentId());
        assertEquals("Engineering", response.getDepartmentName());
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
}
