package com.trusthold.user.integration;

import com.trusthold.auth.dtos.LoginUserDto;
import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.shared.dtos.ApiResponse;
import com.trusthold.shared.integration.IntegrationTestSupport;
import com.trusthold.user.dtos.LandlordDashboardSummaryDto;
import com.trusthold.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class LandlordIntegrationTests extends IntegrationTestSupport {

  private final String username = "landlord123";
  private final String email = "landlord@example.com";
  private final String password = "Password123!";

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();

    String firstName = "Bipin";
    String lastName = "Koirala";
    String phone = "+1234567890";

    validRegisterUserDto = new RegisterUserDto(firstName, lastName, username, email, password, phone, RegistrationRole.LANDLORD);
    validLoginUserDto = new LoginUserDto(email, password);

    assertThat(register(validRegisterUserDto).getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
  }

  @Nested
  class GetLandlordStatsTests {

    private final String GET_LANDLORD_STATS = "/landlord/stats";

    @Test
    void shouldReturnZeroedStats_whenLandlordHasNoLeases() {
      ResponseEntity<ApiResponse<LandlordDashboardSummaryDto>> response =
          get(GET_LANDLORD_STATS, getAuthenticatedHeaders(), LandlordDashboardSummaryDto.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().data()).isNotNull();

      LandlordDashboardSummaryDto stats = response.getBody().data();
      assertThat(stats.activeLeases()).isZero();
      assertThat(stats.moveOutThisMonth()).isZero();
      assertThat(stats.totalAmountHeldInEscrow()).isEqualByComparingTo("0");
      assertThat(stats.escrowDepositCount()).isZero();
      assertThat(stats.totalAmountAwaitingPayment()).isZero();
      assertThat(stats.totalPendingAmount()).isEqualByComparingTo("0");
      assertThat(stats.numbOfAwaitingMyAgreement()).isZero();
    }

    @Test
    void shouldReturnUnauthorized_whenRequestIsNotAuthenticated() {
      ResponseEntity<ApiResponse<LandlordDashboardSummaryDto>> response =
          get(GET_LANDLORD_STATS, getHeaders(), LandlordDashboardSummaryDto.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().data()).isNull();
      assertThat(response.getBody().error()).isNotNull();
    }

    @Test
    void shouldReturnForbidden_whenUserIsNotLandlord() {
      String tenantEmail = "tenant@example.com";
      RegisterUserDto tenantDto = new RegisterUserDto(
          "Tenant", "User", "tenant123", tenantEmail, password, "+1987654321", RegistrationRole.TENANT);
      assertThat(register(tenantDto).getStatusCode()).isEqualTo(HttpStatus.CREATED);

      // getAuthenticatedHeaders() logs in using this field, so this switches the caller to the tenant
      validLoginUserDto = new LoginUserDto(tenantEmail, password);

      ResponseEntity<ApiResponse<LandlordDashboardSummaryDto>> response =
          get(GET_LANDLORD_STATS, getAuthenticatedHeaders(), LandlordDashboardSummaryDto.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().data()).isNull();
      assertThat(response.getBody().error()).isNotNull();
    }
  }
}