package com.trusthold.auth.exception;

public class EmailAlreadyExists extends RuntimeException {
  public EmailAlreadyExists(String message) {
    super(message);
  }
}
