package com.trusthold.auth.exception;

public class InValidPasswordResetTokenException extends RuntimeException {
  public InValidPasswordResetTokenException(String message) {
    super(message);
  }
}
