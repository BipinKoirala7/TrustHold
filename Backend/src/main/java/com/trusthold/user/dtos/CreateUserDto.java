package com.trusthold.user.dtos;

import com.trusthold.user.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserDto {

  @NotNull
  @Max(value = 50)
  @Min(value = 3)
  private String firstName;

  @NotNull
  @Max(value = 50)
  @Min(value = 3)
  private String lastName;

  @NotNull
  @Email
  private String email;

  @Min(value = 8)
  @Max(value = 100)
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
      message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character."
  )
  private String password;

  @NotNull
  private Role role;
}
