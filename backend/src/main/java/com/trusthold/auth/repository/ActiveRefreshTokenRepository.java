package com.trusthold.auth.repository;

import com.trusthold.auth.model.ActiveRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface ActiveRefreshTokenRepository extends JpaRepository<ActiveRefreshToken, UUID> {

  boolean existsByRefreshTokenAndExpiresAtAfter(String token, Instant expiresAt);

  void deleteActiveRefreshTokenByUserId(UUID userId);
}
