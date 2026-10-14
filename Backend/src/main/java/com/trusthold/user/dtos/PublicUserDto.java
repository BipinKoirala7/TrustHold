package com.trusthold.user.dtos;

import com.trusthold.user.AuthProvider;
import com.trusthold.user.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class PublicUserDto {
  private String firstName;
  private String lastName;
  private String email;
  private Role role;
  private AuthProvider authProvider;
  private Instant lastLogin;
  private Instant createdAt;
  private Instant updatedAt;
}