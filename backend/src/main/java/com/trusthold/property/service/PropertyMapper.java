package com.trusthold.property.service;

import com.trusthold.property.dtos.CreatePropertyDto;
import com.trusthold.property.dtos.PropertySummaryDto;
import com.trusthold.property.model.Property;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PropertyMapper {

  Property toPropertyEntity(CreatePropertyDto createPropertyDto);
  PropertySummaryDto toPropertySummary(Property property);
}
