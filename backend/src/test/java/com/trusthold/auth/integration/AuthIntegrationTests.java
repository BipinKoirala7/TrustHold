package com.trusthold.auth.integration;

import com.trusthold.auth.dtos.LoginUserDto;
import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.shared.dtos.ApiResponse;
import com.trusthold.shared.dtos.IntegrationTestSupport;
import com.trusthold.user.model.AuthProvider;
import com.trusthold.user.model.User;
import com.trusthold.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

public class AuthIntegrationTests extends IntegrationTestSupport {

  @Autowired
  private PasswordEncoder passwordEncoder;

  @MockitoSpyBean
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  // Info
  private String username = "bipin123";
  private String email = "bipin@example.com";
  private String password = "Password123!";

  private final String REGISTER_USER_ENDPOINT = "/auth/register";

  private RegisterUserDto validRegisterUserDto;

  @Nested
  class RegisterLocalUserTests {

    @BeforeEach
    void setUp() {
      validRegisterUserDto = new RegisterUserDto(
          "Bipin", "Koirala",
          username, email, password, "+1234567890", RegistrationRole.TENANT);
    }

    @AfterEach
    void tearDown() {
      userRepository.deleteAll();
    }

    @Test
    void shouldReturnCreatedAndPersistUserWithHashedPassword_whenRegistrationSucceeds() {
      ResponseEntity<ApiResponse<Void>> response = register(validRegisterUserDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().data()).isNull();
      assertThat(response.getBody().error()).isNull();

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
      assertThat(register(validRegisterUserDto).getStatusCode()).isEqualTo(HttpStatus.CREATED);
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
      // A duplicate-key race is arguably a 409; if you change the handler, update this assertion.
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().error()).isNotNull();
      assertThat(response.getBody().error().message()).isEqualTo("Request conflicts with existing data");
      assertThat(userRepository.count()).isEqualTo(1);
    }
  }

  @Nested
  class LoginLocalUserTests{

    private LoginUserDto validLoginUserDto;

    @BeforeEach
    void setUp() {
      validRegisterUserDto = new RegisterUserDto(
          "Bipin", "Koirala",
          username, email, password, "+1234567890", RegistrationRole.TENANT);
      register(validRegisterUserDto);
      validLoginUserDto = new LoginUserDto(email, password);
    }

    @AfterEach
    void tearDown(){
      userRepository.deleteAll();
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
}
