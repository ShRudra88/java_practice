package com.nexora.workforce.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record EmployeeTransferRequestDTO(
        @NotNull @Positive Long departmentId,
        @NotNull @PastOrPresent LocalDate effectiveDate) {
}
