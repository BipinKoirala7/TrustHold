package com.trusthold.auth.controller;

import com.trusthold.auth.dtos.*;
import com.trusthold.auth.model.AuthUser;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.exception.AppException;
import com.trusthold.auth.service.AuthService;
import com.trusthold.auth.service.PasswordResetTokenService;
import com.trusthold.shared.dtos.ApiResponse;
import com.trusthold.user.model.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
  private final PasswordResetTokenService passwordResetTokenService;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<Void> registerLocalUser(@RequestBody @Valid RegisterUserDto registerUserDto) {
    authService.registerLocalUser(registerUserDto);
    return ApiResponse.success(null);
  }

  @PostMapping("/oauth/register")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<Void> beginGoogleRegistration(
      @RequestBody @Valid @NotNull GoogleRegistrationRequest body,
      HttpServletRequest request
  ) {
    Role role = body.role();

    if (role != Role.TENANT && role != Role.LANDLORD) {
      throw new AppException(ErrorInfo.INVALID_ROLE_FOR_REGISTRATION);
    }

    request.getSession(true).setAttribute("registrationRole", role.toString());
    return ApiResponse.success(null);
  }

  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<Void> loginUser(@RequestBody @Valid  LoginUserDto loginUserDto, HttpServletResponse response) {
    authService.loginUser(loginUserDto, response);
    return ApiResponse.success(null);
  }

  @PostMapping("/token/refresh")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<Void> refreshToken(HttpServletRequest request, HttpServletResponse response){
    authService.refreshToken(request, response);
    return ApiResponse.success(null);
  }

  @PostMapping("/forgot-password")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<Void> forgotPassword(
      @RequestBody @Valid ForgotPasswordDto forgotPasswordDto
  ){
    passwordResetTokenService.forgotPassword(forgotPasswordDto);
    return ApiResponse.success(null);
  }

  @PostMapping("/reset-password")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<Void> resetPassword(
      HttpServletResponse response,
      @RequestBody @Valid ResetPasswordDto resetPasswordDto
  ){
    passwordResetTokenService.resetPassword(response, resetPasswordDto);
    return ApiResponse.success(null);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<Void> logout(HttpServletResponse response, @AuthenticationPrincipal @Valid AuthUser authUser){
    authService.logoutUser(response, authUser);
    return ApiResponse.success(null);
  }
}
