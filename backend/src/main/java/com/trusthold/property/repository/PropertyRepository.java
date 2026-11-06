package com.trusthold.property.repository;

import com.trusthold.property.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID> {

  Optional<Property> findByIdAndLandlordId(UUID id, UUID landlordId);
  List<Property> findByLandlordId(UUID landlordId);
}
