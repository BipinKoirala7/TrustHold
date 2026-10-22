package com.trusthold.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginOAuthUserDto {

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be a valid email address")
  private String email;

  @NotBlank(message = "Provider ID is required")
  private String providerId;
}
