package com.trusthold.mail.dtos;

import java.time.Instant;
import java.util.UUID;

/**
 * Carries the RAW token only in memory to the mail listener.
 * Never persist or log this record.
 */

public record AccountDeletionEvent(
    UUID userId,
    String email,
    String userName,
    String rawToken,
    Instant expiresAt
) {
}
