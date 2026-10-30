package com.trusthold.auth.repository;

import com.trusthold.auth.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

  Optional<PasswordResetToken> findByTokenHash(String token);

  int deleteByUserId(UUID userId);
  void deleteByExpiresAtBefore(Instant instant);

  @Modifying(clearAutomatically = true)
  @Query("delete from PasswordResetToken t where t.tokenHash = :hash and t.expiresAt > :now")
  int consume(@Param("hash") String hash, @Param("now") Instant now);
}
