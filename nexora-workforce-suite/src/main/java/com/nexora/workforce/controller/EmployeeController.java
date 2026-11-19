
package com.nexora.workforce.controller;

import com.nexora.workforce.dto.EmployeeDepartmentHistoryResponseDTO;
import com.nexora.workforce.dto.EmployeePageResponseDTO;
import com.nexora.workforce.dto.EmployeeRequestDTO;
import com.nexora.workforce.dto.EmployeeResponseDTO;
import com.nexora.workforce.dto.EmployeeTransferRequestDTO;
import com.nexora.workforce.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @Valid @RequestBody EmployeeRequestDTO employee) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createEmployee(employee));
    }

    @GetMapping
    public List<EmployeeResponseDTO> getAllEmployees() {
        return service.getAllEmployees();
    }

    @GetMapping("/{id}")
    public EmployeeResponseDTO getEmployeeById(@PathVariable Long id) {
        return service.getEmployeeById(id);
    }

    @GetMapping("/{id}/department-history")
    public List<EmployeeDepartmentHistoryResponseDTO> getDepartmentHistory(
            @PathVariable Long id) {
        return service.getDepartmentHistory(id);
    }

    @PostMapping("/{id}/transfers")
    public EmployeeResponseDTO transferEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeTransferRequestDTO request) {
        return service.transferEmployee(id, request);
    }

    @PutMapping("/{id}")
    public EmployeeResponseDTO updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDTO employee) {
        return service.updateEmployee(id, employee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(
            @PathVariable Long id) {
        service.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public EmployeePageResponseDTO searchEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.searchEmployees(name, departmentId, page, size);
    }
}
