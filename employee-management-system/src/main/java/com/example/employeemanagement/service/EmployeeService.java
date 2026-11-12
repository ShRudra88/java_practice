package com.example.employeemanagement.service;

import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class EmployeeService {

	private final EmployeeRepository employeeRepository;
	private final DepartmentRepository departmentRepository;

	public EmployeeService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
		this.employeeRepository = employeeRepository;
		this.departmentRepository = departmentRepository;
	}

	@Transactional(readOnly = true)
	public List<Employee> getAllEmployees() {
		return employeeRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Employee getEmployeeById(Long id) {
		return employeeRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
	}

	public Employee createEmployee(Employee employee) {
		employee.setId(null);
		employee.setDepartment(requireDepartment(employee.getDepartment()));
		return employeeRepository.save(employee);
	}

	public Employee updateEmployee(Long id, Employee employee) {
		Employee existing = getEmployeeById(id);
		existing.setFirstName(employee.getFirstName());
		existing.setLastName(employee.getLastName());
		existing.setEmail(employee.getEmail());
		existing.setSalary(employee.getSalary());
		existing.setDepartment(requireDepartment(employee.getDepartment()));
		return employeeRepository.save(existing);
	}

	public void deleteEmployee(Long id) {
		if (!employeeRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
		}
		employeeRepository.deleteById(id);
	}

	private Department requireDepartment(Department department) {
		if (department == null || department.getId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A department id is required");
		}
		return departmentRepository.findById(department.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department not found"));
	}
}
