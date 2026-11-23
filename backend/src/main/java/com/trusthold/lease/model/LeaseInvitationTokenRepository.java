package com.trusthold.lease.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LeaseInvitationTokenRepository extends JpaRepository<LeaseInvitationToken, UUID> {

  Optional<LeaseInvitationToken> findByTokenHash(String tokenHash);

  void deleteByLeaseId(UUID leaseId);
}
