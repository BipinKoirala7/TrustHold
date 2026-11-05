package com.trusthold.lease.repository;

import com.trusthold.lease.model.LeasePhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LeasePhotoRepository extends JpaRepository<LeasePhoto, UUID> {
}
