package com.trusthold.property.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "properties",
    indexes = {
        @Index(name = "idx_property_landlord", columnList = "landlord_id")
    }
)
@Getter
@Setter
public class Property {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull(message = "Landlord ID cannot be null")
  @Column(name = "landlord_id", nullable = false)
  private UUID landlordId;

  @NotBlank(message = "Property name cannot be null or empty")
  @Column(nullable = false, length = 150)
  private String name;

  @NotNull(message = "Property type cannot be null")
  @Enumerated(EnumType.STRING)
  @Column(name = "property_type", nullable = false, length = 20)
  private PropertyType propertyType;

  @NotBlank(message = "Address line cannot be null or empty")
  @Column(name = "address_line", nullable = false)
  private String addressLine;

  @NotBlank(message = "City cannot be null or empty")
  @Column(nullable = false, length = 100)
  private String city;

  @NotBlank(message = "Description cannot be null or empty")
  @Column(length = 1000, columnDefinition = "TEXT", nullable = false)
  private String description;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
