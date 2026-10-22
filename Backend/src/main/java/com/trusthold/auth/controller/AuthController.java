package com.trusthold.auth.controller;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.auth.service.AuthService;
import com.trusthold.auth.dtos.LoginUserDto;
import com.trusthold.shared.dtos.ApiResponse;
import com.trusthold.auth.dtos.RegisterUserDto;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
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
  public ApiResponse<Void> loginUser(@Valid @RequestBody LoginUserDto loginUserDto, HttpServletResponse response) {
    authService.loginUser(loginUserDto, response);
    return ApiResponse.success("logged in successfully");
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<Void> logout(HttpServletResponse response, @AuthenticationPrincipal AuthUser authUser){
    authService.logoutUser(response, authUser);
    return ApiResponse.success("Logged Out Successfully");
  }
}
