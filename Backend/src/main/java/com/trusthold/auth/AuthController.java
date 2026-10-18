package com.trusthold.auth;

import com.trusthold.shared.dtos.ApiResponse;
import com.trusthold.user.dtos.RegisterUserDto;
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
  public ApiResponse<Void> registerLocalUser(@Valid @RequestBody RegisterUserDto user) {
    authService.registerLocalUser(user);
    return ApiResponse.success("User registered successfully");
  }
}
