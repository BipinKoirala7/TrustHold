package com.trusthold.auth.repository;

import com.trusthold.auth.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

  Optional<PasswordResetToken> findByTokenHash(String token);

  void deleteByUserId(UUID userId);
  void deleteByExpiresAtBefore(Instant instant);
}
