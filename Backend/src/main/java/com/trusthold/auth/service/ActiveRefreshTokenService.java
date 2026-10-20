package com.trusthold.auth.service;

import com.trusthold.auth.model.ActiveRefreshToken;
import com.trusthold.auth.repository.ActiveRefreshTokenRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
public class ActiveRefreshTokenService {

  private ActiveRefreshTokenRepository activeRefreshTokenRepository;

  @Transactional
  public void createActiveRefreshToken(@NotNull UUID userId, @NotBlank String refreshToken){
    ActiveRefreshToken activeRefreshToken = new ActiveRefreshToken();
    activeRefreshToken.setUserId(userId);
    activeRefreshToken.setRefreshToken(refreshToken);
    activeRefreshTokenRepository.save(activeRefreshToken);
  }

  public boolean isRefreshTokenActive(@NotBlank String token){
    return activeRefreshTokenRepository.existsByRefreshToken(token);
  }

}
