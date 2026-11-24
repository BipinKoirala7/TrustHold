package com.trusthold.lease.repository;

import com.trusthold.lease.model.Lease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LeaseRepository extends JpaRepository<Lease, UUID> {

  List<Lease> findAllByLandlordId(UUID landlordId);
  List<Lease> findAllByTenantId(UUID tenantId);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE Lease l SET l.tenantId = :tenantId WHERE l.id = :leaseId")
  void updateTenantId(@Param("leaseId") UUID leaseId, @Param("tenantId") UUID tenantId);
}
