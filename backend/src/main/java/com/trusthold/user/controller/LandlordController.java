package com.trusthold.user.controller;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.user.dtos.LandlordDashboardSummaryDto;
import com.trusthold.shared.dtos.ApiResponse;
import com.trusthold.user.service.LandlordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/landlord")
@RequiredArgsConstructor
public class LandlordController {

  private final LandlordService landlordService;

  @GetMapping("stats")
  public ApiResponse<LandlordDashboardSummaryDto> getLandlordStats(@AuthenticationPrincipal AuthUser authUser) {
    return ApiResponse.success(landlordService.getLandlordDashboardStatsSummary(authUser));
  }
}
