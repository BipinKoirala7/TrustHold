package com.trusthold.lease.controller;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.lease.dtos.CreateLeaseDto;
import com.trusthold.lease.dtos.LeaseSummaryDto;
import com.trusthold.lease.model.LeaseStatus;
import com.trusthold.lease.service.LeaseActivityService;
import com.trusthold.lease.service.LeaseService;
import com.trusthold.property.dtos.PropertySummaryDto;
import com.trusthold.shared.exception.AuthException;
import com.trusthold.shared.exception.PropertyException;
import com.trusthold.shared.controller.ControllerTestSupport;
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.model.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeaseController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LeaseControllerTests extends ControllerTestSupport {

  private static final String BASE_URL = "/lease";

  @MockitoBean
  private LeaseService leaseService;

  @MockitoBean
  private LeaseActivityService leaseActivityService;

  private AuthUser sampleAuthUser;
  private CreateLeaseDto createLeaseDto;

  @BeforeEach
  void setUp() {
    sampleAuthUser = new AuthUser(UUID.randomUUID(), "dummy-token", Role.LANDLORD);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            sampleAuthUser, null, List.of(new SimpleGrantedAuthority("ROLE_LANDLORD"))));

    Instant startDate = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS);
    createLeaseDto = new CreateLeaseDto(
        UUID.randomUUID(),
        "john.doe@example.com",
        new BigDecimal("25000.50"),
        new BigDecimal("50000.00"),
        startDate
    );
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Nested
  class CreateLeaseTests {

    @Test
    void shouldReturnCreated_whenRequestIsValid() throws Exception {
      // Act & Assert
      mockMvc.perform(post(BASE_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(createLeaseDto)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.timestamp").exists())
          .andExpect(jsonPath("$.data").doesNotExist())
          .andExpect(jsonPath("$.error").doesNotExist());
    }
  }

  @Nested
  class GetLeasesTests {

    @Test
    void shouldReturnOk_withLeaseSummaries_whenUserHasLeases() throws Exception {
      // Arrange
      UUID leaseId = UUID.randomUUID();
      Instant startDate = Instant.now().truncatedTo(ChronoUnit.MILLIS);
      Instant endDate = startDate.plus(1, ChronoUnit.MILLIS);
      LeaseSummaryDto summary = new LeaseSummaryDto(
          leaseId,
          new PropertySummaryDto("Sunrise Apartment", null, "Pokhara"),
          "John Doe",
          new BigDecimal("50000.00"),
          LeaseStatus.ACTIVE,
          startDate,
          endDate);
      when(leaseService.getUsersLeases(sampleAuthUser)).thenReturn(List.of(summary));

      // Act & Assert
      mockMvc.perform(get(BASE_URL))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.timestamp").exists())
          .andExpect(jsonPath("$.error").doesNotExist())
          .andExpect(jsonPath("$.data", hasSize(1)))
          .andExpect(jsonPath("$.data[0].id").value(leaseId.toString()))
          .andExpect(jsonPath("$.data[0].propertyInfo.name").value("Sunrise Apartment"))
          .andExpect(jsonPath("$.data[0].propertyInfo.city").value("Pokhara"))
          .andExpect(jsonPath("$.data[0].tenantName").value("John Doe"))
          .andExpect(jsonPath("$.data[0].depositAmount").value(50000.0))
          .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
          .andExpect(jsonPath("$.data[0].startDate").exists());

      verify(leaseService).getUsersLeases(sampleAuthUser);
    }

    @Test
    void shouldReturnOk_withEmptyList_whenUserHasNoLeases() throws Exception {
      // Arrange
      when(leaseService.getUsersLeases(sampleAuthUser)).thenReturn(List.of());

      // Act & Assert
      mockMvc.perform(get(BASE_URL))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.timestamp").exists())
          .andExpect(jsonPath("$.error").doesNotExist())
          .andExpect(jsonPath("$.data", hasSize(0)))
          .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void shouldReturnTenantNameAsNull_whenTenantIsMissing() throws Exception {
      // Arrange
      LeaseSummaryDto summary = new LeaseSummaryDto(
          UUID.randomUUID(),
          new PropertySummaryDto("Sunrise Apartment", null, "Pokhara"),
          null,
          new BigDecimal("50000.00"),
          LeaseStatus.ACTIVE,
          Instant.now(),
          Instant.now().plus(1, ChronoUnit.MILLIS));
      when(leaseService.getUsersLeases(sampleAuthUser)).thenReturn(List.of(summary));

      // Act & Assert
      mockMvc.perform(get(BASE_URL))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[0].tenantName").value(nullValue()));
    }

    @Test
    void shouldReturnUnauthorizedError_whenServiceThrowsUnauthorized() throws Exception {
      // Arrange
      when(leaseService.getUsersLeases(sampleAuthUser))
          .thenThrow(new AppException(AuthException.UNAUTHORIZED));

      // Act & Assert
      mockMvc.perform(get(BASE_URL))
          .andExpect(status().is(AuthException.UNAUTHORIZED.getHttpStatus().value()))
          .andExpect(jsonPath("$.error.code").value(AuthException.UNAUTHORIZED.getErrCode()))
          .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void shouldReturnPropertyNotFoundError_whenServiceThrowsPropertyNotFound() throws Exception {
      // Arrange
      when(leaseService.getUsersLeases(sampleAuthUser))
          .thenThrow(new AppException(PropertyException.PROPERTY_NOT_FOUND));

      // Act & Assert
      mockMvc.perform(get(BASE_URL))
          .andExpect(status().is(PropertyException.PROPERTY_NOT_FOUND.getHttpStatus().value()))
          .andExpect(jsonPath("$.error.code").value(PropertyException.PROPERTY_NOT_FOUND.getErrCode()));
    }
  }
}