package com.trusthold.auth.dtos;

import com.trusthold.user.model.Role;
import jakarta.validation.constraints.NotNull;

public record GoogleRegistrationRequest(@NotNull(message = "Role is required") Role role) { }
