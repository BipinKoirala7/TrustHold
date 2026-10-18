package com.trusthold;

import com.trusthold.shared.dtos.ApiResponse;
import com.trusthold.user.dtos.CreateUserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

  private final UserService userService;

  @PostMapping("/")
  @ResponseStatus(code = HttpStatus.CREATED)
  public ApiResponse<Void> createUser(@RequestBody @Valid CreateUserDto createUserDto) {
    userService.createUser(createUserDto);
    return ApiResponse.success( null, "User created successfully");
  }
}
