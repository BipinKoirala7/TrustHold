package com.trusthold.lease.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
        @Index(name = "idx_lease_status", columnList = "status")
    }
)
@Getter
@Setter
public class Lease {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull(message = "Property ID is required")
  @Column(name = "property_id", nullable = false, updatable = false)
  private UUID propertyId;

  @NotNull(message = "Tenant ID is required")
  @Column(name = "tenant_id", nullable = false, updatable = false)
  private UUID tenantId;

  @NotNull(message = "Monthly rent cannot be null")
  @Column(name = "monthly_rent", nullable = false, precision = 12, scale = 2)
  private BigDecimal monthlyRent;

  @NotNull(message = "Deposit amount cannot be null")
  @Column(name = "deposit_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal depositAmount;

  @NotNull(message = "Start date cannot be null")
  @Column(name = "start_date", nullable = false)
  private Instant startDate;

  @NotNull(message = "End date cannot be null")
  @Column(name = "end_date", nullable = false)
  private Instant endDate;

  @NotNull(message = "Lease Status cannot be null")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private LeaseStatus status = LeaseStatus.PENDING_MOVE_IN;

  @NotNull(message = "Moved in date cannot be null")
  @Column(name = "moved_in_at")
  private Instant movedInAt;

  @NotNull(message = "Move out initiated date cannot be null")
  @Column(name = "move_out_initiated_at")
  private Instant moveOutInitiatedAt;

  @NotNull(message = "Move out initiated by cannot be null")
  @Column(name = "move_out_initiated_by")
  private UUID moveOutInitiatedBy;  // userId of the user who initiated move out

  @Column(name = "moved_out_at")
  private Instant movedOutAt;

  @NotNull(message = "Settled cannot be null")
  @Column(name = "is_settled", nullable = false)
  private boolean settled = false;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
