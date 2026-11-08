package com.trusthold.settlement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "settlement_proposals",
    indexes = {
        @Index(name = "idx_sp_lease", columnList = "lease_id"),
        @Index(name = "idx_sp_lease_status", columnList = "lease_id, status")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class SettlementProposal {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull(message = "Lease is required")
  @Column(name = "lease_id", nullable = false, updatable = false)
  private UUID leaseId;

  @NotNull(message = "Proposer is required")
  @Column(name = "proposed_by", nullable = false, updatable = false)
  private UUID proposedBy;

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

  @Column(name = "tenant_agreed_at")
  private Instant tenantAgreedAt;

  @Column(name = "landlord_agreed_at")
  private Instant landlordAgreedAt;

  @NotNull(message = "Status is required")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SettlementProposalStatus status;

  @Version
  private Long version;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}