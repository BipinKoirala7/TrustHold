package com.trusthold.property.integration;

import com.trusthold.auth.dtos.LoginUserDto;
import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.property.dtos.CreatePropertyDto;
import com.trusthold.property.model.Property;
import com.trusthold.property.model.PropertyType;
import com.trusthold.property.repository.PropertyRepository;
import com.trusthold.shared.dtos.ApiResponse;
import com.trusthold.shared.exception.PropertyException;
import com.trusthold.shared.integration.IntegrationTestSupport;
import com.trusthold.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the Property endpoints: real security chain, real method
 * security, real database.
 * <p>
 * Business rules live in PropertyServiceTests; per-exception HTTP mapping lives in
 * PropertyControllerTests. Here we prove what only the full stack can show:
 * authentication, role checks, ownership isolation and persistence.
 */
public class PropertyIntegrationTests extends IntegrationTestSupport {

  private static final String PROPERTY_PATH = "/property";
  private static final String PASSWORD = "Password123!";

  private static final String LANDLORD_EMAIL = "landlord@example.com";
  private static final String OTHER_LANDLORD_EMAIL = "otherlandlord@example.com";
  private static final String TENANT_EMAIL = "tenant@example.com";

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PropertyRepository propertyRepository;

  private HttpHeaders landlordHeaders;
  private HttpHeaders otherLandlordHeaders;
  private HttpHeaders tenantHeaders;

  @BeforeEach
  void setUp() {
    cleanDatabase();

    registerUser("landlord123", LANDLORD_EMAIL, "+1234567890", RegistrationRole.LANDLORD);
    registerUser("otherlandlord123", OTHER_LANDLORD_EMAIL, "+1234567891", RegistrationRole.LANDLORD);
    registerUser("tenant123", TENANT_EMAIL, "+1234567892", RegistrationRole.TENANT);

    landlordHeaders = loginAs(LANDLORD_EMAIL);
    otherLandlordHeaders = loginAs(OTHER_LANDLORD_EMAIL);
    tenantHeaders = loginAs(TENANT_EMAIL);
  }

  @AfterEach
  void tearDown() {
    cleanDatabase();
  }

  @Nested
  class CreatePropertyTests {

