package com.trusthold.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordDto(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    String email
) {
}
