package com.trusthold.lease.service;

import com.trusthold.lease.repository.LeaseRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Validated
@Slf4j
public class LeaseHelper {

  private final LeaseRepository leaseRepository;

  @PreAuthorize("hasRole('TENANT')")
  public void addTenantToLease(@NotNull UUID leaseId, @NotNull UUID tenantId) {
    log.info("Adding tenant={} to lease={}", tenantId, leaseId);
    leaseRepository.updateTenantId(leaseId, tenantId);
  }
}
