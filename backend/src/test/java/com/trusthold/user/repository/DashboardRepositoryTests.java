package com.trusthold.user.repository;

import com.trusthold.TestcontainersConfiguration;
import com.trusthold.lease.model.Lease;
import com.trusthold.lease.model.LeaseStatus;
import com.trusthold.settlement.model.Settlement;
import com.trusthold.settlement.model.SettlementProposal;
import com.trusthold.settlement.model.SettlementProposalStatus;
import com.trusthold.user.dtos.LandlordDashboardSummaryDto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class DashboardRepositoryTests {

  private static final Instant MONTH_START = Instant.parse("2026-11-01T00:00:00Z");
  private static final Instant MONTH_END = Instant.parse("2026-12-01T00:00:00Z");

  private static final Instant IN_MONTH = Instant.parse("2026-11-15T00:00:00Z");
  private static final Instant BEFORE_MONTH = Instant.parse("2026-10-15T00:00:00Z");
  private static final Instant AFTER_MONTH = Instant.parse("2026-12-15T00:00:00Z");

  @Autowired
  private DashboardRepository dashboardRepository;

  @Autowired
  private EntityManager em;

  private final UUID landlordId = UUID.randomUUID();
  private final UUID tenantId = UUID.randomUUID();
  private final UUID otherLandlordId = UUID.randomUUID();
  private final UUID otherTenantId = UUID.randomUUID();

  // ---------------------------------------------------------------- helpers

  private Lease persistLease(UUID landlord, UUID tenant, LeaseStatus status,
                             String deposit, Instant endDate) {
    Lease lease = new Lease();
    lease.setPropertyId(UUID.randomUUID());
    lease.setLandlordId(landlord);
    lease.setTenantId(tenant);
    lease.setMonthlyRent(new BigDecimal("500.00"));
    lease.setDepositAmount(new BigDecimal(deposit));
    lease.setStartDate(endDate.minus(365, ChronoUnit.DAYS));
    lease.setEndDate(endDate);
    lease.setStatus(status);
    em.persist(lease);
    return lease;
  }

  private void persistProposal(Lease lease, UUID proposedBy,
                               SettlementProposalStatus status,
                               Instant tenantAgreedAt,
                               Instant landlordAgreedAt) {
    SettlementProposal sp = new SettlementProposal();
    sp.setLeaseId(lease.getId());
    sp.setProposedBy(proposedBy);
    sp.setDepositAmount(lease.getDepositAmount());
    sp.setRefundToTenant(new BigDecimal("0.00"));
    sp.setDeductionAmount(lease.getDepositAmount());
    sp.setStatus(status);
    sp.setTenantAgreedAt(tenantAgreedAt);
    sp.setLandlordAgreedAt(landlordAgreedAt);
    em.persist(sp);
  }

  /**
   * closedAt == null means agreed but not yet closed by admin.
   */
  private void persistSettlement(Lease lease, Instant closedAt) {
    Settlement s = new Settlement();
    s.setLeaseId(lease.getId());
    s.setProposalId(UUID.randomUUID());
    s.setDepositAmount(lease.getDepositAmount());
    s.setRefundToTenant(new BigDecimal("0.00"));
    s.setDeductionAmount(lease.getDepositAmount());
    s.setAgreedOn(Instant.parse("2026-11-01T00:00:00Z"));
    if (closedAt != null) {
      s.setClosedAt(closedAt);
      s.setClosedBy(UUID.randomUUID());
    }
    em.persist(s);
  }

  private LandlordDashboardSummaryDto summaryFor(UUID userId) {
    em.flush();
    em.clear();
    return dashboardRepository.getLeaseSummary(userId, MONTH_START, MONTH_END);
  }

  // ------------------------------------------------------------------ tests

  @Test
  void returnsAllZeros_whenUserHasNoLeases() {
    LandlordDashboardSummaryDto result = summaryFor(UUID.randomUUID());

    assertThat(result.activeLeases()).isZero();
    assertThat(result.moveOutThisMonth()).isZero();
    assertThat(result.totalAmountHeldInEscrow()).isEqualByComparingTo("0");
    assertThat(result.escrowDepositCount()).isZero();
    assertThat(result.totalAmountAwaitingPayment()).isZero();
    assertThat(result.totalPendingAmount()).isEqualByComparingTo("0");
    assertThat(result.numbOfAwaitingMyAgreement()).isZero();
  }

  @Test
  void countsOnlyActiveLeases_forActiveLeasesMetric() {
    persistLease(landlordId, tenantId, LeaseStatus.ACTIVE, "1000.00", AFTER_MONTH);
    persistLease(landlordId, tenantId, LeaseStatus.ACTIVE, "1000.00", AFTER_MONTH);
    persistLease(landlordId, tenantId, LeaseStatus.MOVE_OUT_IN_PROGRESS, "1000.00", AFTER_MONTH);
    persistLease(landlordId, tenantId, LeaseStatus.PENDING_MOVE_IN, "1000.00", AFTER_MONTH);
    persistLease(landlordId, tenantId, LeaseStatus.SETTLED, "1000.00", BEFORE_MONTH);

    assertThat(summaryFor(landlordId).activeLeases()).isEqualTo(2);
  }

  @Test
  void ignoresLeasesBelongingToOtherUsers() {
    persistLease(landlordId, tenantId, LeaseStatus.ACTIVE, "1000.00", IN_MONTH);
    persistLease(otherLandlordId, otherTenantId, LeaseStatus.ACTIVE, "9000.00", IN_MONTH);
    persistLease(otherLandlordId, otherTenantId, LeaseStatus.PENDING_MOVE_IN, "7000.00", IN_MONTH);

    LandlordDashboardSummaryDto result = summaryFor(landlordId);

    assertThat(result.activeLeases()).isEqualTo(1);
    assertThat(result.moveOutThisMonth()).isEqualTo(1);
    assertThat(result.totalAmountHeldInEscrow()).isEqualByComparingTo("1000.00");
    assertThat(result.escrowDepositCount()).isEqualTo(1);
    assertThat(result.totalAmountAwaitingPayment()).isZero();
    assertThat(result.totalPendingAmount()).isEqualByComparingTo("0");
  }

  @Test
  void includesLeasesWhereUserIsTenant() {
    persistLease(otherLandlordId, landlordId, LeaseStatus.ACTIVE, "1500.00", AFTER_MONTH);

    LandlordDashboardSummaryDto result = summaryFor(landlordId);

    assertThat(result.activeLeases()).isEqualTo(1);
    assertThat(result.totalAmountHeldInEscrow()).isEqualByComparingTo("1500.00");
  }

  @Test
  void moveOutsThisMonth_countsActiveAndMoveOutInProgressEndingWithinRange() {
    // counted
    persistLease(landlordId, tenantId, LeaseStatus.ACTIVE, "1000.00", IN_MONTH);
    persistLease(landlordId, tenantId, LeaseStatus.MOVE_OUT_IN_PROGRESS, "1000.00", IN_MONTH);
    // lower bound is inclusive
    persistLease(landlordId, tenantId, LeaseStatus.ACTIVE, "1000.00", MONTH_START);
    // not counted: upper bound is exclusive
    persistLease(landlordId, tenantId, LeaseStatus.ACTIVE, "1000.00", MONTH_END);
    // not counted: outside range
    persistLease(landlordId, tenantId, LeaseStatus.ACTIVE, "1000.00", BEFORE_MONTH);
    persistLease(landlordId, tenantId, LeaseStatus.ACTIVE, "1000.00", AFTER_MONTH);
    // not counted: wrong status
    persistLease(landlordId, tenantId, LeaseStatus.MOVED_OUT, "1000.00", IN_MONTH);
    persistLease(landlordId, tenantId, LeaseStatus.AGREED, "1000.00", IN_MONTH);
    persistLease(landlordId, tenantId, LeaseStatus.PENDING_MOVE_IN, "1000.00", IN_MONTH);
    persistLease(landlordId, tenantId, LeaseStatus.SETTLED, "1000.00", IN_MONTH);

    assertThat(summaryFor(landlordId).moveOutThisMonth()).isEqualTo(3);
  }

  @Test
  void heldInEscrow_includesLeasesWithNoSettlementOrAnOpenSettlement() {
    persistLease(landlordId, tenantId, LeaseStatus.ACTIVE, "1000.00", AFTER_MONTH);
    persistLease(landlordId, tenantId, LeaseStatus.MOVE_OUT_IN_PROGRESS, "2000.00", AFTER_MONTH);

    Lease movedOut = persistLease(landlordId, tenantId, LeaseStatus.MOVED_OUT, "3000.00", BEFORE_MONTH);
    persistSettlement(movedOut, null);

    // both parties agreed, waiting on admin to close: deposit is still held
    Lease agreed = persistLease(landlordId, tenantId, LeaseStatus.AGREED, "2500.00", BEFORE_MONTH);
    persistSettlement(agreed, null);

    LandlordDashboardSummaryDto result = summaryFor(landlordId);

    assertThat(result.totalAmountHeldInEscrow()).isEqualByComparingTo("8500.00");
    assertThat(result.escrowDepositCount()).isEqualTo(4);
  }

  @Test
  void heldInEscrow_excludesClosedSettlementsAndNonEscrowStatuses() {
    Lease closed = persistLease(landlordId, tenantId, LeaseStatus.MOVED_OUT, "4000.00", BEFORE_MONTH);
    persistSettlement(closed, Instant.parse("2026-11-05T00:00:00Z"));

    Lease settled = persistLease(landlordId, tenantId, LeaseStatus.SETTLED, "6000.00", BEFORE_MONTH);
    persistSettlement(settled, Instant.parse("2026-11-05T00:00:00Z"));

    persistLease(landlordId, tenantId, LeaseStatus.PENDING_MOVE_IN, "5000.00", AFTER_MONTH);

    LandlordDashboardSummaryDto result = summaryFor(landlordId);

    assertThat(result.totalAmountHeldInEscrow()).isEqualByComparingTo("0");
    assertThat(result.escrowDepositCount()).isZero();
  }

  @Test
  void awaitingPayment_sumsAndCountsPendingMoveInLeases() {
    persistLease(landlordId, tenantId, LeaseStatus.PENDING_MOVE_IN, "600.00", AFTER_MONTH);
    persistLease(landlordId, tenantId, LeaseStatus.PENDING_MOVE_IN, "700.50", AFTER_MONTH);
    persistLease(landlordId, tenantId, LeaseStatus.ACTIVE, "999.00", AFTER_MONTH);

    LandlordDashboardSummaryDto result = summaryFor(landlordId);

    assertThat(result.totalAmountAwaitingPayment()).isEqualTo(2);
    assertThat(result.totalPendingAmount()).isEqualByComparingTo("1300.50");
  }

  @Test
  void awaitingMyAgreement_countsPendingProposalsFromOtherPartyThatIHaveNotAgreedTo() {
    // landlord is the user: tenant proposed, landlord hasn't agreed -> counted
    Lease l1 = persistLease(landlordId, tenantId, LeaseStatus.MOVED_OUT, "1000.00", BEFORE_MONTH);
    persistProposal(l1, tenantId, SettlementProposalStatus.PENDING, Instant.now(), null);

    // user is the tenant: landlord proposed, tenant hasn't agreed -> counted
    Lease l2 = persistLease(otherLandlordId, landlordId, LeaseStatus.MOVED_OUT, "1000.00", BEFORE_MONTH);
    persistProposal(l2, otherLandlordId, SettlementProposalStatus.PENDING, null, Instant.now());

    // user proposed it themselves -> not counted
    Lease l3 = persistLease(landlordId, tenantId, LeaseStatus.MOVED_OUT, "1000.00", BEFORE_MONTH);
    persistProposal(l3, landlordId, SettlementProposalStatus.PENDING, null, Instant.now());

    // user already agreed -> not counted
    Lease l4 = persistLease(landlordId, tenantId, LeaseStatus.MOVED_OUT, "1000.00", BEFORE_MONTH);
    persistProposal(l4, tenantId, SettlementProposalStatus.PENDING, Instant.now(), Instant.now());

    // proposal no longer pending -> not counted
    Lease l5 = persistLease(landlordId, tenantId, LeaseStatus.MOVED_OUT, "1000.00", BEFORE_MONTH);
    persistProposal(l5, tenantId, SettlementProposalStatus.SUPERSEDED, Instant.now(), null);
    Lease l6 = persistLease(landlordId, tenantId, LeaseStatus.MOVED_OUT, "1000.00", BEFORE_MONTH);
    persistProposal(l6, tenantId, SettlementProposalStatus.AGREED, Instant.now(), null);

    // someone else's lease -> not counted
    Lease l7 = persistLease(otherLandlordId, otherTenantId, LeaseStatus.MOVED_OUT, "1000.00", BEFORE_MONTH);
    persistProposal(l7, otherTenantId, SettlementProposalStatus.PENDING, Instant.now(), null);

    assertThat(summaryFor(landlordId).numbOfAwaitingMyAgreement()).isEqualTo(2);
  }
}