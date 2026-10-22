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

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class ActiveRefreshTokenService {

  private final JwtConfig jwtConfig;
  private final ActiveRefreshTokenRepository activeRefreshTokenRepository;

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

  @Transactional
  public void removeActiveRefreshToken(@NotNull UUID userId){
    log.debug("Removing active refresh token for user with ID: {}", userId);
    activeRefreshTokenRepository.deleteActiveRefreshTokenByUserId(userId);
  }

  public boolean isRefreshTokenActive(@NotBlank String refreshToken){
    return activeRefreshTokenRepository.existsByRefreshToken(refreshToken);
  }

}
