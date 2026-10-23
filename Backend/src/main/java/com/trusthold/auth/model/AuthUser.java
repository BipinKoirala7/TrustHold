package com.trusthold.auth.model;

import com.trusthold.user.model.Role;

import java.util.UUID;

public record AuthUser(UUID id, String accessToken, Role role) {
}
