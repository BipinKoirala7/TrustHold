package com.trusthold.shared.exception;

import com.trusthold.shared.ErrorCode;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

  private final ErrorCode errorCode;

  public AppException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
