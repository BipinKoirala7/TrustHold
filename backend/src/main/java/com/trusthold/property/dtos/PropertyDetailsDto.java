package com.trusthold.property.dtos;

import com.trusthold.property.model.Property;

import java.util.UUID;

public record PropertyDetailsDto(
    UUID id,
    UUID landlordId,
    String name,
    String type,
    String addressLine,
    String city,
    String description,
    boolean isArchived,
    String createdAt,
    String updatedAt,

    // Additional fields for detail
    boolean isPropertyLeased
) {

  public PropertyDetailsDto(Property property, boolean isPropertyLeased) {
    this(
      property.getId(),
      property.getLandlordId(),
      property.getName(),
      property.getType().toString(),
      property.getAddressLine(),
      property.getCity(),
      property.getDescription(),
      property.getIsArchived(),
      property.getCreatedAt().toString(),
      property.getUpdatedAt().toString(),
      isPropertyLeased
    );
  }
}
