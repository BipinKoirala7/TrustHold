package com.trusthold.shared.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum UserException implements ExceptionCode {

  USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "Account not found"),
  DELETION_TOKEN_INVALID("DELETION_TOKEN_INVALID", HttpStatus.BAD_REQUEST, "This deletion link is invalid or has already been used."),
  DELETION_TOKEN_EXPIRED("DELETION_TOKEN_EXPIRED", HttpStatus.GONE, "This deletion link has expired. Please request a new one."),
  DELETION_BLOCKED_ACTIVE_LEASE("DELETION_BLOCKED_ACTIVE_LEASE", HttpStatus.CONFLICT, "You can't delete your account while you have unsettled leases.");

  private final String errCode;
  private final HttpStatus httpStatus;
  private final String message;
}
