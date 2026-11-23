package com.trusthold.lease.service;

import com.resend.core.exception.ResendException;
import com.trusthold.lease.model.Lease;
import com.trusthold.lease.model.LeaseInvitationToken;
import com.trusthold.lease.model.LeaseInvitationTokenRepository;
import com.trusthold.mail.EmailService;
import com.trusthold.mail.dtos.LeaseInvitationDto;
import com.trusthold.property.model.Property;
import com.trusthold.shared.exception.AppException;
import com.trusthold.shared.exception.LeaseException;
import com.trusthold.shared.service.TokenService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class LeaseInvitationTokenService {

  private final long LEASE_INVITATION_EXPIRY = 48; // Hr
  private final LeaseInvitationTokenRepository leaseInvitationTokenRepository;
  private final EmailService emailService;

  @Transactional
  public void inviteTenant(@NotBlank String landlordName, @NotNull Property property, @NotNull Lease lease, @NotBlank @Email String tenantEmail){
    log.info("Lease Invitation Request for lease={}", lease.getId());
    String rawToken = createLeaseInvitationToken(lease.getId(), tenantEmail);
    LeaseInvitationDto dto = new LeaseInvitationDto(
        landlordName,
        property.getName(),
        property.getAddressLine(),
        lease.getDepositAmount().toString(),
        LEASE_INVITATION_EXPIRY);
    try{
      emailService.sendLeaseInvitationEmail(tenantEmail, dto, rawToken);
    } catch (ResendException e){
      log.error("Failed to send lease invitation email to tenant={} for lease={}", tenantEmail, lease.getId(), e);
      throw new AppException(LeaseException.LEASE_INVITATION_FAILED);
    }
  }

  private String createLeaseInvitationToken(UUID leaseId, String tenantEmail){
    log.info("Creating Lease Invitation Token for lease={}", leaseId);
    deleteStaleInvitationTokens(leaseId);

    String rawToken = TokenService.generateToken();

    LeaseInvitationToken token = new LeaseInvitationToken();
    token.setLeaseId(leaseId);
    token.setTenantEmail(tenantEmail);
    token.setTokenHash(TokenService.sha256Hex(rawToken));
    token.setExpiresAt(Instant.now().plus(LEASE_INVITATION_EXPIRY, ChronoUnit.HOURS));
    leaseInvitationTokenRepository.save(token);

    return rawToken;
  }

  private void deleteStaleInvitationTokens(UUID leaseId){
    log.info("Deleting stale invitation tokens for lease {}", leaseId);
    leaseInvitationTokenRepository.deleteByLeaseId(leaseId);
  }
}
