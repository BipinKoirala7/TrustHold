package com.trusthold.user.service;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.user.dtos.LandlordDashboardSummaryDto;
import com.trusthold.user.repository.DashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class LandlordService {
  private final DashboardRepository dashboardRepository;

  /**
   * Fetched landlord dashboard stats summary
   *
   * @param authUser current authenticated user
   * @return the landlord dashboard stats summary
   */
  public LandlordDashboardSummaryDto getLandlordDashboardStatsSummary(AuthUser authUser) {
    ZoneId zoneId = ZoneId.of("Asia/Kathmandu");
    YearMonth yearMonth = YearMonth.now(zoneId);
    Instant startOfMonth = yearMonth.atDay(1).atStartOfDay(zoneId).toInstant();
    Instant endOfMonth = yearMonth.atEndOfMonth().atStartOfDay(zoneId).toInstant();

    return dashboardRepository.getLeaseSummary(authUser.id(), startOfMonth, endOfMonth);
  }
}
