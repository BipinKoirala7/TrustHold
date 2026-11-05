package com.trusthold.property.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

  @NotNull(message = "Landlord is required")
  @Column(name = "landlord_id", nullable = false, updatable = false)
  private UUID landlordId;

  @NotBlank(message = "Property name is required")
  @Size(max = 150, message = "Property name must be at most 150 characters")
  @Column(nullable = false, length = 150)
  private String name;

  @NotNull(message = "Property type cannot be null")
  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 20)
  private PropertyType type;

  @NotBlank(message = "Address line is required")
  @Size(max = 255, message = "Address must be at most 255 characters")
  @Column(name = "address_line", nullable = false)
  private String addressLine;

  @NotBlank(message = "City is required")
  @Size(max = 100, message = "City must be at most 100 characters")
  @Column(nullable = false, length = 100)
  private String city;

  @NotBlank(message = "Description is required")
  @Size(max = 1000, message = "Description must be at most 1000 characters")
  @Column(length = 1000, nullable = false)
  private String description;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
