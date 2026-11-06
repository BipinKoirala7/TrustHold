package com.trusthold.property.service;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.property.dtos.CreatePropertyDto;
import com.trusthold.property.model.Property;
import com.trusthold.property.repository.PropertyRepository;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.model.Role;
import com.trusthold.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PropertyServiceTests {

  @InjectMocks
  private PropertyService propertyService;

  @Mock
  private PropertyRepository propertyRepository;

  @Mock
  private UserService userService;

  @Mock
  private PropertyMapper propertyMapper;

  @Mock
  private CreatePropertyDto createPropertyDto;

  private AuthUser sampleAuthUser;
  private UUID landlordId;

  @BeforeEach
  void setUp() {
    landlordId = UUID.randomUUID();

    sampleAuthUser = new AuthUser(landlordId, "dummy-token", Role.LANDLORD);
  }

  @Nested
  class CreatePropertyTests {

    @Test
    void shouldThrowAppException_whenUserIsNotALandlord() {
      // Arrange
      doThrow(new AppException(ErrorInfo.UNAUTHORIZED))
          .when(userService).isAValidLandlord(sampleAuthUser);

      // Act & Assert
      assertThatThrownBy(() -> propertyService.createProperty(sampleAuthUser, createPropertyDto))
          .isInstanceOf(AppException.class)
          .extracting("errorInfo")
          .isEqualTo(ErrorInfo.UNAUTHORIZED);

      verifyNoInteractions(propertyMapper, propertyRepository);
    }

    @Test
    void shouldPropagateError_whenPropertySaveFails() {
      // Arrange
      Property property = new Property();

      when(propertyMapper.toPropertyEntity(createPropertyDto)).thenReturn(property);
      doThrow(new RuntimeException("Database connection failed"))
          .when(propertyRepository).save(any(Property.class));

      // Act & Assert
      assertThatThrownBy(() -> propertyService.createProperty(sampleAuthUser, createPropertyDto))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Database connection failed");

      verify(propertyRepository).save(property);
    }

    @Test
    void shouldCreatePropertySuccessfully() {
      // Arrange
      Property property = new Property();

      when(propertyMapper.toPropertyEntity(createPropertyDto)).thenReturn(property);

      // Act
      propertyService.createProperty(sampleAuthUser, createPropertyDto);

      // Assert
      InOrder inOrder = inOrder(userService, propertyMapper, propertyRepository);
      inOrder.verify(userService).isAValidLandlord(sampleAuthUser);
      inOrder.verify(propertyMapper).toPropertyEntity(createPropertyDto);
      inOrder.verify(propertyRepository).save(property);
    }

    @Test
    void shouldAssignAuthenticatedUserAsLandlord_whenPropertyIsCreated() {
      // Arrange
      Property property = new Property();

      when(propertyMapper.toPropertyEntity(createPropertyDto)).thenReturn(property);

      // Act
      propertyService.createProperty(sampleAuthUser, createPropertyDto);

      // Assert
      ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);
      verify(propertyRepository).save(propertyCaptor.capture());
      assertThat(propertyCaptor.getValue().getLandlordId()).isEqualTo(landlordId);
    }
  }
}