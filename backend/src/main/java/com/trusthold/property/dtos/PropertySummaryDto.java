package com.trusthold.property.dtos;

import com.trusthold.property.model.PropertyType;

public record PropertySummaryDto(
    String name,
    PropertyType type,
    String city
) {
}
