package com.trusthold.lease.controller;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.lease.dtos.CreateLeaseDto;
import com.trusthold.lease.service.LeaseService;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.controller.ControllerTestSupport;
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.model.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeaseController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LeaseControllerTests extends ControllerTestSupport {

  private static final String BASE_URL = "/lease";

  @MockitoBean
  private LeaseService leaseService;

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
        UUID.randomUUID(),
        new BigDecimal("25000.50"),
        new BigDecimal("50000.00"),
        startDate,
        startDate.plus(365, ChronoUnit.DAYS)
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

    @Test
    void shouldReturnMalformedRequest_whenBodyIsMalformedJson() throws Exception {
      // Act & Assert
      mockMvc.perform(post(BASE_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content("{ not-json"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));

      verifyNoInteractions(leaseService);
    }

    @Test
    void shouldReturnUnsupportedMediaType_whenContentTypeIsNotJson() throws Exception {
      // Act & Assert
      mockMvc.perform(post(BASE_URL)
              .contentType(MediaType.TEXT_PLAIN)
              .content(objectMapper.writeValueAsString(createLeaseDto)))
          .andExpect(status().isUnsupportedMediaType())
          .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_MEDIA_TYPE"));

      verifyNoInteractions(leaseService);
    }

    @ParameterizedTest
    @EnumSource(ErrorInfo.class)
    void shouldReturnErrorInfoResponse_whenServiceThrowsAppException(ErrorInfo errorInfo) throws Exception {
      // Arrange
      doThrow(new AppException(errorInfo))
          .when(leaseService).createLease(eq(sampleAuthUser), any(CreateLeaseDto.class));

      // Act & Assert
      mockMvc.perform(post(BASE_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(createLeaseDto)))
          .andExpect(status().is(errorInfo.getHttpStatus().value()))
          .andExpect(jsonPath("$.error.code").value(errorInfo.getErrCode()))
          .andExpect(jsonPath("$.error.message").value(errorInfo.getMessage()));
    }

    @Test
    void shouldReturnInternalServerError_whenServiceThrowsUnexpectedException() throws Exception {
      // Arrange
      doThrow(new RuntimeException("Database connection failed"))
          .when(leaseService).createLease(eq(sampleAuthUser), any(CreateLeaseDto.class));

      // Act & Assert
      mockMvc.perform(post(BASE_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(createLeaseDto)))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.error.code").value("INTERNAL_SERVER_ERROR"))
          .andExpect(jsonPath("$.error.message").value(
              "An unexpected error occurred. Please try again later."));
    }
  }
}