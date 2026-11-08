package com.trusthold.lease.dtos;

public record LandlordStatsGridDto(
    ActiveLeases activeLeases,
    HeldInEscrow heldInEscrow,
    AwaitingPayment awaitingPayment,
    AwaitingMyAgreement awaitingMyAgreement) {
}
