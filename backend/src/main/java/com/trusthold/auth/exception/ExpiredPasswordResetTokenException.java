package com.trusthold.auth.exception;

public class ExpiredPasswordResetTokenException extends RuntimeException {
  public ExpiredPasswordResetTokenException(String message) {
    super(message);
  }
}
