package com.trusthold.lease.repository;

import com.trusthold.lease.model.Lease;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LeaseRepository extends JpaRepository<Lease, UUID> {
}
