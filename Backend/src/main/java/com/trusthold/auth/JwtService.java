package com.trusthold.auth;

import com.trusthold.config.JwtConfig;
import com.trusthold.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

  private JwtConfig jwtConfig;

  private final String ACCESS_TYPE = "ACCESS_TOKEN";
  private final String REFRESH_TYPE = "REFRESH_TOKEN";
  private final String TOKEN_TYPE = "TOKEN_TYPE";

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

  public Claims extractClaims(@NotNull String token){
    return Jwts
        .parser()
        .verifyWith(generateSignInKey())
        .requireIssuer(jwtConfig.getIssuer())
        .requireAudience(jwtConfig.getAudience())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public boolean isAccessToken(@NotNull String token){
    return extractClaims(token).get(TOKEN_TYPE, String.class).equals(ACCESS_TYPE);
  }

  public boolean isRefreshToken(@NotNull String token){
    return extractClaims(token).get(TOKEN_TYPE, String.class).equals(REFRESH_TYPE);
  }

  public String extractSubject(@NotNull String token){
    return extractClaims(token).getSubject();
  }

  public String extractEmail(@NotNull String token){
    return isAccessToken(token) ? extractClaims(token).get("email", String.class) : null;
  }

  public String extractUsername(@NotNull String token){
    return isAccessToken(token) ? extractClaims(token).get("username", String.class) : null;
  }

  public Instant extractExpiration(@NotNull String token){
    return extractClaims(token).getExpiration().toInstant();
  }
}
