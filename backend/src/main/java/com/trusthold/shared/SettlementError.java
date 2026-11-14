package com.trusthold.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum SettlementError implements ErrorCode {

  SETTLEMENT_PROPOSAL_NOT_PENDING("SETTLEMENT_PROPOSAL_NOT_PENDING", HttpStatus.CONFLICT, "This proposal is no longer open for agreement"),
  SETTLEMENT_PROPOSAL_NOT_AGREED("SETTLEMENT_PROPOSAL_NOT_AGREED", HttpStatus.CONFLICT, "Both parties must agree before a settlement is created"),
  SETTLEMENT_ALREADY_AGREED_BY_PARTY("SETTLEMENT_ALREADY_AGREED_BY_PARTY", HttpStatus.CONFLICT, "You have already agreed to this proposal"),
  SETTLEMENT_ALREADY_CLOSED("SETTLEMENT_ALREADY_CLOSED", HttpStatus.CONFLICT, "This settlement is already closed"),
  SETTLEMENT_NOT_PARTY("SETTLEMENT_NOT_PARTY", HttpStatus.FORBIDDEN, "You are not a party to this lease"),
  SETTLEMENT_CANNOT_AGREE_OWN("SETTLEMENT_CANNOT_AGREE_OWN", HttpStatus.FORBIDDEN, "The proposer has already agreed; the other party must respond"),
  SETTLEMENT_AMOUNTS_MISMATCH("SETTLEMENT_AMOUNTS_MISMATCH", HttpStatus.UNPROCESSABLE_CONTENT, "Refund and deduction must add up to the deposit amount"),
  SETTLEMENT_DEPOSIT_NOT_HELD("SETTLEMENT_DEPOSIT_NOT_HELD", HttpStatus.CONFLICT, "No deposit is currently held for this lease");

  private final String errCode;
  private final HttpStatus httpStatus;
  private final String message;
}
