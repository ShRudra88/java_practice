
package com.nexora.workforce.controller;

import com.nexora.workforce.dto.EmployeeRequestDTO;
import com.nexora.workforce.dto.EmployeeResponseDTO;
import com.nexora.workforce.service.EmployeeService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    // CREATE EMPLOYEE
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @Valid @RequestBody EmployeeRequestDTO employee) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createEmployee(employee));
    }

    // GET ALL EMPLOYEES
    @GetMapping
    public List<EmployeeResponseDTO> getAllEmployees() {
        return service.getAllEmployees();
    }

    // GET EMPLOYEE BY ID
    @GetMapping("/{id}")
    public EmployeeResponseDTO getEmployeeById(@PathVariable Long id) {
        return service.getEmployeeById(id);
    }

    // UPDATE EMPLOYEE
    @PutMapping("/{id}")
    public EmployeeResponseDTO updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDTO employee) {

        return service.updateEmployee(id, employee);
    }

    // DELETE EMPLOYEE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(
            @PathVariable Long id) {

        service.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    // SEARCH EMPLOYEE
    @GetMapping("/search")
    public List<EmployeeResponseDTO> searchEmployees(
            @RequestParam String name) {

        return service.searchEmployees(name);
    }
}
