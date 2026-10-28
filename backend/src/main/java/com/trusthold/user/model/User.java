package com.trusthold.user.model;

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

  @NotBlank(message = "First name is required")
  @Size(min = 3, max = 50, message = "First name must be between 3 and 50 characters")
  @Column(nullable = false, length = 50)
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(min = 3, max = 50, message = "Last name must be between 3 and 50 characters")
  @Column(nullable = false, length = 50)
  private String lastName;

  // Username would mostly be used for unique searching user without
  // having to type the whole email address.
  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  @Pattern(
      regexp = "^[a-z0-9_-]+$",
      message = "Username can contain only lowercase letters, numbers, underscores, and hyphens"
  )
  @Column(nullable = false, length = 50)
  private String username;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be a valid email address")
  @Column(nullable = false)
  private String email;

  // A password field is required for users registered with a LOCAL auth provider.
  // For users registered via oauth providers (e.g., Google), this field can be null.
  // Hash passwords are stored in this field for security reasons.
  private String password;

  @NotNull(message = "User role is required")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @NotNull(message = "Auth provider is required")
  @Enumerated(EnumType.STRING)
  @Column(name = "auth_provider", nullable = false)
  private AuthProvider authProvider = AuthProvider.LOCAL;

  // When in development, we would log in the user automatically after registration.
  // This field is used to track the last login time of the user.
//  @NotNull(message = "Last login time is required")
  @Column(name = "last_login")
  private Instant lastLogin;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  @UpdateTimestamp
  private Instant updatedAt;
}
