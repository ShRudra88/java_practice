package com.nexora.workforce.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nexora.workforce.entity.Department;
import com.nexora.workforce.entity.Employee;
import com.nexora.workforce.repository.DepartmentRepository;
import com.nexora.workforce.repository.EmployeeRepository;
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
        Employee employee = new Employee();
        Department blankDepartment = new Department();
        blankDepartment.setName("");
        employee.setDepartment(blankDepartment);
        when(employeeRepository.save(employee)).thenReturn(employee);

        Employee savedEmployee = employeeService.createEmployee(employee);

        assertNull(savedEmployee.getDepartment());
        verify(employeeRepository).save(employee);
        verifyNoInteractions(departmentRepository);
    }
}
