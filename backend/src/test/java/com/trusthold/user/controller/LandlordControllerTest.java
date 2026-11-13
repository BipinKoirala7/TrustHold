package com.trusthold.user.controller;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.controller.ControllerTestSupport;
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.dtos.LandlordDashboardSummaryDto;
import com.trusthold.user.model.Role;
import com.trusthold.user.service.LandlordService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LandlordController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LandlordControllerTest extends ControllerTestSupport {

  private static final String STATS_ENDPOINT = "/landlord/stats";

  @MockitoBean
  private LandlordService landlordService;

  private AuthUser authUser;

  @BeforeEach
  void setUp() {
    authUser = new AuthUser(UUID.randomUUID(), "token", Role.LANDLORD);
    // Filters are disabled, so @AuthenticationPrincipal is resolved from the SecurityContextHolder directly.
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(authUser, null, List.of(new SimpleGrantedAuthority("ROLE_LANDLORD"))));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Nested
  class GetLandlordStatsTests{
    @Test
    void shouldReturnOkWithWrappedSummary_whenRequestIsSuccessful() throws Exception {
      when(landlordService.getLandlordDashboardStatsSummary(authUser)).thenReturn(
          new LandlordDashboardSummaryDto(3L, 1L, new BigDecimal("1500.50"), 2L, 1L, new BigDecimal("300.00"), 4L));

      mockMvc.perform(get(STATS_ENDPOINT))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.activeLeases").value(3))
          .andExpect(jsonPath("$.data.moveOutThisMonth").value(1))
          .andExpect(jsonPath("$.data.totalAmountHeldInEscrow").value(1500.5))
          .andExpect(jsonPath("$.data.escrowDepositCount").value(2))
          .andExpect(jsonPath("$.data.totalAmountAwaitingPayment").value(1))
          .andExpect(jsonPath("$.data.totalPendingAmount").value(300.0))
          .andExpect(jsonPath("$.data.numbOfAwaitingMyAgreement").value(4))
          .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void shouldPassAuthenticatedUserToService() throws Exception {
      when(landlordService.getLandlordDashboardStatsSummary(authUser)).thenReturn(
          new LandlordDashboardSummaryDto(0L, 0L, BigDecimal.ZERO, 0L, 0L, BigDecimal.ZERO, 0L));

      mockMvc.perform(get(STATS_ENDPOINT)).andExpect(status().isOk());

      verify(landlordService).getLandlordDashboardStatsSummary(authUser);
    }

    @Test
    void shouldReturnMethodNotAllowed_whenUsingPost() throws Exception {
      mockMvc.perform(post(STATS_ENDPOINT))
          .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void shouldMapAppExceptionFromService_toItsHttpStatus() throws Exception {
      when(landlordService.getLandlordDashboardStatsSummary(authUser))
          .thenThrow(new AppException(ErrorInfo.USER_NOT_FOUND));

      mockMvc.perform(get(STATS_ENDPOINT))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"));
    }
  }
}
