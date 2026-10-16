package com.trusthold.user.dtos;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserDto {

  @NotNull
  @Size(min = 3, max = 50)
  private String firstName;

  @NotNull
  @Size(min = 3, max = 50)
  private String lastName;

  @NotNull
  @Size(min = 3, max = 50)
  private String username;

  @NotNull
  @Email
  private String email;

  @Size(min = 8, max = 100)
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
      message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character."
  )
  private String password;
}
