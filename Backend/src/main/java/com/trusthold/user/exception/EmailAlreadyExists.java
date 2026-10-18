package com.trusthold.user.exception;

public class EmailAlreadyExists extends RuntimeException {
  public EmailAlreadyExists(String message) {
    super(message);
  }
}
