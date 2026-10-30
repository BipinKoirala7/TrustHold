package com.trusthold.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorInfo {

  // Auth
  UNAUTHORIZED                ( "AUTH_UNAUTHORIZED",                HttpStatus.UNAUTHORIZED, "Please Log in"),
  EXPIRED_PASSWORD_RESET_TOKEN( "AUTH_RESET_TOKEN_EXPIRED",         HttpStatus.BAD_REQUEST,  "Password reset token has expired"),
  INVALID_PASSWORD_RESET_TOKEN( "AUTH_RESET_TOKEN_INVALID",         HttpStatus.BAD_REQUEST,  "Password reset token is invalid"),
  EMAIL_ALREADY_EXISTS        ( "AUTH_EMAIL_TAKEN",                 HttpStatus.CONFLICT,     "Email is already registered"),
  USERNAME_ALREADY_EXISTS     ( "AUTH_USERNAME_TAKEN",              HttpStatus.CONFLICT,     "Username is already taken"),
  INVALID_CREDENTIALS         ( "AUTH_INVALID_CREDENTIALS",         HttpStatus.UNAUTHORIZED, "Invalid credentials"),
  INVALID_ROLE_FOR_REGISTRATION("AUTH_REGISTRATION_ROLE_INVALID",   HttpStatus.BAD_REQUEST,  "Role not allowed for registration"),
  INVALID_TOKEN               ( "AUTH_INVALID_TOKEN",               HttpStatus.UNAUTHORIZED, "Please Log in"),
  USER_NOT_FOUND              ( "USER_NOT_FOUND",                   HttpStatus.NOT_FOUND,    "Account not found");

  private final String errCode;
  private final HttpStatus httpStatus;
  private final String message;
}
