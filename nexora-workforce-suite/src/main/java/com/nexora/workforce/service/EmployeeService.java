
package com.nexora.workforce.service;

import com.nexora.workforce.dto.EmployeeRequestDTO;
import com.nexora.workforce.dto.EmployeeResponseDTO;
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
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO request) {
        Employee employee = toEmployee(request);
        employee.setDepartment(resolveDepartment(employee.getDepartment()));
        return toResponse(repository.save(employee));
    }

    // READ ALL
    public List<EmployeeResponseDTO> getAllEmployees() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // READ BY ID
    public EmployeeResponseDTO getEmployeeById(Long id) {
        return toResponse(findEmployeeEntityById(id));
    }

    private Employee findEmployeeEntityById(Long id) {
        return repository.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Employee not found: " + id)
            );
    }

    // UPDATE
    public EmployeeResponseDTO updateEmployee(
            Long id, EmployeeRequestDTO request) {

        Employee updatedEmployee = toEmployee(request);
        Employee currentEmployee = findEmployeeEntityById(id);
        currentEmployee.setFirstName(updatedEmployee.getFirstName());
        currentEmployee.setLastName(updatedEmployee.getLastName());
        currentEmployee.setEmail(updatedEmployee.getEmail());
        currentEmployee.setPhone(updatedEmployee.getPhone());
        currentEmployee.setDepartment(resolveDepartment(updatedEmployee.getDepartment()));
        currentEmployee.setJobTitle(updatedEmployee.getJobTitle());
        currentEmployee.setSalary(updatedEmployee.getSalary());
        currentEmployee.setHireDate(updatedEmployee.getHireDate());
        currentEmployee.setStatus(updatedEmployee.getStatus());

        return toResponse(repository.save(currentEmployee));
    }

    // DELETE
    public void deleteEmployee(Long id) {
        Employee employee = findEmployeeEntityById(id);
        repository.delete(employee);
    }

    // SEARCH
    public List<EmployeeResponseDTO> searchEmployees(String name) {
        return repository.findByFirstNameContainingIgnoreCase(name).stream()
                .map(this::toResponse)
                .toList();
    }

    private Employee toEmployee(EmployeeRequestDTO request) {
        Employee employee = new Employee();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setDepartment(request.getDepartment());
        employee.setJobTitle(request.getJobTitle());
        employee.setSalary(request.getSalary());
        employee.setHireDate(request.getHireDate());
        employee.setStatus(request.getStatus());
        return employee;
    }

    private EmployeeResponseDTO toResponse(Employee employee) {
        EmployeeResponseDTO response = new EmployeeResponseDTO();
        response.setId(employee.getId());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setEmail(employee.getEmail());
        response.setPhone(employee.getPhone());
        if (employee.getDepartment() != null) {
            response.setDepartmentId(employee.getDepartment().getId());
            response.setDepartmentName(employee.getDepartment().getName());
        }
        response.setJobTitle(employee.getJobTitle());
        response.setSalary(employee.getSalary());
        response.setHireDate(employee.getHireDate());
        response.setStatus(employee.getStatus());
        return response;
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
