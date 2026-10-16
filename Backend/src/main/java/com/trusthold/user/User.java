package com.trusthold.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_user_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_user_provider_id", columnNames = "provider_id")
    })
@Getter
@Setter
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  // Optional field to store the provider ID (sub) for users authenticated via oauth providers (e.g., Google).
  @Column(name = "provider_id")
  private String providerId;

  @NotNull
  @Size(min = 3, max = 50)
  @Column(nullable = false, length = 50)
  private String firstName;

  @NotNull
  @Size(min = 3, max = 50)
  @Column(nullable = false, length = 50)
  private String lastName;

  // Username would mostly be used for unique searching user without
  // having to type the whole email address.
  @NotNull
  @Size(min = 3, max = 50)
  @Column(nullable = false, length = 50)
  private String username;

  @NotNull
  @Email
  @Column(nullable = false)
  private String email;

  // Password field is required for users registered with LOCAL auth provider.
  // For users registered via oauth providers (e.g., Google), this field can be null.
  // Hash password are stored in this field for security reasons.
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

  @NotNull
  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private Instant createdAt;

  @NotNull
  @Column(name = "updated_at", nullable = false)
  @UpdateTimestamp
  private Instant updatedAt;
}
