package com.trusthold.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorInfo {

  // Auth
  UNAUTHORIZED                ( "AUTH_UNAUTHORIZED",                HttpStatus.UNAUTHORIZED, "Please Log in"),
  FORBIDDEN                   ( "AUTH_FORBIDDEN",                   HttpStatus.FORBIDDEN,    "You are not allowed to access this resource"),
  EXPIRED_PASSWORD_RESET_TOKEN( "AUTH_RESET_TOKEN_EXPIRED",         HttpStatus.BAD_REQUEST,  "Password reset token has expired"),
  INVALID_PASSWORD_RESET_TOKEN( "AUTH_RESET_TOKEN_INVALID",         HttpStatus.BAD_REQUEST,  "Password reset token is invalid"),
  EMAIL_ALREADY_EXISTS        ( "AUTH_EMAIL_TAKEN",                 HttpStatus.CONFLICT,     "Email is already registered"),
  USERNAME_ALREADY_EXISTS     ( "AUTH_USERNAME_TAKEN",              HttpStatus.CONFLICT,     "Username is already taken"),
  INVALID_CREDENTIALS         ( "AUTH_INVALID_CREDENTIALS",         HttpStatus.UNAUTHORIZED, "Invalid credentials"),
  INVALID_ROLE_FOR_REGISTRATION("AUTH_REGISTRATION_ROLE_INVALID",   HttpStatus.BAD_REQUEST,  "Role not allowed for registration"),
  INVALID_TOKEN               ( "AUTH_INVALID_TOKEN",               HttpStatus.UNAUTHORIZED, "Please Log in"),
  USER_NOT_FOUND              ( "USER_NOT_FOUND",                   HttpStatus.NOT_FOUND,    "Account not found"),

  // User
  DELETION_TOKEN_INVALID(       "DELETION_TOKEN_INVALID",           HttpStatus.BAD_REQUEST,  "This deletion link is invalid or has already been used."),
  DELETION_TOKEN_EXPIRED(       "DELETION_TOKEN_EXPIRED",           HttpStatus.GONE,         "This deletion link has expired. Please request a new one."),
  DELETION_BLOCKED_ACTIVE_LEASE("DELETION_BLOCKED_ACTIVE_LEASE",    HttpStatus.CONFLICT,     "You can't delete your account while you have unsettled leases.");

  private final String errCode;
  private final HttpStatus httpStatus;
  private final String message;
}
