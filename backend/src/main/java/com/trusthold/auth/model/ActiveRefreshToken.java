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
  @NotNull
  @Column(name = "user_id", nullable = false, unique = true)
  private UUID userId;

  @Column(name = "refresh_token", columnDefinition = "TEXT")
  private String refreshToken;

  @Column(name = "created_at")
  @CreationTimestamp
  private Instant createdAt;

  @NotNull
  @Column(name = "expires_at")
  private Instant expiresAt;
}
