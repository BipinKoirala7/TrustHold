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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.ObjectMapper;

import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccessTokenFilterTests {

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
  private PrintWriter mockPrintWriter;

  @Mock
  private FilterChain filterChain;

  @Mock
  private ObjectMapper objectMapper;

  @Captor
  private ArgumentCaptor<HttpServletRequest> requestCaptor;

  @Captor
  private ArgumentCaptor<HttpServletResponse> responseCaptor;

  @InjectMocks
  private AccessTokenFilter accessTokenFilter;

  private final UUID testUserId = UUID.randomUUID();
  private final String validAccessToken = "valid.jwt.token";

  @BeforeEach
  void setUp() throws Exception {
    SecurityContextHolder.clearContext();
    lenient().when(authConfig.getPublicEndpoints()).thenReturn(List.of("/auth/login", "/auth/register"));
    lenient().when(response.getWriter()).thenReturn(mockPrintWriter);
  }

  @Nested
  class DoFilterInternalTests {

    @Test
    void shouldReturn401_whenAccessTokenIsNull() throws Exception {
      // Arrange
      when(cookieService.getAccessTokenCookie(request)).thenReturn(null);
      when(objectMapper.writeValueAsString(any())).thenReturn("{}");

      // Act
      accessTokenFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      verify(response).setContentType("application/json");
      verify(response).setCharacterEncoding("UTF-8");
      verify(response).getWriter();
      verify(filterChain, never()).doFilter(any(), any());
      verify(cookieService).getAccessTokenCookie(request);
    }

    @Test
    void shouldReturn401_whenAccessTokenIsExpired() throws Exception {
      // Arrange
      when(cookieService.getAccessTokenCookie(request)).thenReturn(validAccessToken);
      doThrow(new JwtException("JWT expired"))
          .when(jwtService)
          .extractAccessClaims(validAccessToken);
      when(objectMapper.writeValueAsString(any())).thenReturn("{}");

      // Act
      accessTokenFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      verify(response).setContentType("application/json");
      verify(response).getWriter();
      verify(filterChain, never()).doFilter(any(), any());
      verify(jwtService).extractAccessClaims(validAccessToken);
    }

    @Test
    void shouldReturn401_whenAccessTokenIsInvalid() throws Exception {
      // Arrange
      when(cookieService.getAccessTokenCookie(request)).thenReturn("invalid.token");
      doThrow(new JwtException("Invalid signature"))
          .when(jwtService)
          .extractAccessClaims("invalid.token");
      when(objectMapper.writeValueAsString(any())).thenReturn("{}");

      // Act
      accessTokenFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      verify(response).setContentType("application/json");
      verify(response).getWriter();
      verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldReturn401_whenAccessTokenIsMalformed() throws Exception {
      // Arrange
      when(cookieService.getAccessTokenCookie(request)).thenReturn("malformed.token");
      doThrow(new JwtException("Invalid JWT"))
          .when(jwtService)
          .extractAccessClaims("malformed.token");
      when(objectMapper.writeValueAsString(any())).thenReturn("{}");

      // Act
      accessTokenFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      verify(response).setContentType("application/json");
      verify(response).getWriter();
      verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldReturn401_whenRoleClaimIsNull() throws Exception {
      // Arrange
      when(cookieService.getAccessTokenCookie(request)).thenReturn(validAccessToken);
      Claims claims = mock(Claims.class);
      when(claims.get("role", String.class)).thenReturn(null);
      when(claims.getSubject()).thenReturn(testUserId.toString());
      when(jwtService.extractAccessClaims(validAccessToken)).thenReturn(claims);
      when(objectMapper.writeValueAsString(any())).thenReturn("{}");

      // Act
      accessTokenFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      verify(response).setContentType("application/json");
      verify(response).getWriter();
      verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldReturn401_whenSubjectClaimIsNull() throws Exception {
      // Arrange
      when(cookieService.getAccessTokenCookie(request)).thenReturn(validAccessToken);
      Claims claims = mock(Claims.class);
      when(claims.get("role", String.class)).thenReturn("TENANT");
      when(claims.getSubject()).thenReturn(null);
      when(jwtService.extractAccessClaims(validAccessToken)).thenReturn(claims);
      when(objectMapper.writeValueAsString(any())).thenReturn("{}");

      // Act
      accessTokenFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      verify(response).setContentType("application/json");
      verify(response).getWriter();
      verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldProceedWhenTokenIsValid() throws Exception {
      // Arrange
      when(cookieService.getAccessTokenCookie(request)).thenReturn(validAccessToken);
      Claims claims = mock(Claims.class);
      when(claims.get("role", String.class)).thenReturn("TENANT");
      when(claims.getSubject()).thenReturn(testUserId.toString());
      when(jwtService.extractAccessClaims(validAccessToken)).thenReturn(claims);

      // Act
      accessTokenFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(filterChain).doFilter(requestCaptor.capture(), responseCaptor.capture());

      // Verify authentication is set in SecurityContext
      SecurityContext securityContext = SecurityContextHolder.getContext();
      assertNotNull(securityContext.getAuthentication());
      assertInstanceOf(UsernamePasswordAuthenticationToken.class, securityContext.getAuthentication());

      UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) securityContext.getAuthentication();
      assertInstanceOf(AuthUser.class, auth.getPrincipal());

      AuthUser authUser = (AuthUser) auth.getPrincipal();
      assertEquals(testUserId, authUser.id());
      assertEquals(validAccessToken, authUser.accessToken());
      assertEquals(Role.TENANT, authUser.role());

      // Verify authorities contain ROLE_ prefix
      assertEquals(1, auth.getAuthorities().size());
      assertEquals("ROLE_TENANT", auth.getAuthorities().iterator().next().getAuthority());

      // Verify response was not modified
      verify(response, never()).setStatus(anyInt());
      verify(response, never()).getWriter();
    }

    @Test
    void shouldProceedWhenTokenIsValid_withLandlordRole() throws Exception {
      // Arrange
      when(cookieService.getAccessTokenCookie(request)).thenReturn(validAccessToken);
      Claims claims = mock(Claims.class);
      when(claims.get("role", String.class)).thenReturn("LANDLORD");
      when(claims.getSubject()).thenReturn(testUserId.toString());
      when(jwtService.extractAccessClaims(validAccessToken)).thenReturn(claims);

      // Act
      accessTokenFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(filterChain).doFilter(any(), any());

      SecurityContext securityContext = SecurityContextHolder.getContext();
      UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) securityContext.getAuthentication();

      assertNotNull(auth);
      assertNotNull(auth.getPrincipal());

      AuthUser authUser = (AuthUser) auth.getPrincipal();
      assertEquals(Role.LANDLORD, authUser.role());
      assertEquals("ROLE_LANDLORD", auth.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void shouldProceedWhenTokenIsValid_withAdminRole() throws Exception {
      // Arrange
      when(cookieService.getAccessTokenCookie(request)).thenReturn(validAccessToken);
      Claims claims = mock(Claims.class);
      when(claims.get("role", String.class)).thenReturn("ADMIN");
      when(claims.getSubject()).thenReturn(testUserId.toString());
      when(jwtService.extractAccessClaims(validAccessToken)).thenReturn(claims);

      // Act
      accessTokenFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(filterChain).doFilter(any(), any());

      SecurityContext securityContext = SecurityContextHolder.getContext();
      UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) securityContext.getAuthentication();

      assertNotNull(auth);
      AuthUser authUser = (AuthUser) auth.getPrincipal();

      assertNotNull(authUser);
      assertEquals(Role.ADMIN, authUser.role());
      assertEquals("ROLE_ADMIN", auth.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void shouldClearSecurityContext_whenExceptionOccurs() throws Exception {
      when(cookieService.getAccessTokenCookie(request)).thenReturn(null);

      accessTokenFilter.doFilterInternal(request, response, filterChain);

      verifyNoInteractions(filterChain);
      assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldSendErrorResponseWithUnauthorizedErrorInfo() throws Exception {
      // Arrange
      when(cookieService.getAccessTokenCookie(request)).thenReturn(null);
      when(objectMapper.writeValueAsString(any()))
          .thenReturn("{\"error\":{\"code\":\"AUTH_UNAUTHORIZED\",\"message\":\"Please Log in\"}}");

      // Act
      accessTokenFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      verify(response).setContentType("application/json");
      verify(response).setCharacterEncoding("UTF-8");
      verify(response).getWriter();
      verify(mockPrintWriter).write("{\"error\":{\"code\":\"AUTH_UNAUTHORIZED\",\"message\":\"Please Log in\"}}");
    }

    @Test
    void shouldNotSendErrorResponse_whenResponseIsAlreadyCommitted() throws Exception {
      // Arrange
      when(cookieService.getAccessTokenCookie(request)).thenReturn(null);
      when(response.isCommitted()).thenReturn(true);

      // Act
      accessTokenFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(response, never()).setStatus(anyInt());
      verify(response, never()).getWriter();
      verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldReturn401_whenSubjectIsNotValidUUID() throws Exception {
      // Arrange
      when(cookieService.getAccessTokenCookie(request)).thenReturn(validAccessToken);
      Claims claims = mock(Claims.class);
      when(claims.get("role", String.class)).thenReturn("TENANT");
      when(claims.getSubject()).thenReturn("not-a-uuid");
      when(jwtService.extractAccessClaims(validAccessToken)).thenReturn(claims);
      when(objectMapper.writeValueAsString(any())).thenReturn("{}");

      // Act
      accessTokenFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      verify(response).getWriter();
      verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldReturn401_whenRoleIsNotValidEnumValue() throws Exception {
      // Arrange
      when(cookieService.getAccessTokenCookie(request)).thenReturn(validAccessToken);
      Claims claims = mock(Claims.class);
      when(claims.get("role", String.class)).thenReturn("INVALID_ROLE");
      when(claims.getSubject()).thenReturn(testUserId.toString());
      when(jwtService.extractAccessClaims(validAccessToken)).thenReturn(claims);
      when(objectMapper.writeValueAsString(any())).thenReturn("{}");

      // Act
      accessTokenFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      verify(response).getWriter();
      verify(filterChain, never()).doFilter(any(), any());
    }
  }

  @Nested
  class ShouldNotFilterTests {

    @Test
    void shouldReturnTrue_whenPathIsPublicEndpoint() {
      // Arrange
      when(request.getServletPath()).thenReturn("/auth/login");
      when(authConfig.getPublicEndpoints()).thenReturn(List.of("/auth/login", "/auth/register"));

      // Act
      boolean result = accessTokenFilter.shouldNotFilter(request);

      // Assert
      assertTrue(result);
    }

    @Test
    void shouldReturnTrue_whenPathStartsWithPublicEndpoint() {
      // Arrange
      when(request.getServletPath()).thenReturn("/auth/login/callback");
      when(authConfig.getPublicEndpoints()).thenReturn(List.of("/auth/login"));

      // Act
      boolean result = accessTokenFilter.shouldNotFilter(request);

      // Assert
      assertTrue(result);
    }

    @Test
    void shouldReturnFalse_whenPathIsNotPublicEndpoint() {
      // Arrange
      when(request.getServletPath()).thenReturn("/api/users");
      when(authConfig.getPublicEndpoints()).thenReturn(List.of("/auth/login", "/auth/register"));

      // Act
      boolean result = accessTokenFilter.shouldNotFilter(request);

      // Assert
      assertFalse(result);
    }
  }
}
