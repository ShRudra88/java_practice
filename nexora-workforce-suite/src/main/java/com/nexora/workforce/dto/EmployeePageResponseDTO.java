package com.nexora.workforce.dto;

import java.util.List;

public record EmployeePageResponseDTO(
        List<EmployeeResponseDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious) {
}
