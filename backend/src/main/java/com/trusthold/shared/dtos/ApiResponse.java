package com.trusthold.shared.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trusthold.shared.ErrorInfo;

import java.time.Instant;

/**
 * ApiResponse is a record that represents a response from the server.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(T data, ApiError error, Instant timestamp) {

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(data, null, Instant.now());
  }

  public static <T> ApiResponse<T> error(String code, String message) {
    return new ApiResponse<>(null, new ApiError(code, message), Instant.now());
  }

  public static <T> ApiResponse<T> error(ErrorInfo errorInfo){
    return new ApiResponse<>(null, new ApiError(errorInfo.getErrCode(), errorInfo.getMessage()), Instant.now());
  }
}

