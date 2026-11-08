package com.trusthold.lease.repository;

import com.trusthold.lease.dtos.LandlordDashboardStatsSummaryDto;
import com.trusthold.lease.model.Lease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface LeaseRepository extends JpaRepository<Lease, UUID> {

  @Query(value = """
      SELECT
        COUNT(*) FILTER (WHERE status = 'ACTIVE')                                  AS activeLeases,
        COUNT(*) FILTER (WHERE status IN ('ACTIVE','MOVE_OUT_INITIATED')
                           AND end_date >= :monthStart AND end_date < :monthEnd)   AS moveOutsThisMonth,
        COALESCE(SUM(deposit_amount) FILTER (WHERE status IN ('ACTIVE','MOVE_OUT_INITIATED')
                                               AND is_settled = false), 0)         AS heldInEscrow,
        COUNT(*) FILTER (WHERE status IN ('ACTIVE','MOVE_OUT_INITIATED')
                           AND is_settled = false)                                 AS escrowDepositCount,
        COUNT(*) FILTER (WHERE status = 'PENDING_MOVE_IN')                         AS awaitingPayment,
        COALESCE(SUM(deposit_amount) FILTER (WHERE status = 'PENDING_MOVE_IN'), 0) AS pendingAmount
      FROM leases
      WHERE landlord_id = :userId OR tenant_id = :userId
      """, nativeQuery = true)
  LandlordDashboardStatsSummaryDto getDashboardSummary(
      @Param("userId") UUID userId,
      @Param("monthStart") Instant monthStart,
      @Param("monthEnd") Instant monthEnd);

}
