package com.trusthold.user.service;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.user.dtos.LandlordDashboardSummaryDto;
import com.trusthold.user.repository.DashboardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LandlordServiceTests {

  private static final ZoneId KATHMANDU = ZoneId.of("Asia/Kathmandu");

  @Mock
  private DashboardRepository dashboardRepository;

  @InjectMocks
  private LandlordService landlordService;

  private UUID landlordId;
  private AuthUser authUser;
  private LandlordDashboardSummaryDto summary;

  @BeforeEach
  void setUp() {
    landlordId = UUID.randomUUID();
    authUser = new AuthUser(landlordId, "access-token", null);
    summary = new LandlordDashboardSummaryDto(
        5L,
        2L,
        new BigDecimal("150000.00"),
        4L,
        3L,
        new BigDecimal("45000.00"),
        1L
    );
  }

  private void stubRepository() {
    when(dashboardRepository.getLeaseSummary(eq(landlordId), any(Instant.class), any(Instant.class)))
        .thenReturn(summary);
  }

  @Test
  void shouldReturnRepositorySummary_whenLandlordRequestsDashboardStats() {
    stubRepository();

    LandlordDashboardSummaryDto result = landlordService.getLandlordDashboardStatsSummary(authUser);

    assertThat(result).isSameAs(summary);
    assertThat(result.activeLeases()).isEqualTo(5L);
    assertThat(result.moveOutThisMonth()).isEqualTo(2L);
    assertThat(result.totalAmountHeldInEscrow()).isEqualByComparingTo("150000.00");
    assertThat(result.escrowDepositCount()).isEqualTo(4L);
    assertThat(result.totalAmountAwaitingPayment()).isEqualTo(3L);
    assertThat(result.totalPendingAmount()).isEqualByComparingTo("45000.00");
    assertThat(result.numbOfAwaitingMyAgreement()).isEqualTo(1L);
  }

  @Test
  void shouldQueryRepositoryWithAuthenticatedLandlordId_whenFetchingDashboardStats() {
    stubRepository();

    landlordService.getLandlordDashboardStatsSummary(authUser);

    verify(dashboardRepository).getLeaseSummary(eq(landlordId), any(Instant.class), any(Instant.class));
    verifyNoMoreInteractions(dashboardRepository);
  }

  @Test
  void shouldPassCurrentMonthBoundariesInKathmanduTime_whenFetchingDashboardStats() {
    stubRepository();

    // Capture the month before and after the call so the test can't flake at a month rollover
    YearMonth before = YearMonth.now(KATHMANDU);
    landlordService.getLandlordDashboardStatsSummary(authUser);
    YearMonth after = YearMonth.now(KATHMANDU);

    ArgumentCaptor<Instant> startCaptor = ArgumentCaptor.forClass(Instant.class);
    ArgumentCaptor<Instant> endCaptor = ArgumentCaptor.forClass(Instant.class);
    verify(dashboardRepository).getLeaseSummary(eq(landlordId), startCaptor.capture(), endCaptor.capture());

    YearMonth used = before.equals(after)
        ? before
        : YearMonth.from(startCaptor.getValue().atZone(KATHMANDU));
    Instant expectedStart = used.atDay(1).atStartOfDay(KATHMANDU).toInstant();
    Instant expectedEnd = used.atEndOfMonth().atStartOfDay(KATHMANDU).toInstant();

    assertThat(startCaptor.getValue()).isEqualTo(expectedStart);
    assertThat(endCaptor.getValue()).isEqualTo(expectedEnd);
  }

  @Test
  void shouldStartRangeAtMidnightOnFirstOfMonth_whenFetchingDashboardStats() {
    stubRepository();

    landlordService.getLandlordDashboardStatsSummary(authUser);

    ArgumentCaptor<Instant> startCaptor = ArgumentCaptor.forClass(Instant.class);
    verify(dashboardRepository).getLeaseSummary(eq(landlordId), startCaptor.capture(), any(Instant.class));

    var start = startCaptor.getValue().atZone(KATHMANDU);
    assertThat(start.getDayOfMonth()).isEqualTo(1);
    assertThat(start.toLocalTime()).isEqualTo(LocalTime.MIDNIGHT);
  }

  @Test
  void shouldUseStartBeforeEnd_whenFetchingDashboardStats() {
    stubRepository();

    landlordService.getLandlordDashboardStatsSummary(authUser);

    ArgumentCaptor<Instant> startCaptor = ArgumentCaptor.forClass(Instant.class);
    ArgumentCaptor<Instant> endCaptor = ArgumentCaptor.forClass(Instant.class);
    verify(dashboardRepository).getLeaseSummary(eq(landlordId), startCaptor.capture(), endCaptor.capture());

    assertThat(startCaptor.getValue()).isBefore(endCaptor.getValue());
  }
}