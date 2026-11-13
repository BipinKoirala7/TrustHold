package com.trusthold.lease.controller;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.lease.dtos.CreateLeaseDto;
import com.trusthold.lease.service.LeaseActivityService;
import com.trusthold.lease.service.LeaseService;
import com.trusthold.shared.controller.ControllerTestSupport;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
  }
}