package com.trusthold.auth;

import com.trusthold.auth.dtos.LoginUserDto;
import com.trusthold.shared.dtos.ApiResponse;
import com.trusthold.auth.dtos.RegisterUserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
  private final AuthService authService;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<Void> registerLocalUser(@Valid @RequestBody RegisterUserDto registerUserDto) {
    authService.registerLocalUser(registerUserDto);
    return ApiResponse.success("User registered successfully");
  }

  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<Void> loginUser(@Valid @RequestBody LoginUserDto loginUserDto) {
    authService.loginUser(loginUserDto);
    return ApiResponse.success("User logged in successfully");
  }
}
