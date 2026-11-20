package com.trusthold.shared.exception;

import org.springframework.http.HttpStatus;

public interface ExceptionCode {
  String getErrCode();
  HttpStatus getHttpStatus();
  String getMessage();
}
