package com.trusthold.lease.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lease_activity")
@Getter
@Setter
public class LeaseActivity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull(message = "Lease is required")
  private UUID leaseId;

  @NotBlank(message = "Activity is required")
  private String activity;

  @NotBlank(message = "Description is required")
  private String description;

  @CreationTimestamp
  private Instant createdAt;
}
