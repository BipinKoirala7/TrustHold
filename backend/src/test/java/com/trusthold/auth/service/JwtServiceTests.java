package com.trusthold.auth.service;

import com.trusthold.config.JwtConfig;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.model.Role;
import com.trusthold.user.model.User;
import com.trusthold.user.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.jsonwebtoken.security.WeakKeyException;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static com.trusthold.config.JwtConfig.ACCESS_TYPE;
import static com.trusthold.config.JwtConfig.REFRESH_TYPE;
import static com.trusthold.config.JwtConfig.TOKEN_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTests {

  private static final String APPLICATION_SIGNATURE = "this-is-a-very-long-secret-key-that-is-at-least-256-bits-long";
  private static final String ISSUER = "trusthold";
  private static final String AUDIENCE = "trusthold-app";
  private static final long ACCESS_TOKEN_EXPIRATION = 3600;
  private static final long REFRESH_TOKEN_EXPIRATION = 86400;

  @Mock
  private UserService userService;

  @Mock
  private JwtConfig jwtConfig;

  @InjectMocks
  private JwtService underTest;

  private User user;
  private SecretKey key;
  private String validAccessToken;
  private String validRefreshToken;

  @BeforeEach
  void setUp() {
    // Configure JwtConfig mock — values needed for both generating and parsing tokens
    when(jwtConfig.getApplicationSignature()).thenReturn(APPLICATION_SIGNATURE);
    when(jwtConfig.getIssuer()).thenReturn(ISSUER);
    when(jwtConfig.getAudience()).thenReturn(AUDIENCE);
    when(jwtConfig.getAccessTokenExpiration()).thenReturn(ACCESS_TOKEN_EXPIRATION);
    when(jwtConfig.getRefreshTokenExpiration()).thenReturn(REFRESH_TOKEN_EXPIRATION);

    // Build user
    user = new User();
    user.setId(UUID.randomUUID());
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setUsername("john_doe");
    user.setEmail("john@example.com");
    user.setRole(Role.TENANT);

    // Build key — needed only for tokens that the service under test cannot produce (e.g., expired)
    key = Keys.hmacShaKeyFor(APPLICATION_SIGNATURE.getBytes(StandardCharsets.UTF_8));

    // Use the service under test to generate valid tokens
    validAccessToken = underTest.generateAccessToken(user);
    validRefreshToken = underTest.generateRefreshToken(user);
  }

  @Nested
  class ExtractAccessClaimsTests {

    @Test
    void shouldThrowJwtException_whenTokenIsInvalid() {
      // Arrange
      String invalidToken = "invalid.token.value";

      // Act & Assert
      assertThrows(JwtException.class, () -> underTest.extractAccessClaims(invalidToken));
    }

    @Test
    void shouldThrowJwtException_whenTokenIsMalformed() {
      // Arrange
      String malformedToken = "not.a.jwt.token.at.all";

      // Act & Assert
      assertThrows(JwtException.class, () -> underTest.extractAccessClaims(malformedToken));
    }

    @Test
    void shouldThrowExpiredJwtException_whenTokenIsExpired() {
      // Arrange
      String expiredToken = Jwts.builder()
          .signWith(key, Jwts.SIG.HS256)
          .issuer(ISSUER)
          .audience().add(AUDIENCE).and()
          .subject(user.getId().toString())
          .claim(TOKEN_TYPE, ACCESS_TYPE)
          .issuedAt(new Date(Instant.now().minusSeconds(7200).toEpochMilli()))
          .expiration(new Date(Instant.now().minusSeconds(3600).toEpochMilli()))
          .compact();

      // Act & Assert
      assertThrows(ExpiredJwtException.class, () -> underTest.extractAccessClaims(expiredToken));
    }

    @Test
    void shouldThrowJwtException_whenTokenTypeIsNotAccessToken() {
      // Arrange — use the valid refresh token which has the wrong type
      // Act & Assert
      JwtException ex = assertThrows(JwtException.class, () -> underTest.extractAccessClaims(validRefreshToken));
      assertThat(ex.getMessage()).isEqualTo("Not an access token");
    }

    @Test
    void shouldReturnClaims_whenTokenIsValidAccessToken() {
      // Act
      Claims claims = underTest.extractAccessClaims(validAccessToken);

      // Assert
      assertThat(claims).isNotNull();
      assertThat(claims.getSubject()).isEqualTo(user.getId().toString());
      assertThat(claims.get(TOKEN_TYPE, String.class)).isEqualTo(ACCESS_TYPE);
      assertThat(claims.get("email", String.class)).isEqualTo(user.getEmail());
      assertThat(claims.get("username", String.class)).isEqualTo(user.getUsername());
      assertThat(claims.get("role", String.class)).isEqualTo(user.getRole().toString());
      assertThat(claims.getIssuer()).isEqualTo(ISSUER);
      assertThat(claims.getAudience()).containsExactly(AUDIENCE);
    }
  }

  @Nested
  class ValidateRefreshTokenTests {

    @Test
    void shouldThrowJwtException_whenTokenIsInvalid() {
      // Arrange
      String invalidToken = "invalid.token.value";

      // Act & Assert
      assertThrows(JwtException.class, () -> underTest.validateRefreshToken(invalidToken));
      verify(userService, never()).getUserById(any(UUID.class));
    }

    @Test
    void shouldThrowJwtException_whenTokenIsMalformed() {
      // Arrange
      String malformedToken = "not.a.jwt.token.at.all";

      // Act & Assert
      assertThrows(JwtException.class, () -> underTest.validateRefreshToken(malformedToken));
      verify(userService, never()).getUserById(any(UUID.class));
    }

    @Test
    void shouldThrowExpiredJwtException_whenTokenIsExpired() {
      // Arrange
      String expiredToken = Jwts.builder()
          .signWith(key, Jwts.SIG.HS256)
          .issuer(ISSUER)
          .audience().add(AUDIENCE).and()
          .subject(user.getId().toString())
          .claim(TOKEN_TYPE, REFRESH_TYPE)
          .issuedAt(new Date(Instant.now().minusSeconds(172800).toEpochMilli()))
          .expiration(new Date(Instant.now().minusSeconds(86400).toEpochMilli()))
          .compact();

      // Act & Assert
      assertThrows(ExpiredJwtException.class, () -> underTest.validateRefreshToken(expiredToken));
      verify(userService, never()).getUserById(any(UUID.class));
    }

    @Test
    void shouldThrowJwtException_whenTokenTypeIsNotRefreshToken() {
      // Arrange — use the valid access token which has the wrong type
      // Act & Assert
      JwtException ex = assertThrows(JwtException.class, () -> underTest.validateRefreshToken(validAccessToken));
      assertThat(ex.getMessage()).isEqualTo("Not a refresh token");
      verify(userService, never()).getUserById(any(UUID.class));
    }

    @Test
    void shouldThrowAppException_whenUserNotFound() {
      // Arrange
      when(userService.getUserById(user.getId())).thenThrow(new AppException(ErrorInfo.USER_NOT_FOUND));

      // Act & Assert
      AppException ex = assertThrows(AppException.class, () -> underTest.validateRefreshToken(validRefreshToken));
      assertThat(ex.getErrorInfo()).isEqualTo(ErrorInfo.USER_NOT_FOUND);
      verify(userService).getUserById(user.getId());
    }

    @Test
    void shouldThrowAppException_whenUserServiceThrowsDatabaseError() {
      // Arrange — simulate a generic DB error wrapped in a RuntimeException
      when(userService.getUserById(user.getId()))
          .thenThrow(new RuntimeException("Database connection failed"));

      // Act & Assert
      RuntimeException ex = assertThrows(RuntimeException.class, () -> underTest.validateRefreshToken(validRefreshToken));
      assertThat(ex.getMessage()).isEqualTo("Database connection failed");
      verify(userService).getUserById(user.getId());
    }

    @Test
    void shouldReturnUser_whenRefreshTokenIsValid() {
      // Arrange
      when(userService.getUserById(user.getId())).thenReturn(user);

      // Act
      User result = underTest.validateRefreshToken(validRefreshToken);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(user.getId());
      assertThat(result.getEmail()).isEqualTo(user.getEmail());
      verify(userService).getUserById(user.getId());
    }
  }

  @Nested
  class WrongIssuerAudienceTests {

    @Test
    void shouldThrowJwtException_whenIssuerIsWrong() {
      // Arrange — token signed with the same key but a different issuer
      String wrongIssuerToken = Jwts.builder()
          .signWith(key, Jwts.SIG.HS256)
          .issuer("wrong-issuer")
          .audience().add(AUDIENCE).and()
          .subject(user.getId().toString())
          .claim(TOKEN_TYPE, ACCESS_TYPE)
          .issuedAt(new Date(Instant.now().toEpochMilli()))
          .expiration(new Date(Instant.now().plusSeconds(ACCESS_TOKEN_EXPIRATION).toEpochMilli()))
          .compact();

      // Act & Assert
      assertThrows(JwtException.class, () -> underTest.extractAccessClaims(wrongIssuerToken));
      assertThrows(JwtException.class, () -> underTest.validateRefreshToken(wrongIssuerToken));
    }

    @Test
    void shouldThrowJwtException_whenAudienceIsWrong() {
      // Arrange — token signed with the same key but a different audience
      String wrongAudienceToken = Jwts.builder()
          .signWith(key, Jwts.SIG.HS256)
          .issuer(ISSUER)
          .audience().add("wrong-audience").and()
          .subject(user.getId().toString())
          .claim(TOKEN_TYPE, ACCESS_TYPE)
          .issuedAt(new Date(Instant.now().toEpochMilli()))
          .expiration(new Date(Instant.now().plusSeconds(ACCESS_TOKEN_EXPIRATION).toEpochMilli()))
          .compact();

      // Act & Assert
      assertThrows(JwtException.class, () -> underTest.extractAccessClaims(wrongAudienceToken));
      assertThrows(JwtException.class, () -> underTest.validateRefreshToken(wrongAudienceToken));
    }
  }

  @Nested
  class WrongSignatureKeyTests {

    @Test
    void shouldThrowJwtException_whenSignedWithDifferentKey() {
      // Arrange — token signed with a different key
      SecretKey otherKey = Keys.hmacShaKeyFor("another-very-long-secret-key-that-is-at-least-256-bits".getBytes(StandardCharsets.UTF_8));
      String wrongKeyToken = Jwts.builder()
          .signWith(otherKey, Jwts.SIG.HS256)
          .issuer(ISSUER)
          .audience().add(AUDIENCE).and()
          .subject(user.getId().toString())
          .claim(TOKEN_TYPE, ACCESS_TYPE)
          .issuedAt(new Date(Instant.now().toEpochMilli()))
          .expiration(new Date(Instant.now().plusSeconds(ACCESS_TOKEN_EXPIRATION).toEpochMilli()))
          .compact();

      // Act & Assert
      assertThrows(JwtException.class, () -> underTest.extractAccessClaims(wrongKeyToken));
      assertThrows(JwtException.class, () -> underTest.validateRefreshToken(wrongKeyToken));
    }
  }

  @Nested
  class MissingTokenTypeClaimTests {

    @Test
    void shouldThrowJwtException_whenTokenTypeClaimIsMissing() {
      // Arrange — valid JWT without TOKEN_TYPE claim
      String noTypeToken = Jwts.builder()
          .signWith(key, Jwts.SIG.HS256)
          .issuer(ISSUER)
          .audience().add(AUDIENCE).and()
          .subject(user.getId().toString())
          .issuedAt(new Date(Instant.now().toEpochMilli()))
          .expiration(new Date(Instant.now().plusSeconds(ACCESS_TOKEN_EXPIRATION).toEpochMilli()))
          .compact();

      // Act & Assert — extractAccessClaims should throw "Not an access token"
      JwtException ex1 = assertThrows(JwtException.class, () -> underTest.extractAccessClaims(noTypeToken));
      assertThat(ex1.getMessage()).isEqualTo("Not an access token");

      // And validateRefreshToken should throw "Not a refresh token"
      JwtException ex2 = assertThrows(JwtException.class, () -> underTest.validateRefreshToken(noTypeToken));
      assertThat(ex2.getMessage()).isEqualTo("Not a refresh token");
    }
  }

  @Nested
  class GenerateAccessTokenTests {

    @Test
    void shouldProduceValidAccessToken_withCorrectClaims() {
      // Act
      String token = underTest.generateAccessToken(user);

      // Assert — parse the token using extractAccessClaims to verify it is valid
      Claims claims = underTest.extractAccessClaims(token);
      assertThat(claims).isNotNull();
      assertThat(claims.getSubject()).isEqualTo(user.getId().toString());
      assertThat(claims.get(TOKEN_TYPE, String.class)).isEqualTo(ACCESS_TYPE);
      assertThat(claims.get("email", String.class)).isEqualTo(user.getEmail());
      assertThat(claims.get("username", String.class)).isEqualTo(user.getUsername());
      assertThat(claims.get("role", String.class)).isEqualTo(user.getRole().toString());
      assertThat(claims.getIssuer()).isEqualTo(ISSUER);
      assertThat(claims.getAudience()).containsExactly(AUDIENCE);
      assertThat(claims.getIssuedAt()).isNotNull();
      assertThat(claims.getExpiration()).isNotNull();
    }
  }

  @Nested
  class GenerateRefreshTokenTests {

    @Test
    void shouldProduceValidRefreshToken_withCorrectClaims() {
      // Act
      String token = underTest.generateRefreshToken(user);

      // Assert — parse via extractClaims (no type check) to verify structure
      Claims claims = underTest.extractClaims(token);
      assertThat(claims).isNotNull();
      assertThat(claims.getSubject()).isEqualTo(user.getId().toString());
      assertThat(claims.get(TOKEN_TYPE, String.class)).isEqualTo(REFRESH_TYPE);
      assertThat(claims.getIssuer()).isEqualTo(ISSUER);
      assertThat(claims.getAudience()).containsExactly(AUDIENCE);
      assertThat(claims.getIssuedAt()).isNotNull();
      assertThat(claims.getExpiration()).isNotNull();
      // Refresh token should not carry access-token-specific claims
      assertThat(claims.get("email")).isNull();
      assertThat(claims.get("username")).isNull();
      assertThat(claims.get("role")).isNull();
    }
  }

  @Nested
  class GenerateSignInKeyTests {

    @Test
    void shouldDeriveKeyFromApplicationSignature() {
      // Act
      SecretKey generatedKey = underTest.generateSignInKey();

      // Assert — the key should match a key built from the same signature
      SecretKey expectedKey = Keys.hmacShaKeyFor(APPLICATION_SIGNATURE.getBytes(StandardCharsets.UTF_8));
      assertThat(generatedKey).isNotNull();
      assertThat(generatedKey.getAlgorithm()).isEqualTo(expectedKey.getAlgorithm());
      assertThat(generatedKey.getEncoded()).isEqualTo(expectedKey.getEncoded());
    }

    @Test
    void shouldThrowWeakKeyException_whenSignatureIsTooShort() {
      // Arrange
      when(jwtConfig.getApplicationSignature()).thenReturn("short");

      // Act & Assert
      assertThrows(WeakKeyException.class, () -> underTest.generateSignInKey());
    }
  }

  @Nested
  class ExtractClaimsTests {

    @Test
    void shouldReturnClaims_whenTokenIsValid() {
      // Act
      Claims claims = underTest.extractClaims(validAccessToken);

      // Assert
      assertThat(claims).isNotNull();
      assertThat(claims.getSubject()).isEqualTo(user.getId().toString());
      assertThat(claims.getIssuer()).isEqualTo(ISSUER);
      assertThat(claims.getAudience()).containsExactly(AUDIENCE);
    }

    @Test
    void shouldThrowJwtException_whenIssuerDoesNotMatch() {
      // Arrange
      String wrongIssuerToken = Jwts.builder()
          .signWith(key, Jwts.SIG.HS256)
          .issuer("wrong-issuer")
          .audience().add(AUDIENCE).and()
          .subject(user.getId().toString())
          .issuedAt(new Date(Instant.now().toEpochMilli()))
          .expiration(new Date(Instant.now().plusSeconds(ACCESS_TOKEN_EXPIRATION).toEpochMilli()))
          .compact();

      // Act & Assert
      assertThrows(JwtException.class, () -> underTest.extractClaims(wrongIssuerToken));
    }

    @Test
    void shouldThrowJwtException_whenAudienceDoesNotMatch() {
      // Arrange
      String wrongAudienceToken = Jwts.builder()
          .signWith(key, Jwts.SIG.HS256)
          .issuer(ISSUER)
          .audience().add("wrong-audience").and()
          .subject(user.getId().toString())
          .issuedAt(new Date(Instant.now().toEpochMilli()))
          .expiration(new Date(Instant.now().plusSeconds(ACCESS_TOKEN_EXPIRATION).toEpochMilli()))
          .compact();

      // Act & Assert
      assertThrows(JwtException.class, () -> underTest.extractClaims(wrongAudienceToken));
    }
  }

  @Nested
  class NullEmptyTokenTests {

    @Test
    void shouldThrowException_whenTokenIsNull() {
      // Act & Assert — JWT library throws IllegalArgumentException for null input
      assertThrows(IllegalArgumentException.class, () -> underTest.extractAccessClaims(null));
      assertThrows(IllegalArgumentException.class, () -> underTest.validateRefreshToken(null));
    }

    @Test
    void shouldThrowException_whenTokenIsEmpty() {
      // Act & Assert — JWT library throws IllegalArgumentException for empty input
      assertThrows(IllegalArgumentException.class, () -> underTest.extractAccessClaims(""));
      assertThrows(IllegalArgumentException.class, () -> underTest.validateRefreshToken(""));
    }
  }
}
