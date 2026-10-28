package com.trusthold.auth.service;

import com.trusthold.auth.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetTokenService {
  private final PasswordResetTokenRepository passwordResetTokenRepository;

  @Transactional
  public void resetPassword(String rawToken, String newPassword){
    log.info("Resetting password");

    String hash = sha256Hex(rawToken);
  }
}
