package com.trusthold.lease.dtos;

public record LandlordDashboardStatsSummaryDto(
    int activeLeases, int moveOutThisMonth,
    int totalAmountHeldInEscrow, int numberOfDeposits,
    int totalAmountAwaitingPayment, int numberOfAwaitingPayments,
    int numbOfAwaitingMyAgreement) {
}
