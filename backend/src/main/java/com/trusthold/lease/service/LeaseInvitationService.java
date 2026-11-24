package com.trusthold.lease.service;

import com.resend.core.exception.ResendException;
import com.trusthold.auth.model.AuthUser;
import com.trusthold.lease.dtos.AcceptLeaseInvitationDto;
import com.trusthold.lease.model.Lease;
import com.trusthold.lease.model.LeaseInvitationToken;
import com.trusthold.lease.model.LeaseInvitationTokenRepository;
import com.trusthold.mail.EmailService;
import com.trusthold.mail.dtos.LeaseInvitationDto;
import com.trusthold.property.model.Property;
import com.trusthold.shared.Guard;
import com.trusthold.shared.exception.AppException;
import com.trusthold.shared.exception.AuthException;
import com.trusthold.shared.exception.LeaseException;
import com.trusthold.shared.service.TokenService;
import com.trusthold.user.model.User;
import com.trusthold.user.service.UserService;
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
public class LeaseInvitationService {

  private final long LEASE_INVITATION_EXPIRY = 48; // Hr
  private final LeaseInvitationTokenRepository leaseInvitationTokenRepository;
  private final EmailService emailService;
  private final UserService userService;
  private final LeaseHelper leaseHelper;

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

  @Transactional
  public void acceptInvitation(AuthUser authUser, AcceptLeaseInvitationDto acceptLeaseInvitationDto) {
    Guard.requireNotNull(authUser, new AppException(AuthException.UNAUTHORIZED));

    LeaseInvitationToken token = leaseInvitationTokenRepository
        .findNonExpiredInvitation(TokenService.sha256Hex(acceptLeaseInvitationDto.invitationToken()))
        .orElseThrow(() -> new AppException(LeaseException.LEASE_INVITATION_NOT_FOUND));

    log.info("Accepting lease invitation for lease={} for tenant={}", token.getLeaseId(), token.getTenantEmail());

    User tenant = userService.getUserByIdOrNull(authUser.id())
        .orElseThrow(() -> new AppException(AuthException.UNAUTHORIZED));

    if(!tenant.getEmail().equalsIgnoreCase(token.getTenantEmail())){
      log.error("Tenant email mismatch: expected={}, actual={}", token.getTenantEmail(), tenant.getEmail());
      throw new AppException(LeaseException.LEASE_INVITATION_EMAIL_MISMATCH);
    }

    leaseHelper.addTenantToLease(token.getLeaseId(), tenant.getId());
    leaseInvitationTokenRepository.delete(token);
  }
}
