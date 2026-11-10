package com.trusthold.lease.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LeaseActivityEvent(
    @NotNull(message = "Lease is required")
    UUID leaseId,

    @NotBlank(message = "Activity is required")
    String activity,

    @NotBlank(message = "Description is required")
    String description
) {
}
