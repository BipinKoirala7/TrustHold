package com.trusthold.user.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateUserDto {
  @NotNull
  @Size(min = 3, max = 50)
  private String firstName;

  @NotNull
  @Size(min = 3, max = 50)
  private String lastName;
}
