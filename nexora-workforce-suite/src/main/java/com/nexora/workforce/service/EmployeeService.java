
package com.nexora.workforce.service;

import com.nexora.workforce.entity.Employee;
import com.nexora.workforce.entity.Department;
import com.nexora.workforce.exception.ResourceNotFoundException;
import com.nexora.workforce.repository.DepartmentRepository;
import com.nexora.workforce.repository.EmployeeRepository;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;
    private final DepartmentRepository departmentRepository;

    public EmployeeService(
            EmployeeRepository repository,
            DepartmentRepository departmentRepository) {
        this.repository = repository;
        this.departmentRepository = departmentRepository;
    }

    // CREATE
    public Employee createEmployee(Employee employee) {
        employee.setDepartment(resolveDepartment(employee.getDepartment()));
        return repository.save(employee);
    }

    // READ ALL
    public List<Employee> getAllEmployees() {
        return repository.findAll();
    }

    // READ BY ID
    public Employee getEmployeeById(Long id) {
        return repository.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Employee not found: " + id)
            );
    }

    // UPDATE
    public Employee updateEmployee(
            Long id, Employee updatedEmployee) {

        Employee employee = getEmployeeById(id);

        employee.setFirstName(updatedEmployee.getFirstName());
        employee.setLastName(updatedEmployee.getLastName());
        employee.setEmail(updatedEmployee.getEmail());
        employee.setPhone(updatedEmployee.getPhone());
        employee.setDepartment(resolveDepartment(updatedEmployee.getDepartment()));
        employee.setJobTitle(updatedEmployee.getJobTitle());
        employee.setSalary(updatedEmployee.getSalary());
        employee.setHireDate(updatedEmployee.getHireDate());
        employee.setStatus(updatedEmployee.getStatus());

        return repository.save(employee);
    }

    // DELETE
    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        repository.delete(employee);
    }

    // SEARCH
    public List<Employee> searchEmployees(String name) {
        return repository.findByFirstNameContainingIgnoreCase(name);
    }

    private Department resolveDepartment(Department departmentReference) {
        if (departmentReference == null) {
            return null;
        }

        if (departmentReference.getId() != null) {
            return departmentRepository.findById(departmentReference.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found: " + departmentReference.getId()));
        }

        if (departmentReference.getName() != null) {
            if (departmentReference.getName().isBlank()) {
                return null;
            }

            List<Department> matches = departmentRepository.findAllByNameIgnoreCase(
                    departmentReference.getName().trim());
            if (matches.size() == 1) {
                return matches.get(0);
            }
            if (matches.isEmpty()) {
                throw new ResourceNotFoundException(
                        "Department not found: " + departmentReference.getName());
            }
            throw new IllegalArgumentException(
                    "Multiple departments match name: " + departmentReference.getName());
        }

        throw new IllegalArgumentException(
                "department must contain an id or a department name");
    }
}
