package com.trusthold.lease.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lease_invitation_tokens")
@Getter
@Setter
public class LeaseInvitationToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotBlank(message = "Tenant email cannot be null")
  @Email(message = "Tenant email must be a valid email address")
  private String tenantEmail;

  @NotNull(message = "Lease cannot be null")
  @Column(name = "lease_id", unique = true)
  private UUID leaseId;

  @NotBlank(message = "Token cannot be blank")
  @Column(name = "token_hash", unique = true)
  private String tokenHash;

  @NotNull(message = "Expires at cannot be null")
  @Column(name = "expires_at")
  private Instant expiresAt;

  @CreationTimestamp
  @Column(name = "created_at")
  private Instant createdAt;

}
