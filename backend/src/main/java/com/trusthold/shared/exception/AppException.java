package com.trusthold.shared.exception;

import com.trusthold.shared.ErrorInfo;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

  private final ErrorInfo errorInfo;
  public AppException(ErrorInfo errorInfo) {
    super(errorInfo.getMessage());
    this.errorInfo = errorInfo;
  }
}
