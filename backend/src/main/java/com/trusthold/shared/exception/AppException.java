package com.trusthold.shared.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

  private final ExceptionCode code;

  public AppException(ExceptionCode exceptionCode) {
    super(exceptionCode.getMessage());
    this.code = exceptionCode;
  }
}
