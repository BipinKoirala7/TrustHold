package com.trusthold.auth.service;

import com.trusthold.auth.dtos.ForgotPasswordDto;
import com.trusthold.auth.dtos.ResetPasswordDto;
import com.trusthold.auth.exception.InValidPasswordResetTokenException;
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
  private final ActiveRefreshTokenService activeRefreshTokenService;
  private final CookieService cookieService;

  private static final String INVALID_TOKEN_MSG = "Invalid or expired token";

  @Transactional
  public void forgotPassword(@NotNull ForgotPasswordDto forgotPasswordDto){
    User user = userService.getUserByEmail(forgotPasswordDto.email());
    String rawToken = createResetPasswordToken(user);
  }

  @Transactional
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
        .orElseThrow(() -> new InValidPasswordResetTokenException(INVALID_TOKEN_MSG));

    int consumed = passwordResetTokenRepository.consume(hash, Instant.now());
    if (consumed != 1) {
      throw new InValidPasswordResetTokenException(INVALID_TOKEN_MSG);
    }

    User user = userService.getUserById(passwordResetToken.getUserId());
    String newPasswordHash = passwordEncoder.encode(resetPasswordDto.newPassword());

    user.setPassword(newPasswordHash);
    userRepository.save(user);

    // Remove any other stale password reset tokens
    passwordResetTokenRepository.deleteByUserId(passwordResetToken.getUserId());

    // log out the user
    activeRefreshTokenService.removeActiveRefreshToken(user.getId());
    cookieService.deleteRefreshTokenCookie(response);
    cookieService.deleteAccessTokenCookie(response);
  }

  @Scheduled(cron = "0 0 * * * *") // hourly
  @Transactional
  public void purgeExpired() {
    passwordResetTokenRepository.deleteByExpiresAtBefore(Instant.now());
  }
}
