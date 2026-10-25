package com.trusthold.user.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

public record UpdateUserDto (
  @NotNull
  @Size(min = 3, max = 50)
  String firstName,

  @NotNull
  @Size(min = 3, max = 50)
  String lastName,

  @NotNull
  @Size(min = 3, max = 50)
  String username
){}
