package com.trusthold.auth.exception;

import com.trusthold.shared.dtos.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class AuthExceptionHandler {

  @ExceptionHandler(ExpiredPasswordResetTokenException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> handleExpiredPasswordResetToken(ExpiredPasswordResetTokenException ex) {
    log.warn("Expired password reset token", ex);
    return ApiResponse.error(ex.getMessage());
  }

  @ExceptionHandler(InValidPasswordResetTokenException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> handleInvalidPasswordResetToken(InValidPasswordResetTokenException ex) {
    log.warn("Invalid password reset token", ex);
    return ApiResponse.error(ex.getMessage());
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiResponse<Void> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
    log.warn("Email already exists", ex);
    return ApiResponse.error(ex.getMessage());
  }

  @ExceptionHandler(UsernameAlreadyExistsException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiResponse<Void> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
    log.warn("Username already exists", ex);
    return ApiResponse.error(ex.getMessage());
  }

  @ExceptionHandler(IncorrectCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ApiResponse<Void> handleIncorrectCredentials(IncorrectCredentialsException ex) {
    log.warn("Incorrect credentials", ex);
    return ApiResponse.error("Invalid email or password!");
  }

  @ExceptionHandler(UserNotFoundException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ApiResponse<Void> handleUserNotFound(UserNotFoundException ex) {
    log.warn("User not found", ex);
    return ApiResponse.error("Invalid email or password!");
  }
}
