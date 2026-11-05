package com.trusthold.lease.dtos;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateLeaseDto(
    @NotNull(message = "Property id cannot be null")
    UUID propertyId,

    @NotNull(message = "Tenant id cannot be null")
    UUID tenantId,

    @NotNull(message = "Monthly rent cannot be null")
    BigDecimal monthlyRent,

    @NotNull(message = "Deposit amount cannot be null")
    BigDecimal depositAmount,

    @NotNull(message = "Move in initiated date cannot be null")
    Instant startDate,

    @NotNull(message = "End date cannot be null")
    Instant endDate,

    @NotNull(message = "Move in date cannot be null")
    Instant movedInAt
) {
}
