package com.trusthold.mail.events;

public record LeaseJoinedEvent(
    String landlordEmail,
    String landlordName,
    String tenantName,
    String tenantEmail,
    String propertyName,
    String propertyAddress,
    String depositAmount
) {
}