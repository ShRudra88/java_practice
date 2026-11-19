package com.nexora.workforce.service;

import com.nexora.workforce.entity.Department;
import com.nexora.workforce.entity.Employee;
import com.nexora.workforce.exception.ResourceNotFoundException;
import com.nexora.workforce.repository.DepartmentRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeService employeeService;

    public DepartmentService(
            DepartmentRepository departmentRepository,
            EmployeeService employeeService) {
        this.departmentRepository = departmentRepository;
        this.employeeService = employeeService;
    }

    public Department createDepartment(Department department) {
        return departmentRepository.save(department);
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Department getDepartmentById(Long id) {
        return departmentRepository.findWithEmployeesById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found: " + id));
    }

    public Employee assignEmployee(Long departmentId, Long employeeId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found: " + departmentId));
        return employeeService.transferEmployeeEntity(
                employeeId, department.getId(), LocalDate.now());
    }
}
