package com.trusthold.user.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

public record UpdateUserDto (
  @NotNull
  @Size(min = 3, max = 50, message = "First name must be between 3 and 50 characters")
  String firstName,

  @NotNull
  @Size(min = 3, max = 50, message = "Last name must be between 3 and 50 characters")
  String lastName,

  @NotNull
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  String username,

  @NotBlank
  @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Invalid phone number format")
  String phone
){}
