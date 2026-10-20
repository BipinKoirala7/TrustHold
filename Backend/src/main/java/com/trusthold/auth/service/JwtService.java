package com.trusthold.auth.service;

import com.trusthold.auth.exception.InValidTokenException;
import com.trusthold.config.JwtConfig;
import com.trusthold.user.Role;
import com.trusthold.user.User;
import static com.trusthold.config.JwtConfig.REFRESH_TYPE;
import static com.trusthold.config.JwtConfig.TOKEN_TYPE;
import static com.trusthold.config.JwtConfig.ACCESS_TYPE;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class JwtService {

  private final JwtConfig jwtConfig;
  private final ActiveRefreshTokenService activeRefreshTokenService;

  public SecretKey generateSignInKey(){
    return Keys.hmacShaKeyFor(jwtConfig.getApplicationSignature().getBytes(StandardCharsets.UTF_8));
  }

  public String generateRefreshToken(@NotNull User user){
    return Jwts
        .builder()
        .signWith(generateSignInKey(), Jwts.SIG.HS256)
        .issuer(jwtConfig.getIssuer())
        .audience().add(jwtConfig.getAudience()).and()
        .subject(user.getId().toString())
        .claim(TOKEN_TYPE, REFRESH_TYPE)
        .issuedAt(new Date(Instant.now().toEpochMilli()))
        .expiration(new Date(Instant.now().plusSeconds(jwtConfig.getRefreshTokenExpiration()).toEpochMilli()))
        .compact();
  }

  public String generateAccessToken(@NotNull User user){
    return Jwts
        .builder()
        .signWith(generateSignInKey(), Jwts.SIG.HS256)
        .issuer(jwtConfig.getIssuer())
        .audience().add(jwtConfig.getAudience()).and()
        .subject(user.getId().toString())
        .claim(TOKEN_TYPE, ACCESS_TYPE)
        .claim("email", user.getEmail())
        .claim("username", user.getUsername())
        .claim("role", user.getRole().toString())
        .issuedAt(new Date(Instant.now().toEpochMilli()))
        .expiration(new Date(Instant.now().plusSeconds(jwtConfig.getAccessTokenExpiration()).toEpochMilli()))
        .compact();
  }

  public Claims extractClaims(String token){
    return Jwts
        .parser()
        .verifyWith(generateSignInKey())
        .requireIssuer(jwtConfig.getIssuer())
        .requireAudience(jwtConfig.getAudience())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public Claims extractAccessClaims(String token) {
    Claims claims = extractClaims(token);
    if (!ACCESS_TYPE.equals(claims.get(TOKEN_TYPE, String.class))) {
      throw new JwtException("Not an access token");
    }
    return claims;
  }

  public boolean isAccessTokenValid(@NotNull String token){
    return extractClaims(token).get(TOKEN_TYPE, String.class).equals(ACCESS_TYPE);
  }

  public boolean isRefreshTokenValid(@NotNull String token){
    return extractClaims(token).get(TOKEN_TYPE, String.class).equals(REFRESH_TYPE);
  }

  public String extractSubject(@NotBlank String token){
    return extractClaims(token).getSubject();
  }

  public Role extractRole(@NotBlank String token){
    return isAccessTokenValid(token) ? Role.valueOf(extractClaims(token).get("role", String.class)) : null;
  }

  public void validateAccessToken(@NotBlank String accessToken){
    log.debug("Access Token Validation");

    Claims claims = extractClaims(accessToken);
    if(!ACCESS_TYPE.equals(claims.get(TOKEN_TYPE, String.class))){
      throw new InValidTokenException("Invalid access token");
    }
  }

  public void validateRefreshToken(@NotBlank String refreshToken){
    log.debug("Refresh Token Validation");

    Claims claims = extractClaims(refreshToken);
    if (!REFRESH_TYPE.equals(claims.get(TOKEN_TYPE, String.class)) || !activeRefreshTokenService.isRefreshTokenActive(refreshToken)) {
      throw new InValidTokenException("Invalid refresh token");
    }
  }

}
