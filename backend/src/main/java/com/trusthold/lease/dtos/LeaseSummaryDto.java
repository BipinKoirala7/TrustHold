package com.trusthold.lease.dtos;

import com.trusthold.lease.model.Lease;
import com.trusthold.lease.model.LeaseStatus;
import com.trusthold.property.dtos.PropertySummaryDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LeaseSummaryDto(
    UUID id,
    PropertySummaryDto propertyInfo,
    String tenantName,
    BigDecimal depositAmount,
    LeaseStatus status,
    Instant startDate,
    Instant endDate
) {

  public LeaseSummaryDto(Lease lease, PropertySummaryDto propertyInfo, String tenantName) {
    this(lease.getId(), propertyInfo, tenantName, lease.getDepositAmount(), lease.getStatus(), lease.getStartDate(), lease.getEndDate());
  }
}
