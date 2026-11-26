package com.trusthold.shared.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum LeaseException implements ExceptionCode {

  TENANT_CANNOT_CREATE_LEASE("TENANT_CANNOT_CREATE_LEASE", HttpStatus.UNAUTHORIZED, "Tenants are not allowed to create leases"),
  PROVIDED_TENANT_IS_NOT_A_TENANT("PROVIDED_TENANT_IS_NOT_A_TENANT", HttpStatus.FORBIDDEN, "Provided tenant is not a tenant"),
  LEASE_ALREADY_EXISTS_FOR_PROPERTY("LEASE_ALREADY_EXISTS_FOR_PROPERTY", HttpStatus.CONFLICT, "This property already has a lease. A property can only have one lease at a time"),
  LEASE_ALREADY_EXISTS_FOR_TENANT("LEASE_ALREADY_EXISTS_FOR_TENANT", HttpStatus.CONFLICT, "A tenant cannot have more than one lease for the same property"),
  LEASE_INVITATION_FAILED("LEASE_INVITATION_FAILED", HttpStatus.BAD_REQUEST, "We could not reach the tenant with the provided email. Please check the email address and try again."),
  LEASE_INVITATION_NOT_FOUND("LEASE_INVITATION_NOT_FOUND", HttpStatus.NOT_FOUND, "Lease invitation is not found or has expired. Please request a new invitation from the landlord."),
  LEASE_INVITATION_EMAIL_MISMATCH("LEASE_INVITATION_EMAIL_MISMATCH", HttpStatus.CONFLICT, "The email address provided does not match the one associated with the lease invitation."),
  LEASE_NOT_FOUND("LEASE_NOT_FOUND", HttpStatus.NOT_FOUND, "Lease does not exist"),
  LEASE_NOT_MOVED_OUT("LEASE_NOT_MOVED_OUT", HttpStatus.CONFLICT, "Settlement can only be proposed after move-out is complete"),
  LEASE_NOT_AGREED("LEASE_NOT_AGREED", HttpStatus.CONFLICT, "The lease has no agreed settlement to close"),
  LEASE_ALREADY_SETTLED("LEASE_ALREADY_SETTLED", HttpStatus.CONFLICT, "This lease is already settled and locked");

  private final String errCode;
  private final HttpStatus httpStatus;
  private final String message;
}
