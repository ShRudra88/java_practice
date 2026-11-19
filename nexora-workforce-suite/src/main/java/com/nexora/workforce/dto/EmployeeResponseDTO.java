package com.nexora.workforce.dto;

import java.time.LocalDate;

public record EmployeeResponseDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        Long departmentId,
        String departmentName,
        String jobTitle,
        Double salary,
        LocalDate hireDate,
        String status) {
}
