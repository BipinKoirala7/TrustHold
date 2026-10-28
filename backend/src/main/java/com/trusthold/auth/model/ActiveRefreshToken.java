package com.trusthold.auth.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "active_refresh_token")
@Setter
@Getter
public class ActiveRefreshToken {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "refresh_token", columnDefinition = "TEXT", nullable = false)
  private String refreshToken;

  @Column(name = "created_at", nullable = false)
  @CreationTimestamp
  private Instant createdAt;

  @NotNull(message = "Expires at is required")
  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;
}
