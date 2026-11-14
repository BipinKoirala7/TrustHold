package com.trusthold.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum PropertyError implements ErrorCode {

  PROPERTY_NOT_FOUND("PROPERTY_NOT_FOUND", HttpStatus.NOT_FOUND, "Property chosen for lease doesn't exists"),
  TENANT_CANNOT_CREATE_PROPERTY("TENANT_CANNOT_CREATE_PROPERTY", HttpStatus.UNAUTHORIZED, "Tenants are not allowed to create properties");

  private final String errCode;
  private final HttpStatus httpStatus;
  private final String message;
}