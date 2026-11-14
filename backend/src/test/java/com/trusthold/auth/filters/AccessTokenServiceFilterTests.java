package com.trusthold.auth.filters;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.auth.service.CookieService;
import com.trusthold.auth.service.JwtService;
import com.trusthold.config.AuthConfig;
import com.trusthold.user.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * The filter only populates the {@link SecurityContextHolder}. It never writes a response:
 * on any token problem it clears the context and lets the chain continue, so Spring Security
 * (entry point / access-denied handler) produces the 401/403.
 */
@ExtendWith(MockitoExtension.class)
public class AccessTokenServiceFilterTests {

  @Mock
  private CookieService cookieService;

  @Mock
  private JwtService jwtService;

  @Mock
  private AuthConfig authConfig;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @InjectMocks
  private AccessTokenFilter accessTokenFilter;

  private final UUID testUserId = UUID.randomUUID();
  private final String validAccessToken = "valid.jwt.token";

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
    MDC.clear();
    lenient().when(authConfig.getPublicEndpoints()).thenReturn(List.of("/auth/login", "/auth/register"));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
    MDC.clear();
  }

  /** Asserts the filter let the request through without authenticating it and without touching the response. */
  private void assertContinuesUnauthenticated() throws Exception {
    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
    assertNull(MDC.get("UserId"));
    verifyNoInteractions(response);
  }

  @Nested
  class DoFilterInternalTests {

    @Nested
    class WhenTokenIsInvalid {

      @Test
      void shouldContinueUnauthenticated_whenAccessTokenIsNull() throws Exception {
        when(cookieService.getAccessTokenCookie(request)).thenReturn(null);

        accessTokenFilter.doFilterInternal(request, response, filterChain);

        assertContinuesUnauthenticated();
        verifyNoInteractions(jwtService);
      }

      @Test
      void shouldContinueUnauthenticated_whenAccessTokenIsBlank() throws Exception {
        when(cookieService.getAccessTokenCookie(request)).thenReturn("   ");

        accessTokenFilter.doFilterInternal(request, response, filterChain);

        assertContinuesUnauthenticated();
        verifyNoInteractions(jwtService);
      }

      @Test
      void shouldContinueUnauthenticated_whenAccessTokenIsExpired() throws Exception {
        when(cookieService.getAccessTokenCookie(request)).thenReturn(validAccessToken);
        when(jwtService.extractAccessClaims(validAccessToken)).thenThrow(new JwtException("JWT expired"));

        accessTokenFilter.doFilterInternal(request, response, filterChain);

        assertContinuesUnauthenticated();
      }

      @Test
      void shouldContinueUnauthenticated_whenAccessTokenSignatureIsInvalid() throws Exception {
        when(cookieService.getAccessTokenCookie(request)).thenReturn("invalid.token");
        when(jwtService.extractAccessClaims("invalid.token")).thenThrow(new JwtException("Invalid signature"));

        accessTokenFilter.doFilterInternal(request, response, filterChain);

        assertContinuesUnauthenticated();
      }

      @Test
      void shouldContinueUnauthenticated_whenAccessTokenIsMalformed() throws Exception {
        when(cookieService.getAccessTokenCookie(request)).thenReturn("malformed.token");
        when(jwtService.extractAccessClaims("malformed.token")).thenThrow(new JwtException("Invalid JWT"));

        accessTokenFilter.doFilterInternal(request, response, filterChain);

        assertContinuesUnauthenticated();
      }

      @Test
      void shouldContinueUnauthenticated_whenSubjectIsNotValidUUID() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("not-a-uuid");
        when(cookieService.getAccessTokenCookie(request)).thenReturn(validAccessToken);
        when(jwtService.extractAccessClaims(validAccessToken)).thenReturn(claims);

        accessTokenFilter.doFilterInternal(request, response, filterChain);

        assertContinuesUnauthenticated();
      }

      @Test
      void shouldContinueUnauthenticated_whenRoleIsNotValidEnumValue() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(testUserId.toString());
        when(claims.get("role", String.class)).thenReturn("INVALID_ROLE");
        when(cookieService.getAccessTokenCookie(request)).thenReturn(validAccessToken);
        when(jwtService.extractAccessClaims(validAccessToken)).thenReturn(claims);

        accessTokenFilter.doFilterInternal(request, response, filterChain);

        assertContinuesUnauthenticated();
      }

      // NOTE: the next two fail against the current filter. UUID.fromString(null) and
      // Role.valueOf(null) throw NullPointerException, which the filter does not catch.
      // See the null-check suggested alongside this file.

      @Test
      void shouldContinueUnauthenticated_whenSubjectClaimIsNull() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(null);
        when(cookieService.getAccessTokenCookie(request)).thenReturn(validAccessToken);
        when(jwtService.extractAccessClaims(validAccessToken)).thenReturn(claims);

        accessTokenFilter.doFilterInternal(request, response, filterChain);

        assertContinuesUnauthenticated();
      }

      @Test
      void shouldContinueUnauthenticated_whenRoleClaimIsNull() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(testUserId.toString());
        when(claims.get("role", String.class)).thenReturn(null);
        when(cookieService.getAccessTokenCookie(request)).thenReturn(validAccessToken);
        when(jwtService.extractAccessClaims(validAccessToken)).thenReturn(claims);

        accessTokenFilter.doFilterInternal(request, response, filterChain);

        assertContinuesUnauthenticated();
      }

      @Test
      void shouldClearExistingAuthentication_whenTokenIsInvalid() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                new AuthUser(UUID.randomUUID(), "stale-token", Role.TENANT),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_TENANT"))));
        when(cookieService.getAccessTokenCookie(request)).thenReturn(null);

        accessTokenFilter.doFilterInternal(request, response, filterChain);

        assertContinuesUnauthenticated();
      }
    }

    @Nested
    class WhenTokenIsValid {

      @ParameterizedTest
      @EnumSource(Role.class)
      void shouldSetAuthenticationAndContinue_forEveryRole(Role role) throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(testUserId.toString());
        when(claims.get("role", String.class)).thenReturn(role.name());
        when(cookieService.getAccessTokenCookie(request)).thenReturn(validAccessToken);
        when(jwtService.extractAccessClaims(validAccessToken)).thenReturn(claims);

        accessTokenFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertInstanceOf(UsernamePasswordAuthenticationToken.class, auth);
        assertTrue(auth.isAuthenticated());

        AuthUser authUser = assertInstanceOf(AuthUser.class, auth.getPrincipal());
        assertEquals(testUserId, authUser.id());
        assertEquals(validAccessToken, authUser.accessToken());
        assertEquals(role, authUser.role());

        // "ROLE_" prefix is required by hasRole()
        assertEquals(1, auth.getAuthorities().size());
        assertEquals("ROLE_" + role.name(), auth.getAuthorities().iterator().next().getAuthority());

        assertEquals(testUserId.toString(), MDC.get("UserId"));
        verifyNoInteractions(response);
      }
    }
  }

  @Nested
  class ShouldNotFilterTests {

    @Test
    void shouldReturnTrue_whenPathIsPublicEndpoint() {
      when(request.getServletPath()).thenReturn("/auth/login");

      assertTrue(accessTokenFilter.shouldNotFilter(request));
    }

    @Test
    void shouldReturnTrue_whenPathMatchesWildcardPublicEndpoint() {
      when(request.getServletPath()).thenReturn("/auth/login/callback");
      when(authConfig.getPublicEndpoints()).thenReturn(List.of("/auth/login/**"));

      assertTrue(accessTokenFilter.shouldNotFilter(request));
    }

    @Test
    void shouldReturnFalse_whenPathHasExtraSegmentBeyondExactPublicEndpoint() {
      when(request.getServletPath()).thenReturn("/auth/login/callback");
      when(authConfig.getPublicEndpoints()).thenReturn(List.of("/auth/login"));

      assertFalse(accessTokenFilter.shouldNotFilter(request));
    }

    @Test
    void shouldReturnFalse_whenPathIsNotPublicEndpoint() {
      when(request.getServletPath()).thenReturn("/api/users");

      assertFalse(accessTokenFilter.shouldNotFilter(request));
    }
  }
}