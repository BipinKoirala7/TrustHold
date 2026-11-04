package com.trusthold.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_deletion_tokens", indexes = {
    @Index(name = "idx_deletion_token_hash", columnList = "tokenHash", unique = true)
})
@Getter
@Setter
public class AccountDeletionToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull(message = "User ID cannot be null")
  @Column(name = "user_id")
  private UUID userId;

  @NotBlank(message = "Token hash cannot be null")
  @Column(nullable = false, unique = true, length = 128)
  private String tokenHash;

  @NotNull(message = "Expires at cannot be null")
  @Column(nullable = false)
  private Instant expiresAt;

  @NotNull(message = "Used at cannot be null")
  @Column(nullable = false)
  private Instant usedAt;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;
}