    @Test
    void shouldCreatePropertyOwnedByCaller_whenRequestIsValid() {
      CreatePropertyDto dto = newPropertyDto("Lakeside Apartment");

      ResponseEntity<ApiResponse<Void>> response = post(PROPERTY_PATH, landlordHeaders, dto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody()).isNotNull();

      List<Property> saved = propertyRepository.findAll();
      assertThat(saved).hasSize(1);
      Property property = saved.getFirst();
      assertThat(property.getLandlordId()).isEqualTo(userIdOf(LANDLORD_EMAIL));
      assertThat(property.getName()).isEqualTo(dto.name());
      assertThat(property.getType()).isEqualTo(dto.type());
      assertThat(property.getAddressLine()).isEqualTo(dto.addressLine());
      assertThat(property.getCity()).isEqualTo(dto.city());
      assertThat(property.getDescription()).isEqualTo(dto.description());
      assertThat(property.getIsArchived()).isFalse();
      assertThat(property.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldReturnForbiddenAndNotPersist_whenUserIsTenant() {
      ResponseEntity<ApiResponse<Void>> response = post(PROPERTY_PATH, tenantHeaders, newPropertyDto("Tenant Attempt"));

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
      assertThat(propertyRepository.count()).isZero();
    }

    @Test
    void shouldReturnUnauthorizedAndNotPersist_whenNotAuthenticated() {
      ResponseEntity<ApiResponse<Void>> response = post(PROPERTY_PATH, getHeaders(), newPropertyDto("Anonymous Attempt"));

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      assertThat(propertyRepository.count()).isZero();
    }
  }

  @Nested
  class GetAllPropertiesTests {

    @Test
    void shouldReturnEmptyList_whenLandlordHasNoProperties() {
      ResponseEntity<ApiResponse<List<Property>>> response = listProperties(landlordHeaders);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().data()).isEmpty();
    }

    @Test
    void shouldReturnOnlyPropertiesOwnedByCaller() {
      Property first = createProperty(landlordHeaders, "Landlord One");
      Property second = createProperty(landlordHeaders, "Landlord Two");
      Property others = createProperty(otherLandlordHeaders, "Other Landlord One");

      List<Property> mine = listProperties(landlordHeaders).getBody().data();
      List<Property> theirs = listProperties(otherLandlordHeaders).getBody().data();

      assertThat(mine).extracting(Property::getId).containsExactlyInAnyOrder(first.getId(), second.getId());
      assertThat(theirs).extracting(Property::getId).containsExactly(others.getId());
    }

    @Test
    void shouldExcludeArchivedProperties() {
      Property active = createProperty(landlordHeaders, "Active");
      Property archived = createProperty(landlordHeaders, "Archived");
      assertThat(deleteProperty(archived.getId().toString(), landlordHeaders).getStatusCode())
          .isEqualTo(HttpStatus.OK);

      List<Property> properties = listProperties(landlordHeaders).getBody().data();

      assertThat(properties).extracting(Property::getId).containsExactly(active.getId());
    }

    @Test
    void shouldReturnForbidden_whenUserIsTenant() {
      assertThat(listProperties(tenantHeaders).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldReturnUnauthorized_whenNotAuthenticated() {
      assertThat(listProperties(getHeaders()).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class GetPropertyByIdTests {

    @Test
    void shouldReturnProperty_whenOwnedByCaller() {
      Property created = createProperty(landlordHeaders, "Lakeside Apartment");

      ResponseEntity<ApiResponse<Property>> response =
          get(PROPERTY_PATH + "/" + created.getId(), landlordHeaders, Property.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().data().getId()).isEqualTo(created.getId());
      assertThat(response.getBody().data().getName()).isEqualTo("Lakeside Apartment");
    }

    @Test
    void shouldReturnNotFound_whenPropertyDoesNotExist() {
      ResponseEntity<ApiResponse<Property>> response =
          get(PROPERTY_PATH + "/" + UUID.randomUUID(), landlordHeaders, Property.class);

      assertThat(response.getStatusCode()).isEqualTo(PropertyException.PROPERTY_NOT_FOUND.getHttpStatus());
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error().message()).isEqualTo(PropertyException.PROPERTY_NOT_FOUND.getMessage());
    }

    // Ownership isolation: the real findByIdAndLandlordId query hides other landlords' properties.
    @Test
    void shouldReturnNotFound_whenPropertyBelongsToAnotherLandlord() {
      Property others = createProperty(otherLandlordHeaders, "Not Yours");

      ResponseEntity<ApiResponse<Property>> response =
          get(PROPERTY_PATH + "/" + others.getId(), landlordHeaders, Property.class);

      assertThat(response.getStatusCode()).isEqualTo(PropertyException.PROPERTY_NOT_FOUND.getHttpStatus());
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error().message()).isEqualTo(PropertyException.PROPERTY_NOT_FOUND.getMessage());
    }

    @Test
    void shouldReturnBadRequest_whenIdIsNotAValidUuid() {
      ResponseEntity<ApiResponse<Property>> response =
          get(PROPERTY_PATH + "/not-a-uuid", landlordHeaders, Property.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnForbidden_whenUserIsTenant() {
      ResponseEntity<ApiResponse<Property>> response =
          get(PROPERTY_PATH + "/" + UUID.randomUUID(), tenantHeaders, Property.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldReturnUnauthorized_whenNotAuthenticated() {
      ResponseEntity<ApiResponse<Property>> response =
          get(PROPERTY_PATH + "/" + UUID.randomUUID(), getHeaders(), Property.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
  }

  @Nested
  class ArchivePropertyTests {

    @Test
    void shouldArchiveProperty_whenOwnedByCaller() {
      Property created = createProperty(landlordHeaders, "To Archive");

      ResponseEntity<ApiResponse<Void>> response = deleteProperty(created.getId().toString(), landlordHeaders);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      // Soft delete: the row still exists, it is only flagged
      assertThat(propertyRepository.findById(created.getId()).orElseThrow().getIsArchived()).isTrue();
      assertThat(listProperties(landlordHeaders).getBody().data()).isEmpty();
    }

    @Test
    void shouldReturnForbidden_whenPropertyIsAlreadyArchived() {
      Property created = createProperty(landlordHeaders, "Archive Twice");
      deleteProperty(created.getId().toString(), landlordHeaders);

      ResponseEntity<ApiResponse<Void>> response = deleteProperty(created.getId().toString(), landlordHeaders);

      assertThat(response.getStatusCode()).isEqualTo(PropertyException.PROPERTY_CANNOT_BE_ARCHIVED.getHttpStatus());
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error().message())
          .isEqualTo(PropertyException.PROPERTY_CANNOT_BE_ARCHIVED.getMessage());
    }

    @Test
    void shouldReturnNotFound_whenPropertyDoesNotExist() {
      ResponseEntity<ApiResponse<Void>> response = deleteProperty(UUID.randomUUID().toString(), landlordHeaders);

      assertThat(response.getStatusCode()).isEqualTo(PropertyException.PROPERTY_NOT_FOUND.getHttpStatus());
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error().message()).isEqualTo(PropertyException.PROPERTY_NOT_FOUND.getMessage());
    }

    @Test
    void shouldReturnNotFoundAndLeavePropertyUntouched_whenPropertyBelongsToAnotherLandlord() {
      Property others = createProperty(otherLandlordHeaders, "Not Yours");

      ResponseEntity<ApiResponse<Void>> response = deleteProperty(others.getId().toString(), landlordHeaders);

      assertThat(response.getStatusCode()).isEqualTo(PropertyException.PROPERTY_NOT_FOUND.getHttpStatus());
      assertThat(propertyRepository.findById(others.getId()).orElseThrow().getIsArchived()).isFalse();
    }

    @Test
    void shouldReturnBadRequest_whenIdIsNotAValidUuid() {
      ResponseEntity<ApiResponse<Void>> response = deleteProperty("not-a-uuid", landlordHeaders);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnForbiddenAndLeavePropertyUntouched_whenUserIsTenant() {
      Property created = createProperty(landlordHeaders, "Tenant Cannot Archive");

      ResponseEntity<ApiResponse<Void>> response = deleteProperty(created.getId().toString(), tenantHeaders);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
      assertThat(propertyRepository.findById(created.getId()).orElseThrow().getIsArchived()).isFalse();
    }

    @Test
    void shouldReturnUnauthorized_whenNotAuthenticated() {
      ResponseEntity<ApiResponse<Void>> response = deleteProperty(UUID.randomUUID().toString(), getHeaders());

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private void cleanDatabase() {
    // landlordId is a plain column (no FK), so properties must be cleared explicitly
    propertyRepository.deleteAll();
    userRepository.deleteAll();
  }

  private void registerUser(String username, String email, String phone, RegistrationRole role) {
    validRegisterUserDto = new RegisterUserDto("Test", "User", username, email, PASSWORD, phone, role);
    assertThat(register(validRegisterUserDto).getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  private HttpHeaders loginAs(String email) {
    validLoginUserDto = new LoginUserDto(email, PASSWORD);
    return getAuthenticatedHeaders();
  }

  private UUID userIdOf(String email) {
    return userRepository.findByEmail(email).orElseThrow().getId();
  }

  private CreatePropertyDto newPropertyDto(String name) {
    return new CreatePropertyDto(
        name,
        PropertyType.values()[0],
        "Lakeside Road 12",
        "Pokhara",
        "Two bedroom apartment near the lake");
  }

  /** Creates a property through the API and returns the persisted entity (create returns no body). */
  private Property createProperty(HttpHeaders headers, String name) {
    assertThat(post(PROPERTY_PATH, headers, newPropertyDto(name)).getStatusCode()).isEqualTo(HttpStatus.CREATED);

    return propertyRepository.findAll().stream()
        .filter(p -> p.getName().equals(name))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Property '" + name + "' was not persisted"));
  }

  private ResponseEntity<ApiResponse<List<Property>>> listProperties(HttpHeaders headers) {
    return restTemplate.exchange(
        PROPERTY_PATH,
        HttpMethod.GET,
        new HttpEntity<>(null, headers),
        new ParameterizedTypeReference<>() {});
  }

  private ResponseEntity<ApiResponse<Void>> deleteProperty(String id, HttpHeaders headers) {
    return restTemplate.exchange(
        PROPERTY_PATH + "/" + id,
        HttpMethod.DELETE,
        new HttpEntity<>(null, headers),
        new ParameterizedTypeReference<>() {});
  }
}