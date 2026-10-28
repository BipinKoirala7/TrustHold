package com.trusthold.auth.dtos;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordDto(@NotBlank String rawToken, @NotBlank String newPassword) {
}
