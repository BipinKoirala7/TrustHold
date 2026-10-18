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
public class GlobalExceptionHandler {

  @ExceptionHandler(EmailAlreadyExists.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiResponse<Void> handleEmailAlreadyExists(EmailAlreadyExists ex) {
    log.warn("Email already exists", ex);
    return ApiResponse.error("Email already exists");
  }

  @ExceptionHandler(UsernameAlreadyExists.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiResponse<Void> handleUsernameAlreadyExists(UsernameAlreadyExists ex) {
    log.warn("Username already exists", ex);
    return ApiResponse.error("Username already exists");
  }
}
