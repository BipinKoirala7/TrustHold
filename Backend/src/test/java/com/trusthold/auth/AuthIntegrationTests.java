package com.trusthold.auth;

import com.trusthold.TestcontainersConfiguration;
import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.shared.dtos.ApiResponse;
import com.trusthold.user.model.AuthProvider;
import com.trusthold.user.model.User;
import com.trusthold.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@AutoConfigureTestRestTemplate
public class AuthIntegrationTests {

  @Autowired
  TestRestTemplate restTemplate;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @MockitoSpyBean
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  private ResponseEntity<ApiResponse<Void>> post(String endpoint, HttpHeaders headers, Object body) {
    return restTemplate.exchange(endpoint, HttpMethod.POST,
        new HttpEntity<>(body, headers), new ParameterizedTypeReference<>() {});
  }


  @Nested
  class RegisterLocalUserIntegrationTests {

    private final String REGISTER_USER_ENDPOINT = "/api/v1/auth/register";
    private RegisterUserDto validRegisterUserDto;

    private ResponseEntity<ApiResponse<Void>> register(RegisterUserDto dto) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      return post(REGISTER_USER_ENDPOINT, headers, dto);
    }

    @BeforeEach
    void setUp() {
      validRegisterUserDto = new RegisterUserDto(
          "Bipin",
          "Koirala",
          "bipin123",
          "bipin@example.com",
          "Password123!",
          RegistrationRole.TENANT);
    }

    /**
     * Test cases for valid input
     */
    @Test
    void shouldReturnCreated_whenRegisterUserDtoIsValid() {
      // Arrange & Act
      ResponseEntity<ApiResponse<Void>> response = register(validRegisterUserDto);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().data()).isNull();
      assertThat(response.getBody().message()).isEqualTo("User registered successfully");
      assertThat(response.getBody().success()).isTrue();
    }

    @Test
    void shouldPersistUserWithHashedPassword_whenRegistrationSucceeds() {
      // Arrange
      register(validRegisterUserDto);

      // Act
      List<User> users = userRepository.findAll();

      // Assert
      assertThat(users).hasSize(1);
      assertThat(users.getFirst()).isNotNull();

      User saved = users.getFirst();
      assertThat(saved.getUsername()).isEqualTo("bipin123");
      assertThat(saved.getEmail()).isEqualTo("bipin@example.com");
      assertThat(saved.getAuthProvider()).isEqualTo(AuthProvider.LOCAL);
      assertThat(saved.getLastLogin()).isNull(); // never logged in yet
      assertThat(saved.getCreatedAt()).isNotNull();
      assertThat(saved.getPassword()).isNotEqualTo("Password123!"); // not stored in plain text
      assertThat(passwordEncoder.matches("Password123!", saved.getPassword())).isTrue();
    }

    /**
     * Test cases for invalid input
     */
    @Test
    void shouldReturnBadRequest_whenRegisterUserDtoIsNull() {
      // Arrange & Act
      ResponseEntity<ApiResponse<Void>> response = register(null);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnBadRequest_whenRegisterUserDtoIsInvalid() {
      // Arrange
      validRegisterUserDto = new RegisterUserDto(
          "",
          "user",
          "testuser",
          "invalid-email",
          "short",
          RegistrationRole.TENANT
      );

      // Act
      ResponseEntity<ApiResponse<Void>> response = register(validRegisterUserDto);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().message()).isEqualTo("Invalid input provided");
      assertThat(userRepository.count()).isZero();
    }

    /**
     * Test case for conflicts
     */
    @Test
    void shouldReturnConflict_whenEmailAlreadyExists() {
      // Arrange
      assertThat(register(validRegisterUserDto).getStatusCode()).isEqualTo(HttpStatus.CREATED);
      validRegisterUserDto = new RegisterUserDto(
          validRegisterUserDto.firstName(),
          validRegisterUserDto.lastName(),
          "testuser",
          validRegisterUserDto.email(),
          validRegisterUserDto.password(),
          validRegisterUserDto.registrationRole()
      );

      // Act
      ResponseEntity<ApiResponse<Void>> response = register(validRegisterUserDto);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().message()).isEqualTo("A User with this email already exists");
      assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldReturnConflict_whenUsernameAlreadyExists() {
      // Arrange
      assertThat(register(validRegisterUserDto).getStatusCode()).isEqualTo(HttpStatus.CREATED);
      validRegisterUserDto = new RegisterUserDto(
          validRegisterUserDto.firstName(),
          validRegisterUserDto.lastName(),
          validRegisterUserDto.username(),
          "testuser@example.com",   // Using a different email
          validRegisterUserDto.password(),
          validRegisterUserDto.registrationRole()
      );

      // Act
      ResponseEntity<ApiResponse<Void>> response = register(validRegisterUserDto);

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().message()).isEqualTo("Username is already taken");
      assertThat(userRepository.count()).isEqualTo(1);
    }

    /**
     * Test case for when the database throws an error.
     */
    @Test
    void shouldRejectDuplicate_whenExistsChecksPassButDbConstraintFires() {
      // Simulates a race: two requests both pass the exists-checks, the DB unique constraint catches the second.
      register(validRegisterUserDto);
      doReturn(false).when(userRepository).existsByUsername(any());
      doReturn(false).when(userRepository).existsByEmail(any());

      ResponseEntity<ApiResponse<Void>> response = register(validRegisterUserDto);

      // Currently mapped by handleDataIntegrityViolationException
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().message()).isEqualTo("Request conflicts with existing data");
      assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldReturnInternalServerError_whenDatabaseIsUnavailable() {
      doThrow(new DataAccessResourceFailureException("db down")).when(userRepository).save(any());

      ResponseEntity<ApiResponse<Void>> response = register(validRegisterUserDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred. Please try again later.");
    }
  }
}
