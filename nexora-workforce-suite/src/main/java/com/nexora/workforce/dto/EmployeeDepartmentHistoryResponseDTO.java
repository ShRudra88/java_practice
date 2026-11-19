package com.nexora.workforce.dto;

import java.time.LocalDate;

public record EmployeeDepartmentHistoryResponseDTO(
        Long id,
        Long fromDepartmentId,
        String fromDepartmentName,
        Long toDepartmentId,
        String toDepartmentName,
        LocalDate effectiveDate) {
}
