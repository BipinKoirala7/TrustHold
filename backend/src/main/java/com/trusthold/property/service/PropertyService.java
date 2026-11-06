package com.trusthold.property.service;

import com.trusthold.property.dtos.CreatePropertyDto;
import com.trusthold.property.repository.PropertyRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class PropertyService {

  private final PropertyRepository propertyRepository;

  public void createProperty(@NotNull CreatePropertyDto createPropertyDto) {

  }

  // Helper Methods
  public boolean exists(UUID id){
    return propertyRepository.existsById(id);
  }
}
