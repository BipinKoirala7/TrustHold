package com.trusthold.shared.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum PropertyException implements ExceptionCode {

  PROPERTY_NOT_FOUND("PROPERTY_NOT_FOUND", HttpStatus.NOT_FOUND, "Property chosen for lease doesn't exists"),
  PROPERTY_HAS_ACTIVE_LEASE("PROPERTY_HAS_ACTIVE_LEASE", HttpStatus.FORBIDDEN, "Property has an active lease and cannot be archived"),
  PROPERTY_CANNOT_BE_ARCHIVED("PROPERTY_CANNOT_BE_ARCHIVED", HttpStatus.FORBIDDEN, "Property cannot be archived"),
  TENANT_CANNOT_CREATE_PROPERTY("TENANT_CANNOT_CREATE_PROPERTY", HttpStatus.UNAUTHORIZED, "Tenants are not allowed to create properties"),
  PROPERTY_IS_ARCHIVED("PROPERTY_IS_ARCHIVED", HttpStatus.FORBIDDEN, "Property is archived and cannot be used for lease");

  private final String errCode;
  private final HttpStatus httpStatus;
  private final String message;
}