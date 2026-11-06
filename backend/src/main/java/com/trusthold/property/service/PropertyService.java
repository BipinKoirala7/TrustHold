package com.trusthold.property.service;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.property.dtos.CreatePropertyDto;
import com.trusthold.property.model.Property;
import com.trusthold.property.repository.PropertyRepository;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.model.Role;
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
  private final PropertyMapper propertyMapper;

  /**
   * Creates a new property for the authenticated user.
   *
   * @param authUser authenticated user
   * @param createPropertyDto the property details
   * @throws AppException if the user is not a landlord
   */
  public void createProperty(@NotNull AuthUser authUser, @NotNull CreatePropertyDto createPropertyDto) {
    log.debug("Creating property for user with id {}", authUser.id());

    if(authUser.role() != Role.LANDLORD){
      log.warn("User with id {} is not a landlord", authUser.id());
      throw new AppException(ErrorInfo.TENANT_CANNOT_CREATE_PROPERTY);
    }

    Property property = propertyMapper.toPropertyEntity(createPropertyDto);
    propertyRepository.save(property);
    log.info("Property with id {} created successfully", property.getId());
  }

  // Helper Methods
  public boolean exists(UUID id){
    return propertyRepository.existsById(id);
  }
}
