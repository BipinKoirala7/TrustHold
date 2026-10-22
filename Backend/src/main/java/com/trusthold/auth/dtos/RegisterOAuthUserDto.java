package com.trusthold.auth.dtos;

import com.trusthold.auth.model.RegistrationRole;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterOAuthUserDto {

  @NotBlank(message = "Provider ID is required")
  private String providerId;

  @NotBlank(message = "First name is required")
  @Size(min = 3, max = 50, message = "First name must be between 3 and 50 characters")
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(min = 3, max = 50, message = "Last name must be between 3 and 50 characters")
  private String lastName;

  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  private String username;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be a valid email address")
  private String email;

  @NotNull(message = "User role is required")
  private RegistrationRole registrationRole;
}
