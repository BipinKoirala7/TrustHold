package com.trusthold.shared.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum LeaseException implements ExceptionCode {

  LEASE_NOT_FOUND("LEASE_NOT_FOUND", HttpStatus.NOT_FOUND, "Lease does not exist"),
  TENANT_CANNOT_CREATE_LEASE("TENANT_CANNOT_CREATE_LEASE", HttpStatus.UNAUTHORIZED, "Tenants are not allowed to create leases"),
  PROVIDED_TENANT_IS_NOT_A_TENANT("PROVIDED_TENANT_IS_NOT_A_TENANT", HttpStatus.UNAUTHORIZED, "Provided tenant is not a tenant"),
  LEASE_NOT_MOVED_OUT("LEASE_NOT_MOVED_OUT", HttpStatus.CONFLICT, "Settlement can only be proposed after move-out is complete"),
  LEASE_NOT_AGREED("LEASE_NOT_AGREED", HttpStatus.CONFLICT, "The lease has no agreed settlement to close"),
  LEASE_ALREADY_SETTLED("LEASE_ALREADY_SETTLED", HttpStatus.CONFLICT, "This lease is already settled and locked");

  private final String errCode;
  private final HttpStatus httpStatus;
  private final String message;
}
