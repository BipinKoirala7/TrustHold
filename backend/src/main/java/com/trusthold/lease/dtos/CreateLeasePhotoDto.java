package com.trusthold.lease.dtos;

import com.trusthold.lease.model.LeasePhotoPhase;
import com.trusthold.lease.model.PhotoLabel;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateLeasePhotoDto(
    @NotNull(message = "Lease is required")
    UUID leaseId,

    @NotNull(message = "Photo phase is required")
    LeasePhotoPhase phase,

    @NotNull(message = "Photo label is required")
    PhotoLabel label,

    @Size(max = 60, message = "Custom label must be at most 60 characters")
    String customLabel,

    @NotBlank(message = "Image URL is required")
    @Size(max = 500, message = "Image URL must be at most 500 characters")
    String imageUrl,

    @NotNull(message = "Uploader is required")
    UUID uploadedBy
) {

  @AssertTrue(message = "Custom label is required when the label is Other")
  public boolean isCustomLabelProvided() {
    return label != PhotoLabel.OTHER || (customLabel != null && !customLabel.isBlank());
  }
}
