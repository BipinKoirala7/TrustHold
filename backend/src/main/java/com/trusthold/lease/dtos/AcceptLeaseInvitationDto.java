package com.trusthold.lease.dtos;

import jakarta.validation.constraints.NotBlank;

public record AcceptLeaseInvitationDto(
    @NotBlank(message = "Invitation token is required")
    String invitationToken
) {
}
