package com.trusthold.property.service;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.property.dtos.CreatePropertyDto;
import com.trusthold.property.model.Property;
import com.trusthold.property.repository.PropertyRepository;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.Guard;
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class PropertyService {

  private final PropertyRepository propertyRepository;
  private final UserService userService;
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
    userService.isAValidLandlord(authUser);

    Property property = propertyMapper.toPropertyEntity(createPropertyDto);
    property.setLandlordId(authUser.id());
    propertyRepository.save(property);
    log.info("Property with id {} created successfully", property.getId());
  }

  /**
   * Retrieves all properties for the authenticated landlord.
   *
   * @param authUser authenticated landlord
   * @return list of properties
   * @throws AppException if the user is not a landlord
   */
  public List<Property> getAllProperties(@NotNull AuthUser authUser) {
    log.debug("Fetching properties for user with id {}", authUser.id());
    Guard.requireNotNull(authUser, new AppException(ErrorInfo.UNAUTHORIZED));
    userService.isAValidLandlord(authUser);

    return propertyRepository.findByLandlordId(authUser.id());
  }

  /**
   * Retrieves a property by its ID for the authenticated landlord.
   *
   * @param authUser authenticated landlord
   * @param id       property ID
   * @return property
   * @throws AppException if the user is not a landlord or the property is not found
   */
  public Property getPropertyById(@NotNull AuthUser authUser, @NotNull UUID id){
    log.debug("Fetching property with id {}", id);
    Guard.requireNotNull(authUser, new AppException(ErrorInfo.UNAUTHORIZED));
    userService.isAValidLandlord(authUser);

    return propertyRepository
        .findByIdAndLandlordId(id, authUser.id())
        .orElseThrow(() -> new AppException(ErrorInfo.PROPERTY_NOT_FOUND));
  }

  // Helper Methods
  public boolean exists(UUID id){
    return propertyRepository.existsById(id);
  }
}
