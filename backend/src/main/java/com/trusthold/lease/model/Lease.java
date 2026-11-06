package com.trusthold.lease.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "leases",
    indexes = {
        @Index(name = "idx_lease_property", columnList = "property_id"),
        @Index(name = "idx_lease_tenant", columnList = "tenant_id"),
        @Index(name = "idx_lease_status", columnList = "status"),
        @Index(name = "idx_lease_landlord", columnList = "landlord_id")
    }
)
@Getter
@Setter
public class Lease {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  // Created so to avoid lock failures
  @Version
  private Long version;

  @NotNull(message = "Property is required")
  @Column(name = "property_id", nullable = false, updatable = false)
  private UUID propertyId;

  @NotNull(message = "Landlord is required")
  @Column(name = "landlord_id", nullable = false, updatable = false)
  private UUID landlordId;

  @NotNull(message = "Tenant is required")
  @Column(name = "tenant_id", nullable = false, updatable = false)
  private UUID tenantId;

  @NotNull(message = "Monthly rent is required")
  @Positive(message = "Monthly rent must be greater than zero")
  @Digits(integer = 10, fraction = 2, message = "Monthly rent can have at most 10 digits and 2 decimal places")
  @Column(name = "monthly_rent", nullable = false, precision = 12, scale = 2)
  private BigDecimal monthlyRent;

  @NotNull(message = "Deposit amount is required")
  @Positive(message = "Deposit amount must be greater than zero")
  @Digits(integer = 10, fraction = 2, message = "Deposit amount can have at most 10 digits and 2 decimal places")
  @Column(name = "deposit_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal depositAmount;

  @NotNull(message = "Start date is required")
  @Column(name = "start_date", nullable = false)
  private Instant startDate;

  @NotNull(message = "End date is required")
  @Column(name = "end_date", nullable = false)
  private Instant endDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private LeaseStatus status = LeaseStatus.PENDING_MOVE_IN;

  @Column(name = "moved_in_at")
  private Instant movedInAt;

  @Column(name = "move_out_initiated_at")
  private Instant moveOutInitiatedAt;

  // userId of the user who initiated move out
  @Column(name = "move_out_initiated_by")
  private UUID moveOutInitiatedBy;

  @Column(name = "moved_out_at")
  private Instant movedOutAt;

  @Column(name = "is_settled", nullable = false)
  private boolean settled = false;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @SuppressWarnings("unused")
  @AssertTrue(message = "End date must be after the start date")
  private boolean isEndDateAfterStartDate() {
    return startDate == null || endDate == null || endDate.isAfter(startDate);
  }
}
