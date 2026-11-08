package com.trusthold.settlement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "settlements",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_settlement_lease", columnNames = "lease_id"),
        @UniqueConstraint(name = "uk_settlement_proposal", columnNames = "proposal_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Settlement {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull(message = "Lease is required")
  @Column(name = "lease_id", nullable = false, updatable = false)
  private UUID leaseId;

  @NotNull(message = "Agreed proposal is required")
  @Column(name = "proposal_id", nullable = false, updatable = false)
  private UUID proposalId;

  @NotNull(message = "Deposit amount is required")
  @DecimalMin(value = "0.01", message = "Deposit amount must be greater than zero")
  @Digits(integer = 10, fraction = 2, message = "Deposit amount must have at most 2 decimal places")
  @Column(name = "deposit_amount", nullable = false, updatable = false, precision = 12, scale = 2)
  private BigDecimal depositAmount;

  @NotNull(message = "Refund amount is required")
  @DecimalMin(value = "0.00", message = "Refund amount cannot be negative")
  @Digits(integer = 10, fraction = 2, message = "Refund amount must have at most 2 decimal places")
  @Column(name = "refund_to_tenant", nullable = false, updatable = false, precision = 12, scale = 2)
  private BigDecimal refundToTenant;

  @NotNull(message = "Deduction amount is required")
  @DecimalMin(value = "0.00", message = "Deduction amount cannot be negative")
  @Digits(integer = 10, fraction = 2, message = "Deduction amount must have at most 2 decimal places")
  @Column(name = "deduction_amount", nullable = false, updatable = false, precision = 12, scale = 2)
  private BigDecimal deductionAmount;

  @Size(max = 1000, message = "Deduction reason must be at most 1000 characters")
  @Column(name = "deduction_reason", length = 1000, updatable = false)
  private String deductionReason;

  @NotNull(message = "Agreement date is required")
  @Column(name = "agreed_on", nullable = false, updatable = false)
  private Instant agreedOn;

  @Column(name = "closed_by")
  private UUID closedBy;

  @Column(name = "closed_at")
  private Instant closedAt;

  @Version
  private Long version;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}