package com.trusthold.auth.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
public class PasswordResetToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull(message = "User ID cannot be null")
  @Column(name = "user_id")
  private UUID userId;

  @NotBlank(message = "Token cannot be blank")
  @Column(name = "token_hash")
  private String tokenHash;

  @NotNull(message = "Expires at cannot be null")
  @Column(name = "expires_at")
  private Instant expiresAt;

  @CreationTimestamp
  @Column(name = "created_at")
  private Instant createdAt;
}
