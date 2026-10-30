package com.trusthold.auth.service;

import com.trusthold.auth.dtos.LoginUserDto;
import com.trusthold.auth.dtos.RegisterOAuthUserDto;
import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.auth.model.AuthUser;
import com.trusthold.mail.dtos.UserRegisteredEvent;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.exception.AppException;
import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.user.model.AuthProvider;
import com.trusthold.user.model.Role;
import com.trusthold.user.model.User;
import com.trusthold.user.repository.UserRepository;
import com.trusthold.user.service.UserMapper;
import com.trusthold.user.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {

  @Mock
  private UserMapper userMapper;

  @Mock
  private UserService userService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtService jwtService;

  @Mock
  private CookieService cookieService;

  @Mock
  private ActiveRefreshTokenService activeRefreshTokenService;

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @InjectMocks
  private AuthService underTest;

  private RegisterUserDto registerUserDto;
  private User user;

  @Captor
  private ArgumentCaptor<User> userArgumentCaptor;

  @Captor
  private ArgumentCaptor<UserRegisteredEvent> userRegisteredEventArgumentCaptor;

  @Captor
  private ArgumentCaptor<Cookie> cookieCaptor;

  @BeforeEach
  public void setUp() {
    registerUserDto = new RegisterUserDto(
        "Test",
        "User",
        "testUser_123",
        "testuser@example.com",
        "testPassword@123",
        "+1234567890",
        RegistrationRole.TENANT
    );

    user = new User();
    user.setId(UUID.randomUUID());
    user.setFirstName(registerUserDto.firstName());
    user.setLastName(registerUserDto.lastName());
    user.setUsername(registerUserDto.username());
    user.setEmail(registerUserDto.email());
    user.setPassword(registerUserDto.password());
    user.setRole(Role.valueOf(registerUserDto.registrationRole().name()));

  }

  @Nested
  class RegisterLocalUserTests {

    @Test
    void shouldThrowUsernameAlreadyExists_whenUsernameIsTaken() {
      // Arrange
      when(userRepository.existsByUsername(registerUserDto.username())).thenReturn(true);

      // Act & Assert
      AppException ex = assertThrows(AppException.class, () -> underTest.registerLocalUser(registerUserDto));
      assertThat(ex.getErrorInfo()).isEqualTo(ErrorInfo.USERNAME_ALREADY_EXISTS);
      verify(userRepository).existsByUsername(registerUserDto.username());
      verify(userRepository, never()).existsByEmail(registerUserDto.email());
      verify(userMapper, never()).createUserDtoToUser(registerUserDto);
      verify(passwordEncoder, never()).encode(registerUserDto.password());
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowEmailAlreadyExists_whenEmailIsTaken() {
      // Arrange
      when(userRepository.existsByUsername(registerUserDto.username())).thenReturn(false);
      when(userRepository.existsByEmail(registerUserDto.email())).thenReturn(true);

      // Act & Assert
      AppException ex = assertThrows(AppException.class, () -> underTest.registerLocalUser(registerUserDto));
      assertThat(ex.getErrorInfo()).isEqualTo(ErrorInfo.EMAIL_ALREADY_EXISTS);
      verify(userRepository).existsByUsername(registerUserDto.username());
      verify(userRepository).existsByEmail(registerUserDto.email());
      verify(userMapper, never()).createUserDtoToUser(registerUserDto);
      verify(passwordEncoder, never()).encode(registerUserDto.password());
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldSaveUserWithEncodedPassword_whenRegistrationIsValid() {
      // Arrange
      String encodedPassword = "encodedPassword";
      when(userRepository.existsByUsername(registerUserDto.username())).thenReturn(false);
      when(userRepository.existsByEmail(registerUserDto.email())).thenReturn(false);
      when(userMapper.createUserDtoToUser(registerUserDto)).thenReturn(user);
      when(passwordEncoder.encode(registerUserDto.password())).thenReturn(encodedPassword);
      when(userRepository.save(any(User.class))).thenReturn(user);

      // Act
      underTest.registerLocalUser(registerUserDto);

      // Assert
      verify(passwordEncoder, times(1)).encode(registerUserDto.password());
      verify(userRepository).save(userArgumentCaptor.capture());
      verify(applicationEventPublisher).publishEvent(userRegisteredEventArgumentCaptor.capture());

      User savedUser = userArgumentCaptor.getValue();

      assertSame(user, savedUser);
      assertEquals(encodedPassword, savedUser.getPassword());
      assertEquals(AuthProvider.LOCAL, savedUser.getAuthProvider());
      assertEquals(registerUserDto.email(), userRegisteredEventArgumentCaptor.getValue().email());
      assertEquals(registerUserDto.username(), userRegisteredEventArgumentCaptor.getValue().name());
      assertEquals(String.valueOf(registerUserDto.registrationRole()), userRegisteredEventArgumentCaptor.getValue().role());
    }
  }

  @Nested
  class RegisterOAuthUserTests {

    private RegisterOAuthUserDto registerOAuthUserDto;
    private User oAuthUser;

    @BeforeEach
    void setUp() {
      registerOAuthUserDto = new RegisterOAuthUserDto(
          "google-123",
          "John",
          "Doe",
          "john_doe",
          "john@example.com",
          RegistrationRole.TENANT
      );

      oAuthUser = new User();
      oAuthUser.setId(UUID.randomUUID());
      oAuthUser.setFirstName(registerOAuthUserDto.firstName());
      oAuthUser.setLastName(registerOAuthUserDto.lastName());
      oAuthUser.setUsername(registerOAuthUserDto.username());
      oAuthUser.setEmail(registerOAuthUserDto.email());
      oAuthUser.setRole(Role.valueOf(registerOAuthUserDto.registrationRole().name()));
    }

    @Test
    void shouldThrowUsernameAlreadyExists_whenUsernameIsTaken() {
      // Arrange
      when(userRepository.existsByUsername(registerOAuthUserDto.username())).thenReturn(true);

      // Act & Assert
      AppException ex = assertThrows(AppException.class, () -> underTest.registerOAuthUser(registerOAuthUserDto));
      assertThat(ex.getErrorInfo()).isEqualTo(ErrorInfo.USERNAME_ALREADY_EXISTS);
      verify(userRepository).existsByUsername(registerOAuthUserDto.username());
      verify(userRepository, never()).existsByEmail(registerOAuthUserDto.email());
      verify(userMapper, never()).createOAuthUserDtoToUser(registerOAuthUserDto);
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowEmailAlreadyExists_whenEmailIsTaken() {
      // Arrange
      when(userRepository.existsByUsername(registerOAuthUserDto.username())).thenReturn(false);
      when(userRepository.existsByEmail(registerOAuthUserDto.email())).thenReturn(true);

      // Act & Assert
      AppException ex = assertThrows(AppException.class, () -> underTest.registerOAuthUser(registerOAuthUserDto));
      assertThat(ex.getErrorInfo()).isEqualTo(ErrorInfo.EMAIL_ALREADY_EXISTS);
      verify(userRepository).existsByUsername(registerOAuthUserDto.username());
      verify(userRepository).existsByEmail(registerOAuthUserDto.email());
      verify(userMapper, never()).createOAuthUserDtoToUser(registerOAuthUserDto);
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldSaveOAuthUserAndPublishEvent_whenRegistrationIsValid() {
      // Arrange
      when(userRepository.existsByUsername(registerOAuthUserDto.username())).thenReturn(false);
      when(userRepository.existsByEmail(registerOAuthUserDto.email())).thenReturn(false);
      when(userMapper.createOAuthUserDtoToUser(registerOAuthUserDto)).thenReturn(oAuthUser);
      when(userRepository.save(any(User.class))).thenReturn(oAuthUser);

      // Act
      User result = underTest.registerOAuthUser(registerOAuthUserDto);

      // Assert
      verify(userMapper).createOAuthUserDtoToUser(registerOAuthUserDto);
      verify(userRepository).save(userArgumentCaptor.capture());
      verify(applicationEventPublisher).publishEvent(userRegisteredEventArgumentCaptor.capture());

      assertSame(oAuthUser, result);
      assertEquals(AuthProvider.GOOGLE, oAuthUser.getAuthProvider());
      assertEquals(registerOAuthUserDto.email(), userRegisteredEventArgumentCaptor.getValue().email());
      assertEquals(registerOAuthUserDto.username(), userRegisteredEventArgumentCaptor.getValue().name());
      assertEquals(String.valueOf(registerOAuthUserDto.registrationRole()), userRegisteredEventArgumentCaptor.getValue().role());
    }
  }

  @Nested
  class LoginUserTests {

    private LoginUserDto loginUserDto;
    private User localUser;
    private HttpServletResponse mockResponse;

    @BeforeEach
    void setUp() {
      loginUserDto = new LoginUserDto("testuser@example.com", "Test@1234");
      localUser = new User();
      localUser.setId(UUID.randomUUID());
      localUser.setEmail(loginUserDto.email());
      localUser.setPassword("encodedPassword");
      localUser.setAuthProvider(AuthProvider.LOCAL);
      localUser.setRole(Role.TENANT);
      mockResponse = mock(HttpServletResponse.class);
    }

    @Test
    void shouldThrowInvalidCredentials_whenUserIsNull() {
      // Arrange
      when(userService.getUserByEmail(loginUserDto.email())).thenReturn(null);

      // Act & Assert
      AppException ex = assertThrows(AppException.class, () -> underTest.loginUser(loginUserDto, mockResponse));
      assertThat(ex.getErrorInfo()).isEqualTo(ErrorInfo.INVALID_CREDENTIALS);
      verify(userService).getUserByEmail(loginUserDto.email());
      verify(jwtService, never()).generateRefreshToken(any(User.class));
      verify(jwtService, never()).generateAccessToken(any(User.class));
    }

    @Test
    void shouldThrowInvalidCredentials_whenUserAuthProviderIsGoogle() {
      // Arrange
      localUser.setAuthProvider(AuthProvider.GOOGLE);
      when(userService.getUserByEmail(loginUserDto.email())).thenReturn(localUser);

      // Act & Assert
      AppException ex = assertThrows(AppException.class, () -> underTest.loginUser(loginUserDto, mockResponse));
      assertThat(ex.getErrorInfo()).isEqualTo(ErrorInfo.INVALID_CREDENTIALS);
      verify(userService).getUserByEmail(loginUserDto.email());
      verify(jwtService, never()).generateRefreshToken(any(User.class));
      verify(jwtService, never()).generateAccessToken(any(User.class));
    }

    @Test
    void shouldThrowInvalidCredentials_whenPasswordDoesNotMatch() {
      // Arrange
      when(userService.getUserByEmail(loginUserDto.email())).thenReturn(localUser);
      when(passwordEncoder.matches(loginUserDto.password(), localUser.getPassword())).thenReturn(false);

      // Act & Assert
      AppException ex = assertThrows(AppException.class, () -> underTest.loginUser(loginUserDto, mockResponse));
      assertThat(ex.getErrorInfo()).isEqualTo(ErrorInfo.INVALID_CREDENTIALS);
      verify(passwordEncoder).matches(loginUserDto.password(), localUser.getPassword());
      verify(jwtService, never()).generateRefreshToken(any(User.class));
      verify(jwtService, never()).generateAccessToken(any(User.class));
    }

    @Test
    void shouldGenerateTokensAndSetCookies_whenLoginIsSuccessful() {
      // Arrange
      String refreshToken = "refreshToken123";
      String accessToken = "accessToken123";
      Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
      Cookie accessCookie = new Cookie("accessToken", accessToken);

      when(userService.getUserByEmail(loginUserDto.email())).thenReturn(localUser);
      when(passwordEncoder.matches(loginUserDto.password(), localUser.getPassword())).thenReturn(true);
      when(jwtService.generateRefreshToken(localUser)).thenReturn(refreshToken);
      when(jwtService.generateAccessToken(localUser)).thenReturn(accessToken);
      when(cookieService.createRefreshTokenCookie(refreshToken)).thenReturn(refreshCookie);
      when(cookieService.createAccessTokenCookie(accessToken)).thenReturn(accessCookie);

      // Act
      underTest.loginUser(loginUserDto, mockResponse);

      // Assert
      verify(jwtService).generateRefreshToken(localUser);
      verify(jwtService).generateAccessToken(localUser);
      verify(activeRefreshTokenService).storeActiveRefreshToken(localUser.getId(), refreshToken);
      verify(userService).updateUserLastLoginTime(localUser.getId());
      verify(mockResponse).addCookie(refreshCookie);
      verify(mockResponse).addCookie(accessCookie);
    }

    @Test
    void shouldReplaceExistingValidToken_whenLoggedInAgain(){
      // Arrange
      String refreshToken1 = "refreshToken123";
      String refreshToken2 = "refreshToken456";
      String accessToken1 = "accessToken123";
      String accessToken2 = "accessToken456";

      Cookie refreshTokenCooke1 = new Cookie("refreshToken", refreshToken1);
      Cookie refreshTokenCooke2 = new Cookie("refreshToken", refreshToken2);
      Cookie accessTokenCookie1 = new Cookie("accessToken", accessToken1);
      Cookie accessTokenCookie2 = new Cookie("accessToken", accessToken2);

      when(userService.getUserByEmail(loginUserDto.email())).thenReturn(localUser);
      when(passwordEncoder.matches(loginUserDto.password(), localUser.getPassword())).thenReturn(true);

      when(jwtService.generateRefreshToken(localUser))
          .thenReturn(refreshToken1)
          .thenReturn(refreshToken2);

      when(jwtService.generateAccessToken(localUser))
          .thenReturn(accessToken1)
          .thenReturn(accessToken2);

      when(cookieService.createRefreshTokenCookie(refreshToken1)).thenReturn(refreshTokenCooke1);
      when(cookieService.createAccessTokenCookie(accessToken1)).thenReturn(accessTokenCookie1);
      when(cookieService.createRefreshTokenCookie(refreshToken2)).thenReturn(refreshTokenCooke2);
      when(cookieService.createAccessTokenCookie(accessToken2)).thenReturn(accessTokenCookie2);

      underTest.loginUser(loginUserDto, mockResponse);
      underTest.loginUser(loginUserDto, mockResponse);

      verify(mockResponse, times(4)).addCookie(cookieCaptor.capture());
      List<Cookie> refreshCookies = cookieCaptor.getAllValues().stream().filter(cookie -> cookie.getName().equals("refreshToken")).toList();
      List<Cookie> accessCookies = cookieCaptor.getAllValues().stream().filter(cookie -> cookie.getName().equals("accessToken")).toList();

      assertThat(refreshCookies).containsExactly(refreshTokenCooke1, refreshTokenCooke2);
      assertThat(accessCookies).containsExactly(accessTokenCookie1, accessTokenCookie2);
    }
  }

  @Nested
  class RefreshTokenTests {

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private User user;

    @BeforeEach
    void setUp() {
      mockRequest = mock(HttpServletRequest.class);
      mockResponse = mock(HttpServletResponse.class);
      user = new User();
      user.setId(UUID.randomUUID());
      user.setEmail("testuser@example.com");
      user.setRole(Role.TENANT);
    }

    @Test
    void shouldThrowJwtException_whenRefreshTokenIsNull() {
      // Arrange
      when(cookieService.getRefreshTokenCookie(mockRequest)).thenReturn(null);

      // Act & Assert
      assertThrows(io.jsonwebtoken.JwtException.class, () -> underTest.refreshToken(mockRequest, mockResponse));
      verify(cookieService).getRefreshTokenCookie(mockRequest);
      verify(activeRefreshTokenService, never()).isRefreshTokenActive(anyString());
    }

    @Test
    void shouldThrowJwtException_whenRefreshTokenIsNotActive() {
      // Arrange
      String refreshToken = "invalidRefreshToken";
      when(cookieService.getRefreshTokenCookie(mockRequest)).thenReturn(refreshToken);
      when(activeRefreshTokenService.isRefreshTokenActive(refreshToken)).thenReturn(false);

      // Act & Assert
      assertThrows(JwtException.class, () -> underTest.refreshToken(mockRequest, mockResponse));
      verify(cookieService).getRefreshTokenCookie(mockRequest);
      verify(activeRefreshTokenService).isRefreshTokenActive(refreshToken);
      verify(jwtService, never()).validateRefreshToken(anyString());
    }

    @Test
    void shouldThrowJwtException_whenValidateRefreshTokenThrows() {
      // Arrange
      String refreshToken = "expiredRefreshToken";
      when(cookieService.getRefreshTokenCookie(mockRequest)).thenReturn(refreshToken);
      when(activeRefreshTokenService.isRefreshTokenActive(refreshToken)).thenReturn(true);
      when(jwtService.validateRefreshToken(refreshToken)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

      // Act & Assert
      assertThrows(JwtException.class, () -> underTest.refreshToken(mockRequest, mockResponse));
      verify(cookieService).getRefreshTokenCookie(mockRequest);
      verify(activeRefreshTokenService).isRefreshTokenActive(refreshToken);
      verify(jwtService).validateRefreshToken(refreshToken);
      verify(jwtService, never()).generateRefreshToken(any(User.class));
      verify(jwtService, never()).generateAccessToken(any(User.class));
      verify(mockResponse, never()).addCookie(any(Cookie.class));
    }

    @Test
    void shouldGenerateNewTokensAndSetCookies_whenRefreshTokenIsValid() {
      // Arrange
      String oldRefreshToken = "oldRefreshToken";
      String newRefreshToken = "newRefreshToken";
      String newAccessToken = "newAccessToken";
      Cookie refreshCookie = new Cookie("refreshToken", newRefreshToken);
      Cookie accessCookie = new Cookie("accessToken", newAccessToken);

      when(cookieService.getRefreshTokenCookie(mockRequest)).thenReturn(oldRefreshToken);
      when(activeRefreshTokenService.isRefreshTokenActive(oldRefreshToken)).thenReturn(true);
      when(jwtService.validateRefreshToken(oldRefreshToken)).thenReturn(user);
      when(jwtService.generateRefreshToken(user)).thenReturn(newRefreshToken);
      when(jwtService.generateAccessToken(user)).thenReturn(newAccessToken);
      when(cookieService.createRefreshTokenCookie(newRefreshToken)).thenReturn(refreshCookie);
      when(cookieService.createAccessTokenCookie(newAccessToken)).thenReturn(accessCookie);

      // Act
      underTest.refreshToken(mockRequest, mockResponse);

      // Assert
      verify(jwtService).validateRefreshToken(oldRefreshToken);
      verify(jwtService).generateRefreshToken(user);
      verify(jwtService).generateAccessToken(user);
      verify(activeRefreshTokenService).removeActiveRefreshToken(user.getId());
      verify(activeRefreshTokenService).storeActiveRefreshToken(user.getId(), newRefreshToken);
      verify(mockResponse).addCookie(refreshCookie);
      verify(mockResponse).addCookie(accessCookie);
    }
  }

  @Nested
  class LogoutUserTests {

    private HttpServletResponse mockResponse;
    private AuthUser authUser;

    @BeforeEach
    void setUp() {
      mockResponse = mock(HttpServletResponse.class);
      authUser = new AuthUser(UUID.randomUUID(), "accessToken123", Role.TENANT);
    }

    @Test
    void shouldThrowAppException_whenAuthUserIsNull() {
      // Act & Assert
      AppException ex = assertThrows(AppException.class, () -> underTest.logoutUser(mockResponse, null));
      assertThat(ex.getErrorInfo()).isEqualTo(ErrorInfo.INVALID_TOKEN);
      verify(cookieService, never()).deleteRefreshTokenCookie(any(HttpServletResponse.class));
      verify(cookieService, never()).deleteAccessTokenCookie(any(HttpServletResponse.class));
      verify(activeRefreshTokenService, never()).removeActiveRefreshToken(any(UUID.class));
    }

    @Test
    void shouldDeleteCookiesAndRemoveRefreshToken_whenLogoutIsSuccessful() {
      // Act
      underTest.logoutUser(mockResponse, authUser);

      // Assert
      verify(cookieService).deleteRefreshTokenCookie(mockResponse);
      verify(cookieService).deleteAccessTokenCookie(mockResponse);
      verify(activeRefreshTokenService).removeActiveRefreshToken(authUser.id());
    }
  }
}
