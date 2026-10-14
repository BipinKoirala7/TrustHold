package com.trusthold.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_user_email", columnNames = "email"))
@Getter
@Setter
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  // Optional field to store the provider ID for users authenticated via oauth providers (e.g., Google).
  @Column(name = "provider_id")
  private String providerId;

  @NotNull
  @Max(value = 50)
  @Min(value = 3)
  @Column(nullable = false)
  private String firstName;

  @NotNull
  @Max(value = 50)
  @Min(value = 3)
  @Column(nullable = false)
  private String lastName;

  @NotNull
  @Email
  @Column(nullable = false, unique = true)
  private String email;

  // Password field is required for users registered with LOCAL auth provider.
  // For users registered via oauth providers (e.g., Google), this field can be null.
  // Hash password are stored in this field for security reasons.
  @NotNull
  @Column(nullable = false)
  private String password;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "auth_provider", nullable = false)
  private AuthProvider authProvider = AuthProvider.LOCAL;

  @NotNull
  @Column(name = "last_login", nullable = false)
  private Instant lastLogin;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  @UpdateTimestamp
  private Instant updatedAt;
}
