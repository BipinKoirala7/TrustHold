package com.trusthold.auth.service;

import com.trusthold.config.JwtConfig;
import com.trusthold.user.model.User;
import static com.trusthold.config.JwtConfig.REFRESH_TYPE;
import static com.trusthold.config.JwtConfig.TOKEN_TYPE;
import static com.trusthold.config.JwtConfig.ACCESS_TYPE;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Creates and verifies the JWTs used for authentication.
 * <p>
 * Two token types exist, distinguished by the {@code TOKEN_TYPE} claim:
 * <ul>
 *   <li><b>Access token</b>: short-lived, carries identity and role, authenticates each request.</li>
 *   <li><b>Refresh token</b>: longer-lived, carries only the subject, used to get a new pair.</li>
 * </ul>
 * Both are signed with HS256 and carry issuer and audience claims that are checked on parsing.
 * <p>
 * This class only handles token mechanics. It does not read or write cookies
 * (see {@link CookieService}) and does not decide who may log in (see {@link AuthService}).
 * <p>
 * Never log a raw token: anyone who can read the logs could replay it.
 *
 * @see JwtConfig
 */
@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class JwtService {

  private final JwtConfig jwtConfig;

  /**
  * Builds HMAC key from the configured application signature.
  *
  * @throws io.jsonwebtoken.security.WeakKeyException if the signature is shorter than 256 bits
  */
  public SecretKey generateSignInKey(){
    return Keys.hmacShaKeyFor(jwtConfig.getApplicationSignature().getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Issues a refresh token with userId as a subject and token type as a claim.
   * It is long-lived and carries only the required information.
   *
   * @param user the user to issue the token for
   * @return the refresh token
   */
  public String generateRefreshToken(@NotNull User user){
    log.debug("Generating refresh token: userId={}", user.getId());

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

  /**
   * Issues an access token with userId as a subject and email, username, role as a claim.
   * It is short-lived and carries claims for authenticating a request without a DB lookup.
   * <p>
   *   JWT payloads are only Base64-encoded and should not be trusted for security-sensitive information.
   * </p>
   *
   * @param user the user to issue the token for
   * @return the access token
   */
  public String generateAccessToken(@NotNull User user){
    log.debug("Generating access token: userId={}", user.getId());
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

  /**
   * Extracts the claims from a provided JWT token after validating its signature and mandatory claims.
   * The method verifies the token's issuer and audience against the configured values.
   *
   * <p>
   *   Doesn't check the token type. Use {@link #extractAccessClaims(String)} for access tokens
   * </p>
   *
   * @param token the JWT token to extract claims from
   * @return the extracted claims from the token
   * @throws JwtException if the token is malformed, invalid, or doesn't match the expected issuer and audience
   * @throws io.jsonwebtoken.ExpiredJwtException if the token is expired
   */
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

  /**
   * Extracts claims from an access token.
   * Uses {@link #extractClaims(String)} to parse the token and extract its claims.
   * Used by {@code AccessTokenFilter} so a refresh token can authenticate a request.
   *
   * @param token the access token to extract claims from
   * @return the extracted claims from the token
   * @throws JwtException if the token is malformed, invalid, or doesn't match the expected issuer and audience
   * @throws io.jsonwebtoken.ExpiredJwtException if the token is expired
   */
  public Claims extractAccessClaims(String token) {
    log.debug("Extracting access claims");
    Claims claims = extractClaims(token);
    if (!ACCESS_TYPE.equals(claims.get(TOKEN_TYPE, String.class))) {
      log.warn("Token type mismatch: expected={}, userId={}", ACCESS_TYPE, claims.getSubject());
      throw new JwtException("Not an access token");
    }
    return claims;
  }
}
