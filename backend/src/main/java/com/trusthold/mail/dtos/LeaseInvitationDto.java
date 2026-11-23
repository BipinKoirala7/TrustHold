package com.trusthold.mail.dtos;

public record LeaseInvitationDto(
    String landlordName,
    String propertyName,
    String propertyAddress,
    String depositAmount,
    long expiryHours
) {
}
