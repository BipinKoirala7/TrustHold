package com.trusthold.shared;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
  String getErrCode();
  HttpStatus getHttpStatus();
  String getMessage();
}
