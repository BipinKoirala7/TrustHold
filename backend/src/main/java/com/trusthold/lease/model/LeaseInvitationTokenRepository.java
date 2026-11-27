package com.trusthold.lease.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LeaseInvitationTokenRepository extends JpaRepository<LeaseInvitationToken, UUID> {

  @Query("SELECT token FROM LeaseInvitationToken token WHERE token.tokenHash = :tokenHash AND token.expiresAt > CURRENT_TIMESTAMP")
  Optional<LeaseInvitationToken> findNonExpiredInvitation(@Param("tokenHash") String tokenHash);

  void deleteByLeaseId(UUID leaseId);
}
