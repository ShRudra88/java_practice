package com.example.employeemanagement;

import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.service.DepartmentService;
import com.example.employeemanagement.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class EmployeeManagementSystemApplicationTests {

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private EmployeeService employeeService;

	@Test
	void contextLoads() {
	}

	@Test
	void createsAndRetrievesEmployeeWithPersistedDepartment() {
		Department department = departmentService.createDepartment(new Department("Engineering", "Product development"));
		Employee employee = new Employee("Ada", "Lovelace", "ada@example.com", new BigDecimal("125000"), department);

		Employee created = employeeService.createEmployee(employee);
		Employee retrieved = employeeService.getEmployeeById(created.getId());

		assertEquals("ada@example.com", retrieved.getEmail());
		assertEquals(department.getId(), retrieved.getDepartment().getId());
	}

	@Test
	void preventsDeletingDepartmentWithEmployees() {
		Department department = departmentService.createDepartment(new Department("Operations", null));
		employeeService.createEmployee(new Employee(
				"Grace", "Hopper", "grace@example.com", new BigDecimal("110000"), department));

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class, () -> departmentService.deleteDepartment(department.getId()));

		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
		assertTrue(departmentRepository.existsById(department.getId()));
	}
}
