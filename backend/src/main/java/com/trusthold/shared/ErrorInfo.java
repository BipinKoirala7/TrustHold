package com.trusthold.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorInfo {

  // Auth
  UNAUTHORIZED                ( "AUTH_UNAUTHORIZED",                HttpStatus.UNAUTHORIZED, "Please Log in"),
  FORBIDDEN                   ( "AUTH_FORBIDDEN",                   HttpStatus.FORBIDDEN,    "You are not allowed to access this resource"),
  EXPIRED_PASSWORD_RESET_TOKEN( "AUTH_RESET_TOKEN_EXPIRED",         HttpStatus.BAD_REQUEST,  "Password reset token has expired"),
  INVALID_PASSWORD_RESET_TOKEN( "AUTH_RESET_TOKEN_INVALID",         HttpStatus.BAD_REQUEST,  "Password reset token is invalid"),
  EMAIL_ALREADY_EXISTS        ( "AUTH_EMAIL_TAKEN",                 HttpStatus.CONFLICT,     "Email is already registered"),
  USERNAME_ALREADY_EXISTS     ( "AUTH_USERNAME_TAKEN",              HttpStatus.CONFLICT,     "Username is already taken"),
  INVALID_CREDENTIALS         ( "AUTH_INVALID_CREDENTIALS",         HttpStatus.UNAUTHORIZED, "Invalid credentials"),
  INVALID_ROLE_FOR_REGISTRATION("AUTH_REGISTRATION_ROLE_INVALID",   HttpStatus.BAD_REQUEST,  "Role not allowed for registration"),
  INVALID_TOKEN               ( "AUTH_INVALID_TOKEN",               HttpStatus.UNAUTHORIZED, "Please Log in"),

  // User
  USER_NOT_FOUND              ( "USER_NOT_FOUND",                   HttpStatus.NOT_FOUND,    "Account not found"),
  DELETION_TOKEN_INVALID(       "DELETION_TOKEN_INVALID",           HttpStatus.BAD_REQUEST,  "This deletion link is invalid or has already been used."),
  DELETION_TOKEN_EXPIRED(       "DELETION_TOKEN_EXPIRED",           HttpStatus.GONE,         "This deletion link has expired. Please request a new one."),
  DELETION_BLOCKED_ACTIVE_LEASE("DELETION_BLOCKED_ACTIVE_LEASE",    HttpStatus.CONFLICT,     "You can't delete your account while you have unsettled leases."),

  // Lease
  TENANT_CANNOT_CREATE_LEASE(   "TENANT_CANNOT_CREATE_LEASE",       HttpStatus.UNAUTHORIZED, "Tenants are not allowed to create leases"),
  TENANT_CANNOT_CREATE_PROPERTY("TENANT_CANNOT_CREATE_PROPERTY",    HttpStatus.UNAUTHORIZED, "Tenants are not allowed to create properties"),
  PROVIDED_TENANT_IS_NOT_A_TENANT("PROVIDED_TENANT_IS_NOT_A_TENANT",HttpStatus.UNAUTHORIZED, "Provided tenant is not a tenant"),
  LEASE_NOT_FOUND("LEASE_NOT_FOUND",                                HttpStatus.NOT_FOUND,    "Lease does not exist"),

  // Property
  PROPERTY_NOT_FOUND(           "PROPERTY_NOT_FOUND",               HttpStatus.NOT_FOUND,    "Property chosen for lease doesn't exists"),

  // Settlement
  SETTLEMENT_PROPOSAL_NOT_PENDING("SETTLEMENT_PROPOSAL_NOT_PENDING", HttpStatus.CONFLICT, "This proposal is no longer open for agreement"),
  SETTLEMENT_PROPOSAL_NOT_AGREED("SETTLEMENT_PROPOSAL_NOT_AGREED", HttpStatus.CONFLICT, "Both parties must agree before a settlement is created"),
  SETTLEMENT_ALREADY_AGREED_BY_PARTY("SETTLEMENT_ALREADY_AGREED_BY_PARTY", HttpStatus.CONFLICT, "You have already agreed to this proposal"),
  SETTLEMENT_ALREADY_CLOSED("SETTLEMENT_ALREADY_CLOSED", HttpStatus.CONFLICT, "This settlement is already closed"),
  SETTLEMENT_NOT_PARTY("SETTLEMENT_NOT_PARTY", HttpStatus.FORBIDDEN, "You are not a party to this lease"),
  SETTLEMENT_CANNOT_AGREE_OWN("SETTLEMENT_CANNOT_AGREE_OWN", HttpStatus.FORBIDDEN, "The proposer has already agreed; the other party must respond"),
  SETTLEMENT_AMOUNTS_MISMATCH("SETTLEMENT_AMOUNTS_MISMATCH", HttpStatus.UNPROCESSABLE_CONTENT, "Refund and deduction must add up to the deposit amount"),
  SETTLEMENT_DEPOSIT_NOT_HELD("SETTLEMENT_DEPOSIT_NOT_HELD", HttpStatus.CONFLICT, "No deposit is currently held for this lease"),
  LEASE_NOT_MOVED_OUT("LEASE_NOT_MOVED_OUT", HttpStatus.CONFLICT, "Settlement can only be proposed after move-out is complete"),
  LEASE_NOT_AGREED("LEASE_NOT_AGREED", HttpStatus.CONFLICT, "The lease has no agreed settlement to close"),
  LEASE_ALREADY_SETTLED("LEASE_ALREADY_SETTLED", HttpStatus.CONFLICT, "This lease is already settled and locked"),

  ;

  private final String errCode;
  private final HttpStatus httpStatus;
  private final String message;
}
