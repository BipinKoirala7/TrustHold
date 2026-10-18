package com.trusthold.user.exception;

public class UsernameAlreadyExists extends RuntimeException {
  public UsernameAlreadyExists(String message) {
    super(message);
  }
}
