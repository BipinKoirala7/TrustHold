package com.trusthold.lease.dtos;

import com.trusthold.lease.model.LeasePhotoPhase;
import com.trusthold.lease.model.PhotoLabel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CreateLeasePhotoDto(
    @NotNull(message = "leaseId is required")
    UUID leaseId,

    @NotNull(message = "phase is required")
    LeasePhotoPhase phase,

    @NotNull(message = "label is required")
    PhotoLabel label,

    String customLabel,

    @NotBlank(message = "imageUrl is required")
    String imageUrl,

    @NotNull(message = "uploadedBy is required")
    UUID uploadedBy,

    @NotNull(message = "uploadedAt is required")
    Instant uploadedAt
) {
}
