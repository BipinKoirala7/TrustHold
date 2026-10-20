package com.trusthold.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByUsername(String username);
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);
  boolean existsByUsername(String username);

  @Transactional
  @Modifying
  @Query("Update User u Set u.lastLogin = :lastLoginTime Where u.id = :id")
  void updateUserLastLoginTime(@Param("id") UUID userId, @Param("lastLoginTime") Instant lastLoginTime);
}
