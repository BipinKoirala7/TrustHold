package com.trusthold.property.dtos;

import com.trusthold.property.model.PropertyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePropertyDto(
    @NotNull(message = "landlordId is required")
    UUID landlordId,

    @NotBlank(message = "name is required")
    String name,

    @NotNull(message = "type is required")
    PropertyType type,

    @NotBlank(message = "addressLine is required")
    String addressLine,

    @NotBlank(message = "city is required")
    String city,

    @NotBlank(message = "description is required")
    String description
) {
}
