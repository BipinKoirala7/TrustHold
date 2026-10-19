package com.trusthold.auth;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "active_refresh_token")
@Setter
@Getter
public class ActiveRefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull
  private UUID userId;

  @Column(name = "refresh_token")
  private String refreshToken;

  @Column(name = "created_at")
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(name = "expires_at")
  @CreationTimestamp
  private LocalDateTime expiresAt;
}
