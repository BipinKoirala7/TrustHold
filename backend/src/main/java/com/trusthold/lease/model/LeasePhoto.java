package com.trusthold.lease.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "lease_photos",
    indexes = @Index(name = "idx_lease_photo_lease", columnList = "lease_id")
)
@Getter
@Setter
public class LeasePhoto {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull(message = "Lease is required")
  @Column(name = "lease_id", nullable = false, updatable = false)
  private UUID leaseId;

  @NotNull(message = "Photo phase is required")
  @Enumerated(EnumType.STRING)
  @Column(name = "photo_phase", nullable = false, updatable = false, length = 20)
  private LeasePhotoPhase phase;

  @NotNull(message = "Photo label is required")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private PhotoLabel label;

  // Required only when label == OTHER (e.g. "Balcony", "Water heater")
  @Size(max = 60, message = "Custom label must be at most 60 characters")
  @Column(name = "custom_label", length = 60)
  private String customLabel;

  @NotBlank(message = "Image URL is required")
  @Size(max = 500, message = "Image URL must be at most 500 characters")
  @Column(name = "image_url", nullable = false, length = 500)
  private String imageUrl;

  @NotNull(message = "Uploader is required")
  @Column(name = "uploaded_by", nullable = false, updatable = false)
  private UUID uploadedBy;

  @CreationTimestamp
  @Column(name = "uploaded_at", nullable = false, updatable = false)
  private Instant uploadedAt;

  // Last guard; the request DTO/service should reject this first with a proper error
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
