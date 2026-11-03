package com.trusthold.auth.integration;

import com.trusthold.auth.dtos.ForgotPasswordDto;
import com.trusthold.auth.dtos.LoginUserDto;
import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.auth.dtos.ResetPasswordDto;
import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.auth.repository.ActiveRefreshTokenRepository;
import com.trusthold.auth.repository.PasswordResetTokenRepository;
import com.trusthold.mail.EmailService;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.dtos.ApiResponse;
import com.trusthold.shared.dtos.IntegrationTestSupport;
import com.trusthold.user.model.AuthProvider;
import com.trusthold.user.model.User;
import com.trusthold.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthIntegrationTests extends IntegrationTestSupport {

  private final String username = "bipin123";
  private final String email = "bipin@example.com";
  private final String password = "Password123!";

  @Autowired
  private PasswordEncoder passwordEncoder;

  @MockitoSpyBean
  private UserRepository userRepository;

  @MockitoSpyBean
  private ActiveRefreshTokenRepository activeRefreshTokenRepository;

  @MockitoSpyBean
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @MockitoSpyBean
  private EmailService emailService;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();

    String firstName = "Bipin";
    String lastName = "Koirala";
    String phone = "+1234567890";

    validRegisterUserDto = new RegisterUserDto(firstName, lastName, username, email, password, phone, RegistrationRole.TENANT);
    validLoginUserDto = new LoginUserDto(email, password);

    assertThat(register(validRegisterUserDto).getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
  }

  @Nested
  class RegisterLocalUserTests {

    @Test
    void shouldReturnCreatedAndPersistUserWithHashedPassword_whenRegistrationSucceeds() {
      List<User> users = userRepository.findAll();
      assertThat(users).hasSize(1);

      User saved = users.getFirst();
      assertThat(saved.getUsername()).isEqualTo(username);
      assertThat(saved.getEmail()).isEqualTo(email);
      assertThat(saved.getAuthProvider()).isEqualTo(AuthProvider.LOCAL);
      assertThat(saved.getLastLogin()).isNull(); // never logged in yet
      assertThat(saved.getCreatedAt()).isNotNull();
      assertThat(passwordEncoder.matches(password, saved.getPassword())).isTrue();
      assertThat(saved.getPassword()).isNotEqualTo(password); // not stored in plain text
    }

    // One conflict test: proves the real existsBy... queries and the 409 mapping work together.
    // The per-field branches are owned by AuthServiceTests; the status mapping by AuthControllerTests.
    @Test
    void shouldReturnConflict_whenEmailAlreadyExists() {
      RegisterUserDto sameEmailDifferentUsername = new RegisterUserDto(
          validRegisterUserDto.firstName(),
          validRegisterUserDto.lastName(),
          "testuser",
          validRegisterUserDto.email(),
          validRegisterUserDto.password(),
          validRegisterUserDto.phone(),
          validRegisterUserDto.registrationRole()
      );

      ResponseEntity<ApiResponse<Void>> response = register(sameEmailDifferentUsername);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error()).isNotNull();
      assertThat(response.getBody().error().message()).isEqualTo("Email is already registered");
      assertThat(userRepository.count()).isEqualTo(1);
    }

    // Simulates a race: two requests both pass the exists-checks, and the DB unique constraint
    // catches the second. Only a real database can prove this.
    @Test
    void shouldRejectDuplicate_whenExistsChecksPassButDbConstraintFires() {
      register(validRegisterUserDto);
      doReturn(false).when(userRepository).existsByUsername(any());
      doReturn(false).when(userRepository).existsByEmail(any());

      ResponseEntity<ApiResponse<Void>> response = register(validRegisterUserDto);

      // Pins CURRENT behavior (handleDataIntegrityViolationException -> 400).
      // A duplicate-key race is arguably a 409; if the handler is changed, update this assertion.
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error()).isNotNull();
      assertThat(response.getBody().error().message()).isEqualTo("Request conflicts with existing data");
      assertThat(userRepository.count()).isEqualTo(1);
    }
  }

  @Nested
  class LoginLocalUserTests{

    @BeforeEach
    void setUp() {
      validLoginUserDto = new LoginUserDto(email, password);
    }

    @Test
    void shouldLoginLocalUser() {
      ResponseEntity<ApiResponse<Void>> response = login(validLoginUserDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().data()).isNull();
      assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldFailLogin_whenInvalidCredentials() {
      validLoginUserDto = new LoginUserDto(email, "invalidPassword");
      ResponseEntity<ApiResponse<Void>> response = login(validLoginUserDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error()).isNotNull();
      assertThat(response.getBody().error().message()).isEqualTo("Invalid credentials");
      assertThat(userRepository.count()).isEqualTo(1);
    }
  }

  @Nested
  class TokenRefreshTests {

    private final String TOKEN_REFRESH_PATH = "/auth/token/refresh";

    @BeforeEach
    void setUp() {
      validLoginUserDto = new LoginUserDto(email, password);
    }

    @AfterEach
    void tearDown(){
      activeRefreshTokenRepository.deleteAll();
    }

    @Test
    void shouldRefreshToken() {
      ResponseEntity<ApiResponse<Void>> response = post(TOKEN_REFRESH_PATH, getAuthenticatedHeaders(), null);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldFailTokenRefresh_whenUserIsNotAuthenticated() {
      ResponseEntity<ApiResponse<Void>> response = post(TOKEN_REFRESH_PATH, getHeaders(), null);

      assertThat(response.getStatusCode()).isEqualTo(ErrorInfo.INVALID_TOKEN.getHttpStatus());
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error()).isNotNull();
      assertThat(response.getBody().error().message()).isEqualTo(ErrorInfo.INVALID_TOKEN.getMessage());
    }
  }

  @Nested
  class ResetPasswordTests{

    @Captor
    private ArgumentCaptor<String> rawTokenCaptor;

    private final String newPassword = "NewPassword@123";
    private final String RESET_PASSWORD_PATH = "/auth/reset-password";
    private ForgotPasswordDto validForgotPasswordDto;
    private ResetPasswordDto validResetPasswordDto;

    @BeforeEach
    void setUp(){
      validForgotPasswordDto = new ForgotPasswordDto(email);
      validResetPasswordDto = new ResetPasswordDto(getForgotPasswordToken(), newPassword);
    }


    private String getForgotPasswordToken(){
      ResponseEntity<ApiResponse<Void>> response = post("/auth/forgot-password", getHeaders(), validForgotPasswordDto);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

      verify(emailService, timeout(2000)).sendPasswordResetEmail(eq(email), any(), rawTokenCaptor.capture());
      return rawTokenCaptor.getValue();
    }

    @Test
    void shouldResetPassword() {
      ResponseEntity<ApiResponse<Void>> response = post(RESET_PASSWORD_PATH, getHeaders(), validResetPasswordDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(userRepository.count()).isEqualTo(1);
      assertTrue(passwordEncoder.matches(newPassword, userRepository.findByEmail(email).get().getPassword()));
    }

    @Test
    void shouldFail_whenTokenIsExpired(){
      doReturn(0).when(passwordResetTokenRepository).consume(any(), any());

      ResponseEntity<ApiResponse<Void>> response = post(RESET_PASSWORD_PATH, getHeaders(), validResetPasswordDto);

      assertThat(response.getStatusCode()).isEqualTo(ErrorInfo.EXPIRED_PASSWORD_RESET_TOKEN.getHttpStatus());
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error().message()).isEqualTo(ErrorInfo.EXPIRED_PASSWORD_RESET_TOKEN.getMessage());
    }
  }

  @Nested
  class LogOutUserTests{

    private final String LOG_OUT_PATH = "/auth/logout";

    @BeforeEach
    void setUp(){
      validLoginUserDto = new LoginUserDto(email, password);
    }

    @Test
    void shouldLogOutUser(){
      ResponseEntity<ApiResponse<Void>> response = post(LOG_OUT_PATH, getAuthenticatedHeaders(), null);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().data()).isNull();
    }

    @Test
    void shouldFail_whenUserIsNotLoggedIn(){
      ResponseEntity<ApiResponse<Void>> response = post(LOG_OUT_PATH, getHeaders(), null);

      assertThat(response.getStatusCode()).isEqualTo(ErrorInfo.UNAUTHORIZED.getHttpStatus());
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error().message()).isEqualTo(ErrorInfo.UNAUTHORIZED.getMessage());
    }
  }
}
