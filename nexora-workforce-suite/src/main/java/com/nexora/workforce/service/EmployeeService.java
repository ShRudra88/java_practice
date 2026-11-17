
package com.nexora.workforce.service;

import com.nexora.workforce.entity.Employee;
import com.nexora.workforce.repository.EmployeeRepository;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    // CREATE
    public Employee createEmployee(Employee employee) {
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
                new RuntimeException("Employee not found: " + id)
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
        employee.setDepartment(updatedEmployee.getDepartment());
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
}
