package com.trusthold.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ActiveRefreshTokenRepository extends JpaRepository<ActiveRefreshToken, UUID> {

  Optional<ActiveRefreshToken> findByUserIdAndRefreshToken(UUID userId, String refreshToken);
}
