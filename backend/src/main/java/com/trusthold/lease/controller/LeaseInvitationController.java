package com.trusthold.lease.controller;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.lease.dtos.AcceptLeaseInvitationDto;
import com.trusthold.lease.service.LeaseInvitationService;
import com.trusthold.shared.dtos.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("lease/invitation")
@RequiredArgsConstructor
@Validated
public class LeaseInvitationController {
  private final LeaseInvitationService leaseInvitationService;

  @PostMapping("accept")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<Void> acceptInvitation(@AuthenticationPrincipal AuthUser authUser, @RequestBody @Valid AcceptLeaseInvitationDto acceptLeaseInvitationDto) {
    leaseInvitationService.acceptInvitation(authUser, acceptLeaseInvitationDto);
    return ApiResponse.success();
  }
}
