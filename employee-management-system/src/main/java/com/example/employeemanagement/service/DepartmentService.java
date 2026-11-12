package com.example.employeemanagement.service;

import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class DepartmentService {

	private final DepartmentRepository departmentRepository;
	private final EmployeeRepository employeeRepository;

	public DepartmentService(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
		this.departmentRepository = departmentRepository;
		this.employeeRepository = employeeRepository;
	}

	@Transactional(readOnly = true)
	public List<Department> getAllDepartments() {
		return departmentRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Department getDepartmentById(Long id) {
		return departmentRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
	}

	public Department createDepartment(Department department) {
		department.setId(null);
		return departmentRepository.save(department);
	}

	public Department updateDepartment(Long id, Department department) {
		Department existing = getDepartmentById(id);
		existing.setName(department.getName());
		existing.setDescription(department.getDescription());
		return departmentRepository.save(existing);
	}

	public void deleteDepartment(Long id) {
		if (!departmentRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found");
		}
		if (employeeRepository.existsByDepartmentId(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Department has employees and cannot be deleted");
		}
		departmentRepository.deleteById(id);
	}
}
