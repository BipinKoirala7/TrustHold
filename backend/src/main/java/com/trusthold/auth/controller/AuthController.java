package com.trusthold.auth.controller;

import com.trusthold.auth.dtos.GoogleRegistrationRequest;
import com.trusthold.auth.exception.InValidRoleForRegistrationException;
import com.trusthold.auth.model.AuthUser;
import com.trusthold.auth.service.AuthService;
import com.trusthold.auth.dtos.LoginUserDto;
import com.trusthold.shared.dtos.ApiResponse;
import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.user.model.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Handles the auth-related requests.
 * <p>
 *   Request Path: /api/v1/auth
 * </p>
 *
 * @see AuthService
 */
@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
  private final AuthService authService;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<Void> registerLocalUser(@RequestBody @Valid RegisterUserDto registerUserDto) {
    authService.registerLocalUser(registerUserDto);
    return ApiResponse.success("User registered successfully");
  }

  @PostMapping("/oauth/register")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<Void> beginGoogleRegistration(
      @RequestBody GoogleRegistrationRequest body,
      HttpServletRequest request
  ) {
    Role role = body.role();

    if (role != Role.TENANT && role != Role.LANDLORD) {
      throw new InValidRoleForRegistrationException("Role is missing or invalid in the request");
    }

    request.getSession(true).setAttribute("registrationRole", role.toString());
    return ApiResponse.success("Role recorded for google registration");
  }

  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<Void> loginUser(@RequestBody @Valid  LoginUserDto loginUserDto, HttpServletResponse response) {
    authService.loginUser(loginUserDto, response);
    return ApiResponse.success("logged in successfully");
  }

  @PostMapping("/token/refresh")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<Void> refreshToken(HttpServletRequest request, HttpServletResponse response){
    authService.refreshToken(request, response);
    return ApiResponse.success("Token refreshed successfully");
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<Void> logout(HttpServletResponse response, @AuthenticationPrincipal @Valid AuthUser authUser){
    authService.logoutUser(response, authUser);
    return ApiResponse.success("Logged Out Successfully");
  }
}
