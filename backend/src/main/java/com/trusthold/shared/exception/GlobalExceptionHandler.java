package com.trusthold.shared.exception;

import com.trusthold.shared.dtos.ApiError;
import com.trusthold.shared.dtos.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  // App Exception
  @ExceptionHandler(AppException.class)
  public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
    log.debug("App exception", ex);
    return ResponseEntity
        .status(ex.getErrorInfo().getHttpStatus())
        .body(ApiResponse.error(ex.getErrorInfo().getErrCode(), ex.getMessage()));
  }

  // Validation Exceptions
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new LinkedHashMap<>();
    ex.getBindingResult().getFieldErrors()
        .forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));

    String message = String.join("; ", errors.values());

    log.warn("Validation errors: {}", errors);
    return ApiResponse.error("VALIDATION_ERROR", message);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException ex) {
    Map<String, String> errors = new LinkedHashMap<>();
    ex.getConstraintViolations().forEach(v -> {
      // propertyPath looks like "getUser.id" or "createUser.request.email"; keep the last node
      String field = null;
      for (Path.Node node : v.getPropertyPath()) {
        field = node.getName();
      }
      errors.putIfAbsent(field, v.getMessage());
    });

    String message = String.join("; ", errors.values());

    log.warn("Constraint violations: {}", errors);
    return ApiResponse.error("VALIDATION_ERROR", message);
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
    Map<String, String> errors = new LinkedHashMap<>();

    ex.getParameterValidationResults().forEach(result -> {
      String param = result.getMethodParameter().getParameterName();
      result.getResolvableErrors().forEach(error -> {
        // For @Valid object params, errors are FieldErrors with the field name
        String key = (error instanceof FieldError fe) ? fe.getField() : param;
        errors.putIfAbsent(key, error.getDefaultMessage());
      });
    });

    String message = errors.entrySet().stream()
        .map(e -> e.getKey() + ": " + e.getValue())
        .collect(Collectors.joining("; "));

    log.warn("Handler method validation errors: {}", errors);
    return ApiResponse.error("VALIDATION_ERROR", message);
  }

  // Malformed Request Exception
  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest req) {
    log.warn("{} {} -> {}", req.getMethod(), req.getRequestURI(), "Http message not readable", ex);
    return ApiResponse.error("MALFORMED_REQUEST", "Please send a valid information");
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
    log.warn("{} {} -> {}", req.getMethod(), req.getRequestURI(), "Method argument type mismatch", ex);
    return ApiResponse.error("INVALID_PARAMETER", "Invalid value for parameter '" + ex.getName() + "'");
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex, HttpServletRequest req) {
    log.warn("{} {} -> {}", req.getMethod(), req.getRequestURI(), "Missing request parameter", ex);
    return ApiResponse.error("MISSING_PARAMETER", "Missing required parameter '" + ex.getParameterName() + "'");
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  public ApiResponse<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
    log.warn("{} {} -> {}", req.getMethod(), req.getRequestURI(), "HTTP request method not supported", ex);
    return ApiResponse.error("METHOD_NOT_ALLOWED", "HTTP method not supported for this endpoint");
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
  public ApiResponse<Void> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
    log.warn("{} {} -> {}", req.getMethod(), req.getRequestURI(), "Unsupported media type", ex);
    return ApiResponse.error("UNSUPPORTED_MEDIA_TYPE", "Content type not supported");
  }

  @ExceptionHandler(NoResourceFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiResponse<Void> handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest req) {
    log.warn("{} {} -> {}", req.getMethod(), req.getRequestURI(), "Resource not found", ex);
    return ApiResponse.error("RESOURCE_NOT_FOUND", "Resource not found");
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
  public ApiResponse<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpServletRequest req) {
    log.warn("{} {} -> {}", req.getMethod(), req.getRequestURI(), "Max upload size exceeded", ex);
    return ApiResponse.error("FILE_TOO_LARGE", "File exceeds the maximum allowed size");
  }

  // Security Exceptions
  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ApiResponse<Void> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest req) {
    log.warn("{} {} -> {}", req.getMethod(), req.getRequestURI(), "Access denied", ex);
    return ApiResponse.error("ACCESS_DENIED", "You do not have permission to perform this action");
  }

  @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ApiResponse<Void> handleAuthenticationException(Exception ex, HttpServletRequest req) {
    log.warn("{} {} -> {}", req.getMethod(), req.getRequestURI(), "Invalid credentials", ex);
    return ApiResponse.error("INVALID_CREDENTIALS", "Invalid credentials");
  }

  // Database Exceptions
  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest req) {
    log.warn("{} {} -> {}", req.getMethod(), req.getRequestURI(), "Data integrity violation", ex);
    return ApiResponse.error("DATA_INTEGRITY_VIOLATION", "Request conflicts with existing data");
  }

  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiResponse<Void> handleObjectOptimisticLockingFailureException(
      ObjectOptimisticLockingFailureException ex,
      HttpServletRequest req) {
    log.warn("{} {} -> {}", req.getMethod(), req.getRequestURI(), "Optimistic locking failure", ex);
    return ApiResponse.error("OPTIMISTIC_LOCK_FAILURE", "This record was changed by someone else. Please refresh and try again");
  }

  // Generic Exception Handler (Catch-all)
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiResponse<Void> handleException(Exception ex, HttpServletRequest req) {
    log.error("Unhandled exception on {} {}", req.getMethod(), req.getRequestURI(), ex);
    return ApiResponse.error("INTERNAL_SERVER_ERROR", "An unexpected error occurred. Please try again later.");
  }
}
