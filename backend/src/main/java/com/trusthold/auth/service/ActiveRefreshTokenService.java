package com.trusthold.auth.service;

import com.trusthold.auth.model.ActiveRefreshToken;
import com.trusthold.auth.repository.ActiveRefreshTokenRepository;
import com.trusthold.config.JwtConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.UUID;

/**
 * Manages active refresh tokens for users.
 *
 * <p>
 *   Refresh tokens are created and orchestrated by (see {@link JwtService}). This service is only responsible for
 *   managing active refresh tokens.
 * </p>
 * <p>
 *   Some configuration used here is defined in the {@link JwtConfig} class.
 * </p>
 *
 * @see JwtConfig
 * @see ActiveRefreshTokenRepository
 */
@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class ActiveRefreshTokenService {

  private final JwtConfig jwtConfig;
  private final ActiveRefreshTokenRepository activeRefreshTokenRepository;

  /**
   * Stores a new active refresh token for a user.
   *
   * @param userId The ID of the user.
   * @param refreshToken The refresh token to be stored.
   */
  @Transactional
  public void storeActiveRefreshToken(@NotNull UUID userId, @NotBlank String refreshToken){
    log.debug("Storing active refresh token for user with ID: {}", userId);

    ActiveRefreshToken activeRefreshToken = new ActiveRefreshToken();
    activeRefreshToken.setUserId(userId);
    activeRefreshToken.setRefreshToken(refreshToken);
    activeRefreshToken.setExpiresAt(Instant.now().plusMillis(jwtConfig.getRefreshTokenExpiration()));
    activeRefreshTokenRepository.save(activeRefreshToken);

    log.debug("Active refresh token stored for user with ID: {}", userId);
  }

  /**
   * Removes the active refresh token for a user.
   *
   * @param userId The ID of the user.
   */
  @Transactional
  public void removeActiveRefreshToken(@NotNull UUID userId){
    log.debug("Removing active refresh token for user with ID: {}", userId);
    activeRefreshTokenRepository.deleteActiveRefreshTokenByUserId(userId);
  }

  public boolean isRefreshTokenActive(@NotBlank String refreshToken){
    return activeRefreshTokenRepository.existsByRefreshToken(refreshToken);
  }

}
