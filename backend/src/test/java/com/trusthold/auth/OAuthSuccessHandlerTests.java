package com.trusthold.auth;

import com.trusthold.auth.dtos.RegisterOAuthUserDto;
import com.trusthold.auth.handlers.OAuthSuccessHandler;
import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.auth.service.ActiveRefreshTokenService;
import com.trusthold.auth.service.AuthService;
import com.trusthold.auth.service.CookieService;
import com.trusthold.auth.service.JwtService;
import com.trusthold.user.model.User;
import com.trusthold.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OAuthSuccessHandlerTests {

  @InjectMocks
  private OAuthSuccessHandler handler;

  @Mock
  private UserService userService;

  @Mock
  private AuthService authService;

  @Mock
  private JwtService jwtService;

  @Mock
  private CookieService cookieService;

  @Mock
  private ActiveRefreshTokenService activeRefreshTokenService;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpSession session;

  @Mock
  private Authentication authentication;

  @Mock
  private OAuth2User oAuth2User;

  private final String frontendUrl = "http://localhost:3000";

  private User testUser;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(handler, "frontendUrl", frontendUrl);

    testUser = new User();
    testUser.setId(UUID.randomUUID());
    testUser.setProviderId("google-123");
    testUser.setFirstName("John");
    testUser.setLastName("Doe");
    testUser.setUsername("john_doe");
    testUser.setEmail("john@example.com");
  }

  private void setupOAuth2UserWithValidAttributes() {
    when(authentication.getPrincipal()).thenReturn(oAuth2User);
    when(oAuth2User.getAttribute("sub")).thenReturn("google-123");
    when(oAuth2User.getAttribute("email")).thenReturn("john@example.com");
    when(oAuth2User.getAttribute("email_verified")).thenReturn(true);
    when(oAuth2User.getAttribute("given_name")).thenReturn("John");
    when(oAuth2User.getAttribute("family_name")).thenReturn("Doe");
  }

  private void setupLoginAndRedirectMocks() {
    when(jwtService.generateRefreshToken(testUser)).thenReturn("refresh-token");
    when(jwtService.generateAccessToken(testUser)).thenReturn("access-token");
    when(cookieService.createRefreshTokenCookie("refresh-token"))
        .thenReturn(new MockCookie("refresh", "refresh-token"));
    when(cookieService.createAccessTokenCookie("access-token"))
        .thenReturn(new MockCookie("access", "access-token"));
    doNothing().when(activeRefreshTokenService).storeActiveRefreshToken(testUser.getId(), "refresh-token");
    doNothing().when(userService).updateUserLastLoginTime(testUser.getId());
  }

  @Nested
  class OnAuthenticationSuccessTests {

    @Test
    void shouldRedirectToLogin_whenOAuthUserIsNull() throws IOException {
      // Arrange
      when(authentication.getPrincipal()).thenReturn(null);

      // Act
      handler.onAuthenticationSuccess(request, response, authentication);

      // Assert
      verify(response).sendRedirect(frontendUrl + "/login?error=oauth");
      verify(userService, never()).getByEmail(anyString());
    }

    @Test
    void shouldRedirectToLogin_whenProviderIdIsNull() throws IOException {
      // Arrange
      when(authentication.getPrincipal()).thenReturn(oAuth2User);
      when(oAuth2User.getAttribute("sub")).thenReturn(null);

      // Act
      handler.onAuthenticationSuccess(request, response, authentication);

      // Assert
      verify(response).sendRedirect(frontendUrl + "/login?error=provider");
      verify(userService, never()).getByEmail(anyString());
    }

    @Test
    void shouldRedirectToLogin_whenEmailIsNull() throws IOException {
      // Arrange
      when(authentication.getPrincipal()).thenReturn(oAuth2User);
      when(oAuth2User.getAttribute("sub")).thenReturn("google-123");
      when(oAuth2User.getAttribute("email")).thenReturn(null);

      // Act
      handler.onAuthenticationSuccess(request, response, authentication);

      // Assert
      verify(response).sendRedirect(frontendUrl + "/login?error=email");
      verify(userService, never()).getByEmail(anyString());
    }

    @Test
    void shouldRedirectToLogin_whenEmailIsNotVerified() throws IOException {
      // Arrange
      when(authentication.getPrincipal()).thenReturn(oAuth2User);
      when(oAuth2User.getAttribute("sub")).thenReturn("google-123");
      when(oAuth2User.getAttribute("email")).thenReturn("john@example.com");
      when(oAuth2User.getAttribute("email_verified")).thenReturn(false);
      when(oAuth2User.getAttribute("given_name")).thenReturn("John");
      when(oAuth2User.getAttribute("family_name")).thenReturn("Doe");

      // Act
      handler.onAuthenticationSuccess(request, response, authentication);

      // Assert
      verify(response).sendRedirect(frontendUrl + "/login");
      verify(userService, never()).getByEmail(anyString());
    }

    @Test
    void shouldRedirectToLogin_whenEmailVerifiedIsNull() throws IOException {
      // Arrange
      when(authentication.getPrincipal()).thenReturn(oAuth2User);
      when(oAuth2User.getAttribute("sub")).thenReturn("google-123");
      when(oAuth2User.getAttribute("email")).thenReturn("john@example.com");
      when(oAuth2User.getAttribute("email_verified")).thenReturn(null);
      when(oAuth2User.getAttribute("given_name")).thenReturn("John");
      when(oAuth2User.getAttribute("family_name")).thenReturn("Doe");

      // Act
      handler.onAuthenticationSuccess(request, response, authentication);

      // Assert
      verify(response).sendRedirect(frontendUrl + "/login");
      verify(userService, never()).getByEmail(anyString());
    }

    @Test
    void shouldRedirectToLogin_whenGivenNameIsNull() throws IOException {
      // Arrange
      when(authentication.getPrincipal()).thenReturn(oAuth2User);
      when(oAuth2User.getAttribute("sub")).thenReturn("google-123");
      when(oAuth2User.getAttribute("email")).thenReturn("john@example.com");
      when(oAuth2User.getAttribute("email_verified")).thenReturn(true);
      when(oAuth2User.getAttribute("given_name")).thenReturn(null);
      when(oAuth2User.getAttribute("family_name")).thenReturn("Doe");

      // Act
      handler.onAuthenticationSuccess(request, response, authentication);

      // Assert
      verify(response).sendRedirect(frontendUrl + "/login?error=firstname");
      verify(userService, never()).getByEmail(anyString());
    }

    @Test
    void shouldRedirectToLogin_whenFamilyNameIsNull() throws IOException {
      // Arrange
      when(authentication.getPrincipal()).thenReturn(oAuth2User);
      when(oAuth2User.getAttribute("sub")).thenReturn("google-123");
      when(oAuth2User.getAttribute("email")).thenReturn("john@example.com");
      when(oAuth2User.getAttribute("email_verified")).thenReturn(true);
      when(oAuth2User.getAttribute("given_name")).thenReturn("John");
      when(oAuth2User.getAttribute("family_name")).thenReturn(null);

      // Act
      handler.onAuthenticationSuccess(request, response, authentication);

      // Assert
      verify(response).sendRedirect(frontendUrl + "/login?error=lastname");
      verify(userService, never()).getByEmail(anyString());
    }

    @Nested
    class RegisterOAuthUserTests {

      @Test
      void shouldRedirectToLogin_whenRoleIsNullInSession() throws IOException {
        // Arrange
        setupOAuth2UserWithValidAttributes();
        when(userService.getByEmail("john@example.com")).thenReturn(Optional.empty());
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("registrationRole")).thenReturn(null);

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(response).sendRedirect(frontendUrl + "/login?error=role");
        verify(authService, never()).registerOAuthUser(any(RegisterOAuthUserDto.class));
      }

      @Test
      void shouldRedirectToLogin_whenRoleIsInvalidInSession() throws IOException {
        // Arrange
        setupOAuth2UserWithValidAttributes();
        when(userService.getByEmail("john@example.com")).thenReturn(Optional.empty());
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("registrationRole")).thenReturn("INVALID_ROLE");

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(response).sendRedirect(frontendUrl + "/login?error=internal");
        verify(authService, never()).registerOAuthUser(any(RegisterOAuthUserDto.class));
      }

      @Test
      void shouldRedirectToLogin_whenSessionIsNull() throws IOException {
        // Arrange
        setupOAuth2UserWithValidAttributes();
        when(userService.getByEmail("john@example.com")).thenReturn(Optional.empty());
        when(request.getSession(false)).thenReturn(null);

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(response).sendRedirect(frontendUrl + "/login?error=role");
        verify(authService, never()).registerOAuthUser(any(RegisterOAuthUserDto.class));
      }

      @Test
      void shouldRegisterAndLogin_whenUserNotFoundWithTenantRole() throws IOException {
        // Arrange
        setupOAuth2UserWithValidAttributes();
        when(userService.getByEmail("john@example.com")).thenReturn(Optional.empty());
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("registrationRole")).thenReturn("TENANT");
        when(userService.existsByUsername(anyString())).thenReturn(false);
        when(authService.registerOAuthUser(any(RegisterOAuthUserDto.class))).thenReturn(testUser);
        setupLoginAndRedirectMocks();

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        ArgumentCaptor<RegisterOAuthUserDto> captor = ArgumentCaptor.forClass(RegisterOAuthUserDto.class);
        verify(authService).registerOAuthUser(captor.capture());

        RegisterOAuthUserDto capturedDto = captor.getValue();
        assertEquals("google-123", capturedDto.providerId());
        assertEquals("John", capturedDto.firstName());
        assertEquals("Doe", capturedDto.lastName());
        assertEquals("john@example.com", capturedDto.email());
        assertEquals(RegistrationRole.TENANT, capturedDto.registrationRole());

        verify(session).invalidate();
        verify(jwtService).generateRefreshToken(testUser);
        verify(jwtService).generateAccessToken(testUser);
        verify(activeRefreshTokenService).storeActiveRefreshToken(testUser.getId(), "refresh-token");
        verify(cookieService).createRefreshTokenCookie("refresh-token");
        verify(cookieService).createAccessTokenCookie("access-token");
        verify(userService).updateUserLastLoginTime(testUser.getId());
        verify(response).sendRedirect(frontendUrl + "/google/callback");
      }

      @Test
      void shouldRegisterAndLogin_whenUserNotFoundWithLandlordRole() throws IOException {
        // Arrange
        setupOAuth2UserWithValidAttributes();
        when(userService.getByEmail("john@example.com")).thenReturn(Optional.empty());
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("registrationRole")).thenReturn("LANDLORD");
        when(userService.existsByUsername(anyString())).thenReturn(false);
        when(authService.registerOAuthUser(any(RegisterOAuthUserDto.class))).thenReturn(testUser);
        setupLoginAndRedirectMocks();

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        ArgumentCaptor<RegisterOAuthUserDto> captor = ArgumentCaptor.forClass(RegisterOAuthUserDto.class);
        verify(authService).registerOAuthUser(captor.capture());

        RegisterOAuthUserDto capturedDto = captor.getValue();
        assertEquals(RegistrationRole.LANDLORD, capturedDto.registrationRole());
        verify(response).sendRedirect(frontendUrl + "/google/callback");
      }

      @Test
      void shouldHandleException_whenRegistrationFails() throws IOException {
        // Arrange
        setupOAuth2UserWithValidAttributes();
        when(userService.getByEmail("john@example.com")).thenReturn(Optional.empty());
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("registrationRole")).thenReturn("TENANT");
        when(userService.existsByUsername(anyString())).thenThrow(new RuntimeException("Database error"));

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(response).sendRedirect(frontendUrl + "/login?error=internal");
      }

      @Test
      void shouldHandleException_whenAuthServiceThrows() throws IOException {
        // Arrange
        setupOAuth2UserWithValidAttributes();
        when(userService.getByEmail("john@example.com")).thenReturn(Optional.empty());
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("registrationRole")).thenReturn("TENANT");
        when(userService.existsByUsername(anyString())).thenReturn(false);
        when(authService.registerOAuthUser(any(RegisterOAuthUserDto.class)))
            .thenThrow(new RuntimeException("Registration failed"));

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(response).sendRedirect(frontendUrl + "/login?error=internal");
      }
    }

    @Nested
    class LoginOAuthUserTests {

      @Test
      void shouldRedirectToLogin_whenProviderIdNull() throws IOException {
        // Arrange
        setupOAuth2UserWithValidAttributes();
        when(userService.getByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        testUser.setProviderId(null);

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(response).sendRedirect(frontendUrl + "/login?error=user");
        verify(jwtService, never()).generateRefreshToken(any());
        verify(jwtService, never()).generateAccessToken(any());
      }

      @Test
      void shouldRedirectToLogin_whenProviderIdDoesNotMatch() throws IOException {
        // Arrange
        setupOAuth2UserWithValidAttributes();
        when(userService.getByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        testUser.setProviderId("different-provider-id");

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(response).sendRedirect(frontendUrl + "/login?error=provider");
        verify(jwtService, never()).generateRefreshToken(any());
        verify(jwtService, never()).generateAccessToken(any());
      }

      @Test
      void shouldLoginAndRedirect_whenUserExistsWithMatchingProviderId() throws IOException {
        // Arrange
        setupOAuth2UserWithValidAttributes();
        when(userService.getByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        testUser.setProviderId("google-123");
        setupLoginAndRedirectMocks();

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(jwtService).generateRefreshToken(testUser);
        verify(jwtService).generateAccessToken(testUser);
        verify(activeRefreshTokenService).storeActiveRefreshToken(testUser.getId(), "refresh-token");
        verify(cookieService).createRefreshTokenCookie("refresh-token");
        verify(cookieService).createAccessTokenCookie("access-token");
        verify(userService).updateUserLastLoginTime(testUser.getId());
        verify(response).sendRedirect(frontendUrl + "/google/callback");
      }
    }

    @Test
    void shouldRedirectToLogin_whenExceptionThrown() throws IOException {
      // Arrange
      when(authentication.getPrincipal()).thenThrow(new RuntimeException("Unexpected error"));

      // Act
      handler.onAuthenticationSuccess(request, response, authentication);

      // Assert
      verify(response).sendRedirect(frontendUrl + "/login?error=internal");
    }

    @Test
    void shouldNotRedirectAgain_whenResponseAlreadyCommitted() throws IOException {
      // Arrange
      when(authentication.getPrincipal()).thenThrow(new RuntimeException("Unexpected error"));
      when(response.isCommitted()).thenReturn(true);

      // Act
      handler.onAuthenticationSuccess(request, response, authentication);

      // Assert
      verify(response, never()).sendRedirect(anyString());
    }
  }

  @Nested
  class LoginAndRedirectTests {

    private void invokeLoginAndRedirect(User user) throws Exception {
      Method method = OAuthSuccessHandler.class.getDeclaredMethod(
          "loginAndRedirect", User.class, HttpServletResponse.class);
      method.setAccessible(true);
      method.invoke(handler, user, response);
    }

    @Test
    void shouldThrowException_whenActiveRefreshTokenServiceThrows() throws Exception {
      // Arrange
      when(jwtService.generateRefreshToken(testUser)).thenReturn("refresh-token");
      when(jwtService.generateAccessToken(testUser)).thenReturn("access-token");
      doThrow(new RuntimeException("Token storage failed"))
          .when(activeRefreshTokenService).storeActiveRefreshToken(any(UUID.class), anyString());

      // Act & Assert
      assertThrows(Exception.class, () -> invokeLoginAndRedirect(testUser));
    }

    @Test
    void shouldThrowException_whenUserServiceThrows() throws Exception {
      // Arrange
      when(jwtService.generateRefreshToken(testUser)).thenReturn("refresh-token");
      when(jwtService.generateAccessToken(testUser)).thenReturn("access-token");
      doNothing().when(activeRefreshTokenService).storeActiveRefreshToken(testUser.getId(), "refresh-token");
      when(cookieService.createRefreshTokenCookie("refresh-token"))
          .thenReturn(new MockCookie("refresh", "refresh-token"));
      when(cookieService.createAccessTokenCookie("access-token"))
          .thenReturn(new MockCookie("access", "access-token"));
      doThrow(new RuntimeException("User update failed"))
          .when(userService).updateUserLastLoginTime(any(UUID.class));

      // Act & Assert
      assertThrows(Exception.class, () -> invokeLoginAndRedirect(testUser));
    }

    @Test
    void shouldLoginAndRedirectSuccessfully_whenAllServicesWork() throws Exception {
      // Arrange
      setupLoginAndRedirectMocks();

      // Act
      invokeLoginAndRedirect(testUser);

      // Assert
      verify(jwtService).generateRefreshToken(testUser);
      verify(jwtService).generateAccessToken(testUser);
      verify(activeRefreshTokenService).storeActiveRefreshToken(testUser.getId(), "refresh-token");
      verify(cookieService).createRefreshTokenCookie("refresh-token");
      verify(cookieService).createAccessTokenCookie("access-token");
      verify(userService).updateUserLastLoginTime(testUser.getId());
      verify(response, times(2)).addCookie(any(Cookie.class));
      verify(response).sendRedirect(frontendUrl + "/google/callback");
    }
  }
}
