package com.trusthold.auth.dtos;

import com.trusthold.auth.model.RegistrationRole;
import jakarta.validation.constraints.NotNull;

public record GoogleRegistrationRequest(@NotNull(message = "Role is required") RegistrationRole role) { }
