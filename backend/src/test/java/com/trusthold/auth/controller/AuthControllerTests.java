package com.trusthold.auth.controller;

import com.trusthold.auth.dtos.GoogleRegistrationRequest;
import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.auth.filters.AccessTokenFilter;
import com.trusthold.auth.handlers.OAuthFailureHandler;
import com.trusthold.auth.handlers.OAuthSuccessHandler;
import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.auth.service.AuthService;
import com.trusthold.auth.service.PasswordResetTokenService;
import com.trusthold.config.CustomAuthenticationEntryPoint;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTests {

  @MockitoBean
  private AuthService authService;

  @MockitoBean
  private PasswordResetTokenService passwordResetTokenService;

  @MockitoBean
  private AccessTokenFilter accessTokenFilter;

  @MockitoBean
  private OAuthSuccessHandler oAuthSuccessHandler;

  @MockitoBean
  private OAuthFailureHandler oAuthFailureHandler;

  @MockitoBean
  private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Nested
  class RegisterLocalUserTests {

    private RegisterUserDto registerUserDto;
    private final String REGISTER_USER_ENDPOINT = "/auth/register";

    @BeforeEach
    void setUp() {
      registerUserDto = new RegisterUserDto(
          "Test",
          "User",
          "testuser123",
          "testuser@example.com",
          "Test@Password1",
          "+1234567890",
          RegistrationRole.TENANT);
    }

    @Test
    void shouldReturnBadRequest_whenRegisterUserDtoIsNull() throws Exception {
      mockMvc.perform(post(REGISTER_USER_ENDPOINT)
              .contentType("application/json")
              .content("{}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenRegisterUserDtoIsInvalid() throws Exception {
      // Arrange
      registerUserDto = new RegisterUserDto(
          null,
          null,
          null,
          null,
          null,
          null,
          null
      );

      // Act & Assert
      mockMvc.perform(post(REGISTER_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(registerUserDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflict_whenUsernameAlreadyExists() throws Exception {
      // Arrange
      doThrow(new AppException(ErrorInfo.USERNAME_ALREADY_EXISTS))
          .when(authService)
          .registerLocalUser(any(RegisterUserDto.class));

      // Act & Assert
      mockMvc.perform(post(REGISTER_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(registerUserDto)))
          .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
      // Arrange
      doThrow(new AppException(ErrorInfo.EMAIL_ALREADY_EXISTS))
          .when(authService)
          .registerLocalUser(any(RegisterUserDto.class));

      // Act & Assert
      mockMvc.perform(post(REGISTER_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(registerUserDto)))
          .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn500InternalServerError_whenDatabaseErrorOccurs() throws Exception {
      // Arrange
      doThrow(new RuntimeException("Database error"))
          .when(authService)
          .registerLocalUser(any(RegisterUserDto.class));

      // Act & Assert
      mockMvc.perform(post(REGISTER_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(registerUserDto)))
          .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnCreated_whenRegistrationIsSuccessful() throws Exception {
      mockMvc.perform(post(REGISTER_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(registerUserDto)))
          .andExpect(status().isCreated());

      verify(authService).registerLocalUser(any(RegisterUserDto.class));
    }
  }

  @Nested
  class BeginGoogleRegistrationTests{

    private final String GOOGLE_REGISTER_ENDPOINT = "/auth/oauth/register";

    @Test
    void shouldReturnBadRequest_whenRequestBodyIsEmpty() throws Exception {
      // Arrange - send empty body to simulate missing request body
      mockMvc.perform(post(GOOGLE_REGISTER_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenRoleIsNull() throws Exception {
      // Arrange - send empty JSON object (role is @NotNull)
      mockMvc.perform(post(GOOGLE_REGISTER_ENDPOINT)
              .contentType("application/json")
              .content("{}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenRoleIsAdmin() throws Exception {
      // Arrange
      GoogleRegistrationRequest request = new GoogleRegistrationRequest(Role.ADMIN);

      // Act & Assert - ADMIN is not allowed for registration
      mockMvc.perform(post(GOOGLE_REGISTER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOk_whenRoleIsTenant() throws Exception {
      // Arrange
      GoogleRegistrationRequest request = new GoogleRegistrationRequest(Role.TENANT);

      // Act & Assert
      mockMvc.perform(post(GOOGLE_REGISTER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());
    }

    @Test
    void shouldReturnOk_whenRoleIsLandlord() throws Exception {
      // Arrange
      GoogleRegistrationRequest request = new GoogleRegistrationRequest(Role.LANDLORD);

      // Act & Assert
      mockMvc.perform(post(GOOGLE_REGISTER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequest_whenJsonIsMalformed() throws Exception {
      // Arrange - send malformed JSON
      String malformedJson = "{role: TENANT}";

      // Act & Assert
      mockMvc.perform(post(GOOGLE_REGISTER_ENDPOINT)
              .contentType("application/json")
              .content(malformedJson))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenRoleFieldIsNull() throws Exception {
      // Arrange - explicitly set role to null
      mockMvc.perform(post(GOOGLE_REGISTER_ENDPOINT)
              .contentType("application/json")
              .content("{\"role\": null}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSetSessionAttribute_whenRegistrationIsSuccessful() throws Exception {
      // Arrange
      GoogleRegistrationRequest request = new GoogleRegistrationRequest(Role.TENANT);

      // Act & Assert - verify session attribute is set
      mockMvc.perform(post(GOOGLE_REGISTER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.request().sessionAttribute("registrationRole", "TENANT"));
    }

    @Test
    void shouldReturnUnsupportedMediaType_whenContentTypeIsMissing() throws Exception {
      // Arrange
      GoogleRegistrationRequest request = new GoogleRegistrationRequest(Role.TENANT);

      // Act & Assert - no content type header returns 415 Unsupported Media Type
      mockMvc.perform(post(GOOGLE_REGISTER_ENDPOINT)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnsupportedMediaType());
    }
  }
}
