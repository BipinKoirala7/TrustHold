package com.trusthold.user.dtos;

import java.math.BigDecimal;

public record LandlordDashboardSummaryDto (
  long activeLeases,
  long moveOutThisMonth,
  BigDecimal totalAmountHeldInEscrow,
  long escrowDepositCount,
  long totalAmountAwaitingPayment,
  BigDecimal totalPendingAmount,
  long numbOfAwaitingMyAgreement
){}