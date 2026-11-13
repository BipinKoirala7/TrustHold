package com.trusthold.property.service;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.property.dtos.CreatePropertyDto;
import com.trusthold.property.model.Property;
import com.trusthold.property.repository.PropertyRepository;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTests {

  @InjectMocks
  private PropertyService propertyService;

  @Mock
  private PropertyRepository propertyRepository;

  @Mock
  private PropertyMapper propertyMapper;

  @Mock
  private CreatePropertyDto createPropertyDto;

  @Mock
  private Property property;

  private AuthUser landlordUser;
  private UUID landlordId;
  private UUID propertyId;

  @BeforeEach
  void setUp() {
    landlordId = UUID.randomUUID();
    propertyId = UUID.randomUUID();

    landlordUser = new AuthUser(landlordId, "dummy-token", Role.LANDLORD);
  }

  @Nested
  class CreatePropertyTests {

    @Test
    void shouldThrowUnauthorized_whenAuthUserIsNull() {
      assertThatThrownBy(() -> propertyService.createProperty(null, createPropertyDto))
          .isInstanceOf(AppException.class)
          .hasFieldOrPropertyWithValue("errorInfo", ErrorInfo.UNAUTHORIZED);

      verifyNoInteractions(propertyMapper, propertyRepository);
    }

    @Test
    void shouldPropagateException_whenSaveFails() {
      when(propertyMapper.toPropertyEntity(createPropertyDto)).thenReturn(property);
      when(propertyRepository.save(property))
          .thenThrow(new RuntimeException("Database connection failed"));

      assertThatThrownBy(() -> propertyService.createProperty(landlordUser, createPropertyDto))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Database connection failed");

      verify(propertyRepository).save(property);
    }

    @Test
    void shouldMapAssignLandlordAndSave_inOrder() {
      when(propertyMapper.toPropertyEntity(createPropertyDto)).thenReturn(property);

      propertyService.createProperty(landlordUser, createPropertyDto);

      InOrder inOrder = inOrder(propertyMapper, property, propertyRepository);
      inOrder.verify(propertyMapper).toPropertyEntity(createPropertyDto);
      inOrder.verify(property).setLandlordId(landlordId);
      inOrder.verify(propertyRepository).save(property);
    }
  }

  @Nested
  class GetAllPropertiesTests {

    @Test
    void shouldThrowUnauthorized_whenAuthUserIsNull() {
      assertThatThrownBy(() -> propertyService.getAllProperties(null))
          .isInstanceOf(AppException.class)
          .hasFieldOrPropertyWithValue("errorInfo", ErrorInfo.UNAUTHORIZED);

      verifyNoInteractions(propertyRepository);
    }

    @Test
    void shouldReturnPropertiesOwnedByLandlord() {
      List<Property> properties = List.of(property);
      when(propertyRepository.findByLandlordId(landlordId)).thenReturn(properties);

      List<Property> result = propertyService.getAllProperties(landlordUser);

      assertThat(result).isSameAs(properties);
    }

    @Test
    void shouldReturnEmptyList_whenLandlordHasNoProperties() {
      when(propertyRepository.findByLandlordId(landlordId)).thenReturn(List.of());

      assertThat(propertyService.getAllProperties(landlordUser)).isEmpty();
    }
  }

  @Nested
  class GetPropertyByIdTests {

    @Test
    void shouldThrowUnauthorized_whenAuthUserIsNull() {
      assertThatThrownBy(() -> propertyService.getPropertyById(null, propertyId))
          .isInstanceOf(AppException.class)
          .hasFieldOrPropertyWithValue("errorInfo", ErrorInfo.UNAUTHORIZED);

      verifyNoInteractions(propertyRepository);
    }

    @Test
    void shouldReturnProperty_whenOwnedByLandlord() {
      when(propertyRepository.findByIdAndLandlordId(propertyId, landlordId))
          .thenReturn(Optional.of(property));

      Property result = propertyService.getPropertyById(landlordUser, propertyId);

      assertThat(result).isSameAs(property);
    }

    @Test
    void shouldThrowPropertyNotFound_whenPropertyDoesNotExistOrBelongsToAnotherLandlord() {
      when(propertyRepository.findByIdAndLandlordId(propertyId, landlordId))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> propertyService.getPropertyById(landlordUser, propertyId))
          .isInstanceOf(AppException.class)
          .hasFieldOrPropertyWithValue("errorInfo", ErrorInfo.PROPERTY_NOT_FOUND);
    }
  }

  @Nested
  class ExistsTests {

    @Test
    void shouldReturnTrue_whenPropertyExists() {
      when(propertyRepository.existsById(propertyId)).thenReturn(true);

      assertThat(propertyService.exists(propertyId)).isTrue();
    }

    @Test
    void shouldReturnFalse_whenPropertyDoesNotExist() {
      when(propertyRepository.existsById(propertyId)).thenReturn(false);

      assertThat(propertyService.exists(propertyId)).isFalse();
    }
  }
}