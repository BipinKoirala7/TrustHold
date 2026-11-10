package com.trusthold.lease.controller;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.lease.dtos.CreateLeaseDto;
import com.trusthold.lease.model.Lease;
import com.trusthold.lease.model.LeaseActivity;
import com.trusthold.lease.service.LeaseActivityService;
import com.trusthold.lease.service.LeaseService;
import com.trusthold.shared.dtos.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/lease")
@RequiredArgsConstructor
@Validated
public class LeaseController {

  private final LeaseService leaseService;
  private final LeaseActivityService leaseActivityService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<Void> createLease(@AuthenticationPrincipal AuthUser authUser, @RequestBody @Valid CreateLeaseDto createLeaseDto) {
    leaseService.createLease(authUser, createLeaseDto);
    return ApiResponse.success();
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<List<Lease>> getLeases(@AuthenticationPrincipal AuthUser authUser) {
    return ApiResponse.success(leaseService.getUsersLeases(authUser));
  }

  @GetMapping("{leaseId}/activity")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<List<LeaseActivity>> getLeases(@PathVariable UUID leaseId) {
    return ApiResponse.success(leaseActivityService.getLeaseActivity(leaseId));
  }
}
