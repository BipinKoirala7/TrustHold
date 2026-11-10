package com.trusthold.lease.repository;

import com.trusthold.lease.model.LeaseActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeaseActivityRepository extends JpaRepository<LeaseActivity, UUID> {

  List<LeaseActivity> findTop5ByLeaseIdInOrderByCreatedAtDesc(List<UUID> leaseId);
  List<LeaseActivity> findByLeaseIdOrderByCreatedAtDesc(UUID leaseId);
}
