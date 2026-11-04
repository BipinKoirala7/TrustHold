package com.trusthold.user.repository;

import com.trusthold.user.model.AccountDeletionToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountDeletionTokenRepository extends JpaRepository<AccountDeletionToken, UUID> {

  Optional<AccountDeletionToken> findByTokenHash(String tokenHash);

  void deleteByUserId(UUID user_id);
}
