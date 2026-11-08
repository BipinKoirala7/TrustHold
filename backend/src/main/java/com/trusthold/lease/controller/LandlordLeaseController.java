package com.trusthold.lease.controller;

import com.trusthold.shared.dtos.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lease/landlord")
@RequiredArgsConstructor
public class LandlordLeaseController {

  @GetMapping
  public ApiResponse<Void> getLandlordLeases() {
    return ApiResponse.success();
  }
}
