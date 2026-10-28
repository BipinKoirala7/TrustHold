package com.trusthold.auth.dtos;

import jakarta.validation.constraints.Email;

public record ForgotPasswordDto(@Email(message = "Invalid email address") String email) {
}
