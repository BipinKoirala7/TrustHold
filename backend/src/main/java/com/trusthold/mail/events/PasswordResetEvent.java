package com.trusthold.mail.events;

public record PasswordResetEvent(String to, String firstName, String rawToken) {
}
