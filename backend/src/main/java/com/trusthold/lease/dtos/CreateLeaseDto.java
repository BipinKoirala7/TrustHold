package com.trusthold.lease.dtos;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateLeaseDto(
    @NotNull(message = "Property is required")
    UUID propertyId,

    @NotBlank(message = "Tenant is required")
    @Email(message = "Tenant email must be a valid email address")
    String tenantEmail,

    @NotNull(message = "Monthly rent is required")
    @Positive(message = "Monthly rent must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Monthly rent can have at most 10 digits and 2 decimal places")
    BigDecimal monthlyRent,

    @NotNull(message = "Deposit amount is required")
    @Positive(message = "Deposit amount must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Deposit amount can have at most 10 digits and 2 decimal places")
    BigDecimal depositAmount,

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date cannot be in the past")
    Instant startDate
) {
}
