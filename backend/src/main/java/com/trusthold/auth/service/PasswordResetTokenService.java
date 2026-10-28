package com.trusthold.auth.service;

import com.trusthold.auth.dtos.ResetPasswordDto;
import com.trusthold.auth.exception.InValidPasswordResetTokenException;
import com.trusthold.auth.model.AuthUser;
import com.trusthold.auth.model.PasswordResetToken;
import com.trusthold.auth.repository.PasswordResetTokenRepository;
import com.trusthold.user.model.User;
import com.trusthold.user.repository.UserRepository;
import com.trusthold.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetTokenService {
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final UserService userService;
  private final AuthService authService;
  private final ActiveRefreshTokenService activeRefreshTokenService;

  public String createResetPasswordToken(User user){
    passwordResetTokenRepository.deleteByUserId(user.getId());

    byte[] bytes = new byte[32];
    new SecureRandom().nextBytes(bytes);
    String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

    PasswordResetToken entity = new PasswordResetToken();
    entity.setUserId(user.getId());
    entity.setTokenHash(TokenHasher.sha256Hex(rawToken));
    entity.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
    passwordResetTokenRepository.save(entity);

    return rawToken;
  }

  @Transactional
  public void resetPassword(HttpServletResponse response, @NotNull ResetPasswordDto resetPasswordDto){
    log.info("Resetting password");

    String hash = TokenHasher.sha256Hex(resetPasswordDto.rawToken());
    PasswordResetToken passwordResetToken = passwordResetTokenRepository
        .findByTokenHash(hash)
        .orElseThrow(() -> new InValidPasswordResetTokenException("Password Token is either invalid or malformed"));

    if(!passwordResetToken.getExpiresAt().isAfter(Instant.now())){
      throw new InValidPasswordResetTokenException("Password Token is expired");
    }

    User user = userService.getUserById(passwordResetToken.getUserId());
    String newPasswordHash = passwordEncoder.encode(resetPasswordDto.newPassword());

    user.setPassword(newPasswordHash);
    userRepository.save(user);

    // Remove any other stale password reset tokens
    passwordResetTokenRepository.deleteByUserId(passwordResetToken.getUserId());

    // logout the user
    activeRefreshTokenService.revokeAllRefreshTokensForUser(user.getId());
  }

  @Scheduled(cron = "0 0 * * * *") // hourly
  @Transactional
  public void purgeExpired() {
    passwordResetTokenRepository.deleteByExpiresAtBefore(Instant.now());
  }
}
