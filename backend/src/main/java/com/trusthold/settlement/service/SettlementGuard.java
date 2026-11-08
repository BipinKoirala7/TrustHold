package com.trusthold.settlement.service;

import com.trusthold.lease.model.Lease;
import com.trusthold.lease.model.LeaseStatus;
import com.trusthold.settlement.dto.CreateSettlementProposalRequest;
import com.trusthold.settlement.model.Settlement;
import com.trusthold.settlement.model.SettlementProposal;
import com.trusthold.settlement.model.SettlementProposalStatus;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.exception.AppException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SettlementGuard {

  public void requireParty(Lease lease, UUID userId) {
    if (!userId.equals(lease.getTenantId()) && !userId.equals(lease.getLandlordId())) {
      throw new AppException(ErrorInfo.SETTLEMENT_NOT_PARTY);
    }
  }

  public void validateCreate(Lease lease, UUID userId, CreateSettlementProposalRequest req) {
    requireParty(lease, userId);
    if (lease.isSettled()) throw new AppException(ErrorInfo.LEASE_ALREADY_SETTLED);
    if (lease.getStatus() != LeaseStatus.MOVED_OUT) throw new AppException(ErrorInfo.LEASE_NOT_MOVED_OUT);
    if (lease.getDepositAmount() == null || lease.getDepositAmount().signum() <= 0) {
      throw new AppException(ErrorInfo.SETTLEMENT_DEPOSIT_NOT_HELD);
    }
    if (req.refundToTenant().add(req.deductionAmount()).compareTo(lease.getDepositAmount()) != 0) {
      throw new AppException(ErrorInfo.SETTLEMENT_AMOUNTS_MISMATCH);
    }
  }

  public void validateAgree(Lease lease, SettlementProposal proposal, UUID userId) {
    requireParty(lease, userId);
    if (!proposal.getLeaseId().equals(lease.getId())) {
      throw new AppException(ErrorInfo.SETTLEMENT_NOT_PARTY);
    }
    if (proposal.getStatus() != SettlementProposalStatus.PENDING) {
      throw new AppException(ErrorInfo.SETTLEMENT_PROPOSAL_NOT_PENDING);
    }
    boolean isTenant = userId.equals(lease.getTenantId());
    boolean alreadyAgreed = isTenant ? proposal.getTenantAgreedAt() != null
        : proposal.getLandlordAgreedAt() != null;
    if (alreadyAgreed) throw new AppException(ErrorInfo.SETTLEMENT_ALREADY_AGREED_BY_PARTY);
  }

  public void validateClose(Lease lease, Settlement settlement) {
    if (lease.getStatus() != LeaseStatus.AGREED) throw new AppException(ErrorInfo.LEASE_NOT_AGREED);
    if (settlement.getClosedAt() != null) throw new AppException(ErrorInfo.SETTLEMENT_ALREADY_CLOSED);
  }
}