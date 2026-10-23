package com.trusthold.auth.exception;

/**
 * Custom exception class for incorrect credentials. (Passwords & Emails).
 */
public class IncorrectCredentialsException extends RuntimeException {
  public IncorrectCredentialsException(String message) {
    super(message);
  }
}
