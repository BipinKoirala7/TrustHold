package com.trusthold.user.service;

import com.trusthold.lease.model.Lease;
import com.trusthold.user.dtos.LandlordDashboardSummaryDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface DashboardRepository extends Repository<Lease, UUID> {

  @Query(value = """
    SELECT
      COUNT(*) FILTER (WHERE l.status = 'ACTIVE')                                       AS activeLeases,
      COUNT(*) FILTER (WHERE l.status IN ('ACTIVE','MOVE_OUT_INITIATED')
                         AND l.end_date >= :monthStart AND l.end_date < :monthEnd)      AS moveOutsThisMonth,
      COALESCE(SUM(l.deposit_amount) FILTER (
          WHERE l.status IN ('ACTIVE','MOVE_OUT_INITIATED','MOVED_OUT')
            AND (s.id IS NULL OR s.closed_at IS NULL)), 0)                              AS heldInEscrow,
      COUNT(*) FILTER (WHERE l.status IN ('ACTIVE','MOVE_OUT_INITIATED','MOVED_OUT')
                         AND (s.id IS NULL OR s.closed_at IS NULL))                     AS escrowDepositCount,
      COUNT(*) FILTER (WHERE l.status = 'PENDING_MOVE_IN')                              AS awaitingPayment,
      COALESCE(SUM(l.deposit_amount) FILTER (WHERE l.status = 'PENDING_MOVE_IN'), 0)    AS pendingAmount,
      (SELECT COUNT(*)
         FROM settlement_proposals sp
         JOIN leases pl ON pl.id = sp.lease_id
        WHERE sp.status = 'PENDING'
          AND sp.proposed_by <> :userId
          AND ((pl.tenant_id   = :userId AND sp.tenant_agreed_at   IS NULL)
            OR (pl.landlord_id = :userId AND sp.landlord_agreed_at IS NULL)))           AS awaitingMyAgreement
    FROM leases l
    LEFT JOIN settlements s ON s.lease_id = l.id
    WHERE l.landlord_id = :userId OR l.tenant_id = :userId
    """, nativeQuery = true)
  LandlordDashboardSummaryDto getLeaseSummary(
      @Param("userId") UUID userId,
      @Param("monthStart") Instant monthStart,
      @Param("monthEnd") Instant monthEnd);
}
