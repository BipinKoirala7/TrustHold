package com.trusthold.auth.dtos;

import com.trusthold.auth.RegistrationRole;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserDto {

  @NotBlank
  @Size(min = 3, max = 50)
  private String firstName;

  @NotBlank
  @Size(min = 3, max = 50)
  private String lastName;

  @NotBlank
  @Size(min = 3, max = 50)
  private String username;

  @NotBlank
  @Email
  private String email;

  @NotBlank
  @Size(min = 8, max = 100)
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
      message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character."
  )
  private String password;

  @NotNull
  private RegistrationRole registrationRole;
}
