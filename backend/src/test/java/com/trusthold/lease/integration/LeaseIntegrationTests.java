package com.trusthold.lease.integration;

import com.trusthold.auth.dtos.LoginUserDto;
import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.lease.dtos.CreateLeaseDto;
import com.trusthold.lease.dtos.LeaseSummaryDto;
import com.trusthold.lease.repository.LeaseRepository;
import com.trusthold.property.dtos.CreatePropertyDto;
import com.trusthold.property.model.Property;
import com.trusthold.property.model.PropertyType;
import com.trusthold.property.repository.PropertyRepository;
import com.trusthold.shared.dtos.ApiResponse;
import com.trusthold.shared.integration.IntegrationTestSupport;
import com.trusthold.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class LeaseIntegrationTests extends IntegrationTestSupport {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PropertyRepository propertyRepository;

  @Autowired
  private LeaseRepository leaseRepository;

  @Nested
  class GetLeasesTests {

    private static final String LEASES_URL = "/lease";
    private static final String PROPERTY_URL = "/property"; // ASSUMPTION: adjust to your PropertyController mapping
    private static final String PASSWORD = "Password123!";

    private LoginUserDto landlordLogin;
    private LoginUserDto tenantLogin;
    private HttpHeaders landlordHeaders;
    private HttpHeaders tenantHeaders;
    private String tenantEmail;

    @BeforeEach
    void setUp() {
      cleanDatabase();

      landlordLogin = registerUser("Landlord", "One", "landlord1", "landlord1@example.com",
          "+9779800000001", RegistrationRole.LANDLORD);
      tenantLogin = registerUser("Tenant", "Person", "tenant1", "tenant1@example.com",
          "+9779800000002", RegistrationRole.TENANT);

      landlordHeaders = authHeaders(landlordLogin);
      tenantHeaders = authHeaders(tenantLogin);
      tenantEmail = tenantLogin.email();
    }

    @AfterEach
    void tearDown() {
      cleanDatabase();
    }

    @Test
    void shouldReturnUnauthorized_whenRequestHasNoAuthCookies() {
      ResponseEntity<ApiResponse<LeaseSummaryDto[]>> response = getLeases(getHeaders());

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnEmptyList_whenLandlordHasNoLeases() {
      ResponseEntity<ApiResponse<LeaseSummaryDto[]>> response = getLeases(landlordHeaders);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error()).isNull();
      assertThat(response.getBody().data()).isEmpty();
    }

    @Test
    void shouldReturnEmptyList_whenTenantHasNoLeases() {
      ResponseEntity<ApiResponse<LeaseSummaryDto[]>> response = getLeases(tenantHeaders);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().data()).isEmpty();
    }

    @Test
    void shouldReturnLeaseWithPropertyAndTenantName_whenLandlordHasLease() {
      UUID propertyId = createProperty(landlordHeaders, userIdOf(landlordLogin), "Sunrise Apartment");
      createLease(landlordHeaders, propertyId, tenantEmail);

      ResponseEntity<ApiResponse<LeaseSummaryDto[]>> response = getLeases(landlordHeaders);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      LeaseSummaryDto[] leases = response.getBody().data();
      assertThat(leases).hasSize(1);

      LeaseSummaryDto lease = leases[0];
      assertThat(lease.id()).isNotNull();
      assertThat(lease.propertySummary().name()).isEqualTo("Sunrise Apartment");
      assertThat(lease.propertySummary().city()).isEqualTo("Pokhara");
      assertThat(lease.tenantName()).isEqualTo("Tenant Person");
      assertThat(lease.depositAmount()).isNotNull();
      assertThat(lease.status()).isNotNull();
      assertThat(lease.startDate()).isNotNull();
    }

    @Test
    void shouldReturnLease_whenUserIsTheTenantOfTheLease() {
      UUID propertyId = createProperty(landlordHeaders, userIdOf(landlordLogin), "Sunrise Apartment");
      createLease(landlordHeaders, propertyId, tenantEmail);

      ResponseEntity<ApiResponse<LeaseSummaryDto[]>> response = getLeases(tenantHeaders);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().data()).hasSize(1);
      assertThat(response.getBody().data()[0].propertySummary().name()).isEqualTo("Sunrise Apartment");
      assertThat(response.getBody().data()[0].tenantName()).isEqualTo("Tenant Person");
    }

    @Test
    void shouldNotReturnLeasesOfOtherLandlords() {
      UUID propertyId = createProperty(landlordHeaders, userIdOf(landlordLogin), "Sunrise Apartment");
      createLease(landlordHeaders, propertyId, tenantEmail);

      LoginUserDto otherLandlordLogin = registerUser("Landlord", "Two", "landlord2",
          "landlord2@example.com", "+9779800000003", RegistrationRole.LANDLORD);

      ResponseEntity<ApiResponse<LeaseSummaryDto[]>> response = getLeases(authHeaders(otherLandlordLogin));

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().data()).isEmpty();
    }

    @Test
    void shouldNotReturnLeasesOfOtherTenants() {
      UUID propertyId = createProperty(landlordHeaders, userIdOf(landlordLogin), "Sunrise Apartment");
      createLease(landlordHeaders, propertyId, tenantEmail);

      LoginUserDto otherTenantLogin = registerUser("Tenant", "Other", "tenant2",
          "tenant2@example.com", "+9779800000004", RegistrationRole.TENANT);

      ResponseEntity<ApiResponse<LeaseSummaryDto[]>> response = getLeases(authHeaders(otherTenantLogin));

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().data()).isEmpty();
    }

    @Test
    void shouldReturnAllLeases_whenLandlordHasMultipleLeases() {
      UUID landlordId = userIdOf(landlordLogin);
      UUID firstPropertyId = createProperty(landlordHeaders, landlordId, "Sunrise Apartment");
      UUID secondPropertyId = createProperty(landlordHeaders, landlordId, "Lakeside House");
      createLease(landlordHeaders, firstPropertyId, tenantEmail);
      createLease(landlordHeaders, secondPropertyId, tenantEmail);

      ResponseEntity<ApiResponse<LeaseSummaryDto[]>> response = getLeases(landlordHeaders);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().data())
          .extracting(lease -> lease.propertySummary().name())
          .containsExactlyInAnyOrder("Sunrise Apartment", "Lakeside House");
    }

    // ---------- helpers ----------

    private LoginUserDto registerUser(String firstName, String lastName, String username,
                                      String email, String phone, RegistrationRole role) {
      RegisterUserDto dto = new RegisterUserDto(
          firstName, lastName, username, email, PASSWORD, phone, role);
      assertThat(register(dto).getStatusCode()).isEqualTo(HttpStatus.CREATED);
      return new LoginUserDto(email, PASSWORD);
    }

    // getAuthenticatedHeaders() reads validLoginUserDto, so point it at the user we want.
    private HttpHeaders authHeaders(LoginUserDto loginDto) {
      validLoginUserDto = loginDto;
      return getAuthenticatedHeaders();
    }

    private String tenantEmailOf(LoginUserDto loginDto) {
      return loginDto.email();
    }

    private UUID userIdOf(LoginUserDto loginDto) {
      return userRepository.findByEmail(loginDto.email()).orElseThrow().getId();
    }

    private UUID createProperty(HttpHeaders headers, UUID landlordId, String name) {
      CreatePropertyDto dto = new CreatePropertyDto(
          name,
          PropertyType.values()[0], // ASSUMPTION: any valid PropertyType works
          "Lakeside Road 12",
          "Pokhara",
          "A nice place to live");
      assertThat(post(PROPERTY_URL, headers, dto).getStatusCode()).isEqualTo(HttpStatus.CREATED);

      return propertyRepository.findByLandlordIdAndIsArchivedFalse(landlordId).stream()
          .filter(property -> name.equals(property.getName()))
          .map(Property::getId)
          .findFirst()
          .orElseThrow();
    }

    private ResponseEntity<ApiResponse<LeaseSummaryDto[]>> getLeases(HttpHeaders headers) {
      return get(LEASES_URL, headers, LeaseSummaryDto[].class);
    }

    private void createLease(HttpHeaders headers, UUID propertyId, String tenantEmail) {
      Instant startDate = Instant.now().plus(1, ChronoUnit.DAYS);
      CreateLeaseDto dto = new CreateLeaseDto(
          propertyId,
          tenantEmail,
          new BigDecimal("25000.50"),   // ASSUMPTION: rent, matching your controller test order
          new BigDecimal("50000.00"),   // ASSUMPTION: deposit
          startDate);
      assertThat(post(LEASES_URL, headers, dto).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private void cleanDatabase() {
      // If lease activity rows have a FK to lease, delete them first
      // (e.g., leaseActivityRepository.deleteAll()).
      leaseRepository.deleteAll();
      propertyRepository.deleteAll();
      userRepository.deleteAll();
    }
  }
}
