package com.trusthold.property.dtos;

import com.trusthold.property.model.PropertyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreatePropertyDto(
    @NotNull(message = "Landlord is required")
    UUID landlordId,

    @NotBlank(message = "Property name is required")
    @Size(max = 150, message = "Property name must be at most 150 characters")
    String name,

    @NotNull(message = "Property type cannot be null")
    PropertyType type,

    @NotBlank(message = "Address line is required")
    @Size(max = 255, message = "Address must be at most 255 characters")
    String addressLine,

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must be at most 100 characters")
    String city,

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must be at most 1000 characters")
    String description
) {
}
