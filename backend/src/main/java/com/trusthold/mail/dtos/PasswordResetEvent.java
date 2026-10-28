package com.trusthold.mail.dtos;

public record PasswordResetEvent(String to, String firstName, String rawToken) {
}
