package com.trusthold.auth.service;

import com.trusthold.auth.dtos.ForgotPasswordDto;
import com.trusthold.auth.dtos.ResetPasswordDto;
import com.trusthold.auth.model.PasswordResetToken;
import com.trusthold.auth.repository.PasswordResetTokenRepository;
import com.trusthold.mail.dtos.PasswordResetEvent;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.model.User;
import com.trusthold.user.repository.UserRepository;
import com.trusthold.user.service.UserService;
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
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordResetTokenServiceTests {

  @Mock
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserService userService;

  @Mock
  private ActiveRefreshTokenService activeRefreshTokenService;

  @Mock
  private CookieService cookieService;

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @InjectMocks
  private PasswordResetTokenService underTest;

  @Captor
  private ArgumentCaptor<PasswordResetEvent> passwordResetEventCaptor;

  private User user;
  private ForgotPasswordDto forgotPasswordDto;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(UUID.randomUUID());
    user.setFirstName("Test");
    user.setLastName("User");
    user.setEmail("testuser@example.com");
    user.setPassword("oldPasswordHash");

    forgotPasswordDto = new ForgotPasswordDto("testuser@example.com");
  }

  @Nested
  class ForgotPasswordTests {

    @Test
    void shouldThrowUserNotFound_whenUserEmailDoesNotExist() {
      // Arrange
      when(userRepository.findByEmail(forgotPasswordDto.email())).thenReturn(Optional.empty());

      // Act & Assert
      AppException ex = assertThrows(AppException.class, () -> underTest.forgotPassword(forgotPasswordDto));
      assertThat(ex.getErrorInfo()).isEqualTo(ErrorInfo.USER_NOT_FOUND);
      verify(userRepository).findByEmail(forgotPasswordDto.email());
      verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
      verify(applicationEventPublisher, never()).publishEvent(any(PasswordResetEvent.class));
    }

    @Test
    void shouldPropagateDBError_whenSaveThrowsDataAccessException() {
      // Arrange
      when(userRepository.findByEmail(forgotPasswordDto.email())).thenReturn(Optional.of(user));
      doThrow(new DataAccessException("DB error") {}).when(passwordResetTokenRepository).save(any(PasswordResetToken.class));

      // Act & Assert
      assertThrows(DataAccessException.class, () -> underTest.forgotPassword(forgotPasswordDto));
      verify(userRepository).findByEmail(forgotPasswordDto.email());
      verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
      verify(applicationEventPublisher, never()).publishEvent(any(PasswordResetEvent.class));
    }

    @Test
    void shouldPublishPasswordResetEvent_whenUserExists() {
      // Arrange
      when(userRepository.findByEmail(forgotPasswordDto.email())).thenReturn(Optional.of(user));

      // Act
      underTest.forgotPassword(forgotPasswordDto);

      // Assert
      verify(userRepository).findByEmail(forgotPasswordDto.email());
      verify(passwordResetTokenRepository).deleteByUserId(user.getId());
      verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
      verify(applicationEventPublisher).publishEvent(passwordResetEventCaptor.capture());

      PasswordResetEvent event = passwordResetEventCaptor.getValue();
      assertThat(event.to()).isEqualTo(user.getEmail());
      assertThat(event.firstName()).isEqualTo(user.getFirstName());
      assertThat(event.rawToken()).isNotNull();
      assertThat(event.rawToken()).isNotEmpty();
    }

    @Test
    void shouldDeleteStaleTokens_beforeSavingNewToken() {
      // Arrange
      when(userRepository.findByEmail(forgotPasswordDto.email())).thenReturn(Optional.of(user));

      // Act
      underTest.forgotPassword(forgotPasswordDto);

      // Assert - verify deleteByUserId is called before save
      verify(passwordResetTokenRepository).deleteByUserId(user.getId());
      verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void shouldStoreSha256Hash_ofRawToken() {
      // Arrange
      when(userRepository.findByEmail(forgotPasswordDto.email())).thenReturn(Optional.of(user));

      // Act
      underTest.forgotPassword(forgotPasswordDto);

      // Assert
      verify(applicationEventPublisher).publishEvent(passwordResetEventCaptor.capture());
      PasswordResetEvent event = passwordResetEventCaptor.getValue();
      String rawToken = event.rawToken();

      ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
      verify(passwordResetTokenRepository).save(tokenCaptor.capture());

      PasswordResetToken savedToken = tokenCaptor.getValue();
      assertThat(savedToken.getTokenHash()).isEqualTo(TokenHasher.sha256Hex(rawToken));
    }
  }

  @Nested
  class CreateResetPasswordTokenTests {

    @Test
    void shouldPropagateDBError_whenSaveThrowsDataAccessException() {
      // Arrange
      when(userRepository.findByEmail(forgotPasswordDto.email())).thenReturn(Optional.of(user));
      doThrow(new DataAccessException("DB error") {}).when(passwordResetTokenRepository).save(any(PasswordResetToken.class));

      // Act & Assert
      assertThrows(DataAccessException.class, () -> underTest.forgotPassword(forgotPasswordDto));
      verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
      verify(applicationEventPublisher, never()).publishEvent(any(PasswordResetEvent.class));
    }

    @Test
    void shouldSaveTokenWithCorrectProperties_whenCreatedSuccessfully() {
      // Arrange
      when(userRepository.findByEmail(forgotPasswordDto.email())).thenReturn(Optional.of(user));

      // Act
      underTest.forgotPassword(forgotPasswordDto);

      // Assert
      verify(passwordResetTokenRepository).deleteByUserId(user.getId());

      ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
      verify(passwordResetTokenRepository).save(tokenCaptor.capture());

      PasswordResetToken savedToken = tokenCaptor.getValue();
      assertThat(savedToken.getUserId()).isEqualTo(user.getId());
      assertThat(savedToken.getTokenHash()).isNotNull();
      assertThat(savedToken.getTokenHash()).isNotEmpty();
      assertThat(savedToken.getExpiresAt()).isAfter(Instant.now());
    }
  }

  @Nested
  class ResetPasswordTests {

    private ResetPasswordDto resetPasswordDto;
    private HttpServletResponse mockResponse;
    private String rawToken;
    private PasswordResetToken passwordResetToken;

    @BeforeEach
    void setUp() {
      resetPasswordDto = new ResetPasswordDto("validRawToken", "NewPassword@123");
      mockResponse = mock(HttpServletResponse.class);
      rawToken = "validRawToken";
      passwordResetToken = new PasswordResetToken();
      passwordResetToken.setId(UUID.randomUUID());
      passwordResetToken.setUserId(user.getId());
      passwordResetToken.setTokenHash(TokenHasher.sha256Hex(rawToken));
      passwordResetToken.setExpiresAt(Instant.now().plus(30, java.time.temporal.ChronoUnit.MINUTES));
    }

    @Test
    void shouldThrowInvalidToken_whenTokenHashNotFound() {
      // Arrange
      String hash = TokenHasher.sha256Hex(resetPasswordDto.rawToken());
      when(passwordResetTokenRepository.findByTokenHash(hash)).thenReturn(Optional.empty());

      // Act & Assert
      AppException ex = assertThrows(AppException.class, () -> underTest.resetPassword(mockResponse, resetPasswordDto));
      assertThat(ex.getErrorInfo()).isEqualTo(ErrorInfo.INVALID_PASSWORD_RESET_TOKEN);
      verify(passwordResetTokenRepository).findByTokenHash(hash);
      verify(passwordResetTokenRepository, never()).consume(anyString(), any());
    }

    @Test
    void shouldThrowInvalidToken_whenTokenIsExpired() {
      // Arrange
      passwordResetToken.setExpiresAt(Instant.now().minus(1, java.time.temporal.ChronoUnit.MINUTES));
      String hash = TokenHasher.sha256Hex(resetPasswordDto.rawToken());
      when(passwordResetTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(passwordResetToken));
      when(passwordResetTokenRepository.consume(eq(hash), any(Instant.class))).thenReturn(0);

      // Act & Assert
      AppException ex = assertThrows(AppException.class, () -> underTest.resetPassword(mockResponse, resetPasswordDto));
      assertThat(ex.getErrorInfo()).isEqualTo(ErrorInfo.INVALID_PASSWORD_RESET_TOKEN);
      verify(passwordResetTokenRepository).findByTokenHash(hash);
      verify(passwordResetTokenRepository).consume(eq(hash), any(Instant.class));
      verify(userService, never()).getUserById(any(UUID.class));
    }

    @Test
    void shouldThrowUserNotFound_whenUserServiceCannotFindUser() {
      // Arrange
      String hash = TokenHasher.sha256Hex(resetPasswordDto.rawToken());
      when(passwordResetTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(passwordResetToken));
      when(passwordResetTokenRepository.consume(eq(hash), any(Instant.class))).thenReturn(1);
      when(userService.getUserById(passwordResetToken.getUserId()))
          .thenThrow(new AppException(ErrorInfo.USER_NOT_FOUND));

      // Act & Assert
      AppException ex = assertThrows(AppException.class, () -> underTest.resetPassword(mockResponse, resetPasswordDto));
      assertThat(ex.getErrorInfo()).isEqualTo(ErrorInfo.USER_NOT_FOUND);
      verify(passwordResetTokenRepository).findByTokenHash(hash);
      verify(passwordResetTokenRepository).consume(eq(hash), any(Instant.class));
      verify(userService).getUserById(passwordResetToken.getUserId());
    }

    @Test
    void shouldResetPasswordAndClearCookies_whenTokenIsValid() {
      // Arrange
      String hash = TokenHasher.sha256Hex(resetPasswordDto.rawToken());
      String newEncodedPassword = "newEncodedPassword";

      when(passwordResetTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(passwordResetToken));
      when(passwordResetTokenRepository.consume(eq(hash), any(Instant.class))).thenReturn(1);
      when(userService.getUserById(passwordResetToken.getUserId())).thenReturn(user);
      when(passwordEncoder.encode(resetPasswordDto.newPassword())).thenReturn(newEncodedPassword);
      when(userRepository.save(any(User.class))).thenReturn(user);

      // Act
      underTest.resetPassword(mockResponse, resetPasswordDto);

      // Assert
      verify(passwordResetTokenRepository).findByTokenHash(hash);
      verify(passwordResetTokenRepository).consume(eq(hash), any(Instant.class));
      verify(userService).getUserById(passwordResetToken.getUserId());
      verify(passwordEncoder).encode(resetPasswordDto.newPassword());
      verify(userRepository).save(user);
      verify(passwordResetTokenRepository).deleteByUserId(user.getId());
      verify(activeRefreshTokenService).removeActiveRefreshToken(user.getId());
      verify(cookieService).deleteRefreshTokenCookie(mockResponse);
      verify(cookieService).deleteAccessTokenCookie(mockResponse);
    }

    @Test
    void shouldSetNewPasswordHash_onSavedUser() {
      // Arrange
      String hash = TokenHasher.sha256Hex(resetPasswordDto.rawToken());
      String newEncodedPassword = "newEncodedPassword";

      when(passwordResetTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(passwordResetToken));
      when(passwordResetTokenRepository.consume(eq(hash), any(Instant.class))).thenReturn(1);
      when(userService.getUserById(passwordResetToken.getUserId())).thenReturn(user);
      when(passwordEncoder.encode(resetPasswordDto.newPassword())).thenReturn(newEncodedPassword);
      when(userRepository.save(any(User.class))).thenReturn(user);

      // Act
      underTest.resetPassword(mockResponse, resetPasswordDto);

      // Assert
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());

      User savedUser = userCaptor.getValue();
      assertThat(savedUser.getPassword()).isEqualTo(newEncodedPassword);
    }

    @Test
    void shouldThrowInvalidToken_whenTokenAlreadyConsumed() {
      // Arrange - token exists and is not expired, but was already consumed (replay attack)
      String hash = TokenHasher.sha256Hex(resetPasswordDto.rawToken());
      when(passwordResetTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(passwordResetToken));
      when(passwordResetTokenRepository.consume(eq(hash), any(Instant.class))).thenReturn(0);

      // Act & Assert
      AppException ex = assertThrows(AppException.class, () -> underTest.resetPassword(mockResponse, resetPasswordDto));
      assertThat(ex.getErrorInfo()).isEqualTo(ErrorInfo.INVALID_PASSWORD_RESET_TOKEN);
      verify(passwordResetTokenRepository).findByTokenHash(hash);
      verify(passwordResetTokenRepository).consume(eq(hash), any(Instant.class));
      verify(userService, never()).getUserById(any(UUID.class));
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldDeleteStaleTokens_afterSuccessfulReset() {
      // Arrange
      String hash = TokenHasher.sha256Hex(resetPasswordDto.rawToken());
      String newEncodedPassword = "newEncodedPassword";

      when(passwordResetTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(passwordResetToken));
      when(passwordResetTokenRepository.consume(eq(hash), any(Instant.class))).thenReturn(1);
      when(userService.getUserById(passwordResetToken.getUserId())).thenReturn(user);
      when(passwordEncoder.encode(resetPasswordDto.newPassword())).thenReturn(newEncodedPassword);
      when(userRepository.save(any(User.class))).thenReturn(user);

      // Act
      underTest.resetPassword(mockResponse, resetPasswordDto);

      // Assert - verify deleteByUserId is called after successful reset to clean up remaining stale tokens
      verify(passwordResetTokenRepository).deleteByUserId(user.getId());
    }

    @Test
    void shouldPropagateDBError_whenUserSaveFails() {
      // Arrange - token is consumed but saving the user fails
      String hash = TokenHasher.sha256Hex(resetPasswordDto.rawToken());
      String newEncodedPassword = "newEncodedPassword";

      when(passwordResetTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(passwordResetToken));
      when(passwordResetTokenRepository.consume(eq(hash), any(Instant.class))).thenReturn(1);
      when(userService.getUserById(passwordResetToken.getUserId())).thenReturn(user);
      when(passwordEncoder.encode(resetPasswordDto.newPassword())).thenReturn(newEncodedPassword);
      when(userRepository.save(any(User.class))).thenThrow(new DataAccessException("DB error") {});

      // Act & Assert
      assertThrows(DataAccessException.class, () -> underTest.resetPassword(mockResponse, resetPasswordDto));
      verify(passwordResetTokenRepository).findByTokenHash(hash);
      verify(passwordResetTokenRepository).consume(eq(hash), any(Instant.class));
      verify(userRepository).save(any(User.class));
      // Stale tokens should not be cleaned up because save failed
      verify(passwordResetTokenRepository, never()).deleteByUserId(any(UUID.class));
      verify(activeRefreshTokenService, never()).removeActiveRefreshToken(any(UUID.class));
    }
  }

  @Nested
  class PurgeExpiredTests {

    @Test
    void shouldDeleteExpiredTokens_whenPurgeExpiredCalled() {
      // Act
      underTest.purgeExpired();

      // Assert
      verify(passwordResetTokenRepository).deleteByExpiresAtBefore(any(Instant.class));
    }
  }
}
