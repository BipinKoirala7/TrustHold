package com.trusthold.shared.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trusthold.shared.ErrorCode;

import java.time.Instant;

/**
 * ApiResponse is a record that represents a response from the server.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(T data, ApiError error, Instant timestamp) {

  public static <T> ApiResponse<T> success(){return new ApiResponse<>(null, null, Instant.now());}
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(data, null, Instant.now());
  }

  public static <T> ApiResponse<T> error(String code, String message) {
    return new ApiResponse<>(null, new ApiError(code, message), Instant.now());
  }

  public static <T> ApiResponse<T> error(ErrorCode errorCode){
    return new ApiResponse<>(null, new ApiError(errorCode.getErrCode(), errorCode.getMessage()), Instant.now());
  }
}

