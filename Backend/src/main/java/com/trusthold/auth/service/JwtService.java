package com.trusthold.auth.service;

import com.trusthold.auth.exception.InValidTokenException;
import com.trusthold.config.JwtConfig;
import com.trusthold.user.User;
import static com.trusthold.config.JwtConfig.REFRESH_TYPE;
import static com.trusthold.config.JwtConfig.TOKEN_TYPE;
import static com.trusthold.config.JwtConfig.ACCESS_TYPE;
import io.jsonwebtoken.Claims;
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

  public SecretKey generateSignInKey(){
    return Keys.hmacShaKeyFor(jwtConfig.getApplicationSignature().getBytes(StandardCharsets.UTF_8));
  }

  public String generateRefreshToken(@NotNull User user){
    return Jwts
        .builder()
        .signWith(generateSignInKey())
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
        .signWith(generateSignInKey())
        .issuer(jwtConfig.getIssuer())
        .audience().add(jwtConfig.getAudience()).and()
        .subject(user.getId().toString())
        .claim(TOKEN_TYPE, ACCESS_TYPE)
        .claim("email", user.getEmail())
        .claim("username", user.getUsername())
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

  public boolean isAccessToken(String token){
    return extractClaims(token).get(TOKEN_TYPE, String.class).equals(ACCESS_TYPE);
  }

  public boolean isRefreshToken(String token){
    return extractClaims(token).get(TOKEN_TYPE, String.class).equals(REFRESH_TYPE);
  }

  public String extractSubject(@NotBlank String token){
    return extractClaims(token).getSubject();
  }

  public String extractEmail(@NotBlank String token){
    return isAccessToken(token) ? extractClaims(token).get("email", String.class) : null;
  }

  public String extractUsername(@NotBlank String token){
    return isAccessToken(token) ? extractClaims(token).get("username", String.class) : null;
  }

  public Instant extractExpiration(String token){
    return extractClaims(token).getExpiration().toInstant();
  }

  public boolean isExpired(String token){
    return extractExpiration(token).isBefore(Instant.now());
  }

  public boolean isRefreshTokenExpired(@NotBlank String token){
    return isRefreshToken(token) && isExpired(token);
  }

  public boolean isAccessTokenExpired(String token){
    return isAccessToken(token) && isExpired(token);
  }

  public void validateAccessToken(@NotBlank String token){
    log.debug("Access Token Validation");

    if(isAccessTokenExpired(token)){
      log.debug("Access Token Validation Failed - Access token is expired");
      throw new InValidTokenException("Access token is expired");
    }
  }

}
