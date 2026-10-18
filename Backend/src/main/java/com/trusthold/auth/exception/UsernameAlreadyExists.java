package com.trusthold.auth.exception;

public class UsernameAlreadyExists extends RuntimeException {
  public UsernameAlreadyExists(String message) {
    super(message);
  }
}
