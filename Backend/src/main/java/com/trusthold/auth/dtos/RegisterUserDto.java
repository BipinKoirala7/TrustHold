package com.trusthold.auth.dtos;

import com.trusthold.auth.RegistrationRole;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserDto {

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

  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
      message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character."
  )
  private String password;

  @NotNull(message = "User role is required")
  private RegistrationRole registrationRole;
}
