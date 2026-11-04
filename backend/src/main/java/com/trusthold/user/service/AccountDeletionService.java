package com.trusthold.user.service;

import com.trusthold.mail.dtos.AccountDeletionEvent;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.exception.AppException;
import com.trusthold.shared.service.TokenService;
import com.trusthold.user.model.AccountDeletionToken;
import com.trusthold.user.model.User;
import com.trusthold.user.repository.AccountDeletionTokenRepository;
import com.trusthold.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AccountDeletionService {

  private static final Duration TOKEN_TTL = Duration.ofMinutes(30);

  private final AccountDeletionTokenRepository tokenRepository;
  private final UserRepository userRepository;
//  private final LeaseRepository leaseRepository;
  private final TokenService tokenService;  // your existing hashed token generator
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void requestDeletion(User user) {
//    assertNoUnsettledLeases(user);

    // Only one live token per user: a new request invalidates older ones.
    tokenRepository.deleteByUserId(user.getId());
    tokenRepository.flush();

    String rawToken = TokenService.generateToken();
    Instant expiresAt = Instant.now().plus(TOKEN_TTL);

    AccountDeletionToken token = new AccountDeletionToken();
    token.setUserId(user.getId());
    token.setTokenHash(TokenService.sha256Hex(rawToken));
    token.setExpiresAt(expiresAt);

    tokenRepository.save(token);

    eventPublisher.publishEvent(new AccountDeletionEvent(user.getId(), user.getEmail(), user.getUsername(), rawToken, expiresAt));
  }

  @Transactional
  public void confirmDeletion(String rawToken) {
    AccountDeletionToken token = tokenRepository
        .findByTokenHash(TokenService.sha256Hex(rawToken))
        .orElseThrow(() -> new AppException(ErrorInfo.DELETION_TOKEN_INVALID));

//    if (token.isUsed()) {
//      throw new AppException(ErrorInfo.DELETION_TOKEN_INVALID);
//    }
    if (token.getExpiresAt().isAfter(Instant.now())) {
      throw new AppException(ErrorInfo.DELETION_TOKEN_EXPIRED);
    }

    // Re-check: a lease may have been created between request and confirm.
//    assertNoUnsettledLeases(user);

//    token.markUsed();
//    tokenRepository.deleteByUser(user);
//    userRepository.delete(user);
  }

//  private void assertNoUnsettledLeases(User user) {
//    if (leaseRepository.existsUnsettledLeaseForUser(user.getId())) {
//      throw new AppException(ErrorInfo.DELETION_BLOCKED_ACTIVE_LEASE);
//    }
//  }
}
