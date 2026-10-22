package com.trusthold.user.dtos;

import com.trusthold.user.model.AuthProvider;
import com.trusthold.user.model.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
public class PublicUserDto {
  private UUID id;
  private String firstName;
  private String lastName;
  private String username;
  private String email;
  private Role role;
  private AuthProvider authProvider;
  private Instant lastLogin;
  private Instant createdAt;
  private Instant updatedAt;
}