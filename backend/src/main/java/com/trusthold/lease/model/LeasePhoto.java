package com.trusthold.lease.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
public class LeasePhoto {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull
  private UUID leaseId;

  @Enumerated(EnumType.STRING)
  @Column(name = "photo_phase", nullable = false, length = 20)
  private LeasePhotoPhase phase;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private PhotoLabel label;

  // Required only when label == OTHER (e.g. "Balcony", "Water heater")
  @Column(name = "custom_label", length = 60)
  private String customLabel;

  @Column(name = "image_url", nullable = false, length = 500)
  private String imageUrl;

  @NotNull
  private UUID uploadedBy;

  @CreationTimestamp
  @Column(name = "uploaded_at", nullable = false, updatable = false)
  private Instant uploadedAt;

  @PrePersist
  @PreUpdate
  private void validateLabel() {
    if (label == PhotoLabel.OTHER) {
      if (customLabel == null || customLabel.isBlank()) {
        throw new IllegalStateException("customLabel is required when label is OTHER");
      }
      customLabel = customLabel.trim();
    } else {
      customLabel = null;
    }
  }
}
