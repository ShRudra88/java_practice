package com.nexora.workforce.controller;

import com.nexora.workforce.entity.Department;
import com.nexora.workforce.entity.Employee;
import com.nexora.workforce.service.DepartmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Department> createDepartment(
            @Valid @RequestBody Department department) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createDepartment(department));
    }

    @GetMapping
    public List<Department> getAllDepartments() {
        return service.getAllDepartments();
    }

    @GetMapping("/{id}")
    public Department getDepartmentById(@PathVariable Long id) {
        return service.getDepartmentById(id);
    }

    @PutMapping("/{departmentId}/employees/{employeeId}")
    public Employee assignEmployee(
            @PathVariable Long departmentId,
            @PathVariable Long employeeId) {
        return service.assignEmployee(departmentId, employeeId);
    }
}
