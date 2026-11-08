package com.trusthold.settlement.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateSettlementProposalRequest(

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.00", message = "Refund amount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Refund amount must have at most 2 decimal places")
    BigDecimal refundToTenant,

    @NotNull(message = "Deduction amount is required")
    @DecimalMin(value = "0.00", message = "Deduction amount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Deduction amount must have at most 2 decimal places")
    BigDecimal deductionAmount,

    @Size(max = 1000, message = "Deduction reason must be at most 1000 characters")
    String deductionReason
) {
  @JsonIgnore
  @AssertTrue(message = "A reason is required when a deduction is claimed")
  public boolean isDeductionReasonProvided() {
    if (deductionAmount == null) return true;
    return deductionAmount.signum() == 0 || (deductionReason != null && !deductionReason.isBlank());
  }
}