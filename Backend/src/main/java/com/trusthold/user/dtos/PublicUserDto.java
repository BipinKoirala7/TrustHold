package com.trusthold.user.dtos;

import com.trusthold.user.model.AuthProvider;
import com.trusthold.user.model.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

public record PublicUserDto(
    UUID id,
    String firstName,
    String lastName,
    String username,
    String email,
    Role role,
    AuthProvider authProvider,
    Instant lastLogin,
    Instant createdAt,
    Instant updatedAt) {}