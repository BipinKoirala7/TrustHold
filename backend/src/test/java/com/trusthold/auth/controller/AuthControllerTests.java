package com.trusthold.auth.controller;

import com.trusthold.auth.dtos.*;
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
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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

  @Nested
  class LoginTests {

    private LoginUserDto loginUserDto;
    private final String LOGIN_ENDPOINT = "/auth/login";

    @BeforeEach
    void setUp() {
      loginUserDto = new LoginUserDto("testuser@example.com", "Test@Password1");
    }

    @Test
    void shouldReturnBadRequest_whenRequestBodyIsNull() throws Exception {
      // Arrange & Act — send empty JSON object so @Valid fails on required fields
      mockMvc.perform(post(LOGIN_ENDPOINT)
              .contentType("application/json")
              .content("{}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenRequestBodyIsInvalid() throws Exception {
      // Arrange — missing password field
      LoginUserDto invalidDto = new LoginUserDto("testuser@example.com", null);

      // Act & Assert
      mockMvc.perform(post(LOGIN_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(invalidDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorized_whenUserDoesNotExist() throws Exception {
      // Arrange
      doThrow(new AppException(ErrorInfo.UNAUTHORIZED))
          .when(authService)
          .loginUser(any(LoginUserDto.class), any());

      // Act & Assert
      mockMvc.perform(post(LOGIN_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(loginUserDto)))
          .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorized_whenPasswordDoesNotMatch() throws Exception {
      // Arrange
      doThrow(new AppException(ErrorInfo.INVALID_CREDENTIALS))
          .when(authService)
          .loginUser(any(LoginUserDto.class), any());

      // Act & Assert
      mockMvc.perform(post(LOGIN_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(loginUserDto)))
          .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnInternalServerError_whenActiveRefreshTokenServiceFails() throws Exception {
      // Arrange — simulates a DB failure when storing the refresh token
      doThrow(new RuntimeException("Database error"))
          .when(authService)
          .loginUser(any(LoginUserDto.class), any());

      // Act & Assert
      mockMvc.perform(post(LOGIN_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(loginUserDto)))
          .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnInternalServerError_whenUpdateLastLoginTimeFails() throws Exception {
      // Arrange — simulates a DB failure when updating last login time
      doThrow(new RuntimeException("Database error"))
          .when(authService)
          .loginUser(any(LoginUserDto.class), any());

      // Act & Assert
      mockMvc.perform(post(LOGIN_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(loginUserDto)))
          .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnOk_whenLoginIsSuccessful() throws Exception {
      // Act & Assert
      mockMvc.perform(post(LOGIN_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(loginUserDto)))
          .andExpect(status().isOk());

      verify(authService).loginUser(any(LoginUserDto.class), any());
    }

    @Test
    void shouldReturnBadRequest_whenPasswordIsTooShort() throws Exception {
      // Arrange — password shorter than 8 characters
      LoginUserDto shortPasswordDto = new LoginUserDto("testuser@example.com", "Ab1@");

      // Act & Assert
      mockMvc.perform(post(LOGIN_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(shortPasswordDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenPasswordMissingSpecialCharacter() throws Exception {
      // Arrange — password without special character from @$!%*?&
      LoginUserDto noSpecialCharDto = new LoginUserDto("testuser@example.com", "Abcd1234");

      // Act & Assert
      mockMvc.perform(post(LOGIN_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(noSpecialCharDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenEmailFormatIsInvalid() throws Exception {
      // Arrange — invalid email format
      LoginUserDto invalidEmailDto = new LoginUserDto("not-an-email", "Test@Password1");

      // Act & Assert
      mockMvc.perform(post(LOGIN_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(invalidEmailDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenEmailIsEmpty() throws Exception {
      // Arrange — empty email field
      LoginUserDto emptyEmailDto = new LoginUserDto("", "Test@Password1");

      // Act & Assert
      mockMvc.perform(post(LOGIN_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(emptyEmailDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenJsonIsMalformed() throws Exception {
      // Arrange — malformed JSON with unquoted keys
      String malformedJson = "{email: test@example.com, password: Test@Password1}";

      // Act & Assert
      mockMvc.perform(post(LOGIN_ENDPOINT)
              .contentType("application/json")
              .content(malformedJson))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnsupportedMediaType_whenContentTypeIsMissing() throws Exception {
      // Act & Assert — no content type header returns 415
      mockMvc.perform(post(LOGIN_ENDPOINT)
              .content(objectMapper.writeValueAsString(loginUserDto)))
          .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void shouldReturnUnauthorized_whenOAuthUserTriesToLocalLogin() throws Exception {
      // Arrange — OAuth user (e.g., Google) tries to log in with local credentials
      doThrow(new AppException(ErrorInfo.INVALID_CREDENTIALS))
          .when(authService)
          .loginUser(any(LoginUserDto.class), any());

      // Act & Assert
      mockMvc.perform(post(LOGIN_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(loginUserDto)))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  class TokenRefreshTests {

    private final String REFRESH_TOKEN_ENDPOINT = "/auth/token/refresh";

    @Test
    void shouldReturnInternalServerError_whenRefreshTokenIsNull() throws Exception {
      // Arrange — refresh token cookie is missing, so cookieService returns null
      doThrow(new JwtException("Please Log in"))
          .when(authService)
          .refreshToken(any(), any());

      // Act & Assert
      mockMvc.perform(post(REFRESH_TOKEN_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnInternalServerError_whenRefreshTokenIsNotActive() throws Exception {
      // Arrange — refresh token exists but is not active in the database
      doThrow(new JwtException("Refresh token is not active"))
          .when(authService)
          .refreshToken(any(), any());

      // Act & Assert
      mockMvc.perform(post(REFRESH_TOKEN_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnInternalServerError_whenRefreshTokenIsInvalidOrMalformed() throws Exception {
      // Arrange — refresh token is malformed (e.g., expired, tampered, wrong signature)
      doThrow(new JwtException("Invalid signature"))
          .when(authService)
          .refreshToken(any(), any());

      // Act & Assert
      mockMvc.perform(post(REFRESH_TOKEN_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnInternalServerError_whenDatabaseErrorOccurs() throws Exception {
      // Arrange — simulates a DB failure when storing/removing the refresh token via ActiveRefreshTokenService
      doThrow(new RuntimeException("Database error"))
          .when(authService)
          .refreshToken(any(), any());

      // Act & Assert
      mockMvc.perform(post(REFRESH_TOKEN_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnOk_whenTokenRefreshIsSuccessful() throws Exception {
      // Act & Assert
      mockMvc.perform(post(REFRESH_TOKEN_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isOk());

      verify(authService).refreshToken(any(), any());
    }

    @Test
    void shouldReturnOk_whenTokenCookiesAreSet() throws Exception {
      // Act & Assert — in @WebMvcTest with mocked service, cookies are not actually set by the mock,
      // so we verify the service was called to perform the refresh, which would set cookies in production
      mockMvc.perform(post(REFRESH_TOKEN_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isOk());

      verify(authService).refreshToken(any(), any());
    }

    @Test
    void shouldReturnInternalServerError_whenRefreshTokenIsExpired() throws Exception {
      // Arrange — specifically test ExpiredJwtException (subclass of JwtException)
      doThrow(new ExpiredJwtException(null, null, "JWT expired"))
          .when(authService)
          .refreshToken(any(), any());

      // Act & Assert
      mockMvc.perform(post(REFRESH_TOKEN_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnInternalServerError_whenTokenIsNotRefreshType() throws Exception {
      // Arrange — an access token is passed as a refresh token
      doThrow(new JwtException("Not a refresh token"))
          .when(authService)
          .refreshToken(any(), any());

      // Act & Assert
      mockMvc.perform(post(REFRESH_TOKEN_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnInternalServerError_whenUserNotFound() throws Exception {
      // Arrange — token is valid but user no longer exists in DB
      doThrow(new RuntimeException("User not found"))
          .when(authService)
          .refreshToken(any(), any());

      // Act & Assert
      mockMvc.perform(post(REFRESH_TOKEN_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnMethodNotAllowed_whenGetRequest() throws Exception {
      // Act & Assert — GET request to POST-only endpoint returns 405
      mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(REFRESH_TOKEN_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isMethodNotAllowed());
    }
  }

  @Nested
  class ForgotPasswordTests {

    private ForgotPasswordDto forgotPasswordDto;
    private final String FORGOT_PASSWORD_ENDPOINT = "/auth/forgot-password";

    @BeforeEach
    void setUp() {
      forgotPasswordDto = new ForgotPasswordDto("testuser@example.com");
    }

    @Test
    void shouldReturnNotFound_whenDtoEmailIsNull() throws Exception {
      // Arrange — @Email allows null; service would throw USER_NOT_FOUND for null email
      doThrow(new AppException(ErrorInfo.USER_NOT_FOUND))
          .when(passwordResetTokenService)
          .forgotPassword(any(ForgotPasswordDto.class));

      // Act & Assert
      mockMvc.perform(post(FORGOT_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content("{}"))
          .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequest_whenEmailIsInvalid() throws Exception {
      // Arrange — invalid email format
      ForgotPasswordDto invalidDto = new ForgotPasswordDto("not-an-email");

      // Act & Assert
      mockMvc.perform(post(FORGOT_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(invalidDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFound_whenUserNotFound() throws Exception {
      // Arrange
      doThrow(new AppException(ErrorInfo.USER_NOT_FOUND))
          .when(passwordResetTokenService)
          .forgotPassword(any(ForgotPasswordDto.class));

      // Act & Assert
      mockMvc.perform(post(FORGOT_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(forgotPasswordDto)))
          .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnOk_whenForgotPasswordIsSuccessful() throws Exception {
      // Act & Assert
      mockMvc.perform(post(FORGOT_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(forgotPasswordDto)))
          .andExpect(status().isOk());

      verify(passwordResetTokenService).forgotPassword(any(ForgotPasswordDto.class));
    }

    @Test
    void shouldReturnNotFound_whenEmailIsEmpty() throws Exception {
      // Arrange — @Email allows empty strings; service would throw USER_NOT_FOUND for empty email
      doThrow(new AppException(ErrorInfo.USER_NOT_FOUND))
          .when(passwordResetTokenService)
          .forgotPassword(any(ForgotPasswordDto.class));

      ForgotPasswordDto emptyEmailDto = new ForgotPasswordDto("");

      // Act & Assert
      mockMvc.perform(post(FORGOT_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(emptyEmailDto)))
          .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequest_whenJsonIsMalformed() throws Exception {
      // Arrange — malformed JSON with unquoted keys
      String malformedJson = "{email: test@example.com}";

      // Act & Assert
      mockMvc.perform(post(FORGOT_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(malformedJson))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnsupportedMediaType_whenContentTypeIsMissing() throws Exception {
      // Act & Assert — no content type header returns 415
      mockMvc.perform(post(FORGOT_PASSWORD_ENDPOINT)
              .content(objectMapper.writeValueAsString(forgotPasswordDto)))
          .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void shouldReturnInternalServerError_whenDatabaseErrorOccurs() throws Exception {
      // Arrange — simulates a DB failure when creating the reset token
      doThrow(new RuntimeException("Database error"))
          .when(passwordResetTokenService)
          .forgotPassword(any(ForgotPasswordDto.class));

      // Act & Assert
      mockMvc.perform(post(FORGOT_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(forgotPasswordDto)))
          .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnMethodNotAllowed_whenGetRequest() throws Exception {
      // Act & Assert — GET request to POST-only endpoint returns 405
      mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(FORGOT_PASSWORD_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void shouldReturnBadRequest_whenEmailHasWhitespace() throws Exception {
      // Arrange — email with leading/trailing whitespace
      ForgotPasswordDto whitespaceDto = new ForgotPasswordDto("  test@example.com  ");

      // Act & Assert — @Email validation rejects whitespace-containing emails
      mockMvc.perform(post(FORGOT_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(whitespaceDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenEmailMissingDomain() throws Exception {
      // Arrange — email missing the domain part
      ForgotPasswordDto missingDomainDto = new ForgotPasswordDto("user@");

      // Act & Assert — @Email validation rejects incomplete email
      mockMvc.perform(post(FORGOT_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(missingDomainDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenEmailHasNoAtSymbol() throws Exception {
      // Arrange — email without @ symbol
      ForgotPasswordDto noAtDto = new ForgotPasswordDto("userdomain.com");

      // Act & Assert — @Email validation rejects email without @ symbol
      mockMvc.perform(post(FORGOT_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(noAtDto)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  class ResetPasswordTests {

    private ResetPasswordDto resetPasswordDto;
    private final String RESET_PASSWORD_ENDPOINT = "/auth/reset-password";

    @BeforeEach
    void setUp() {
      resetPasswordDto = new ResetPasswordDto("valid-token-123", "NewPass@1234");
    }

    @Test
    void shouldReturnBadRequest_whenDtoIsNull() throws Exception {
      // Arrange — send empty JSON object so @Valid fails on required fields
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content("{}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenDtoFieldsAreInvalid() throws Exception {
      // Arrange — invalid token (blank) and password without special character
      ResetPasswordDto invalidDto = new ResetPasswordDto("", "Abcd1234");

      // Act & Assert
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(invalidDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenTokenNotFound() throws Exception {
      // Arrange — token hash does not exist in the database
      doThrow(new AppException(ErrorInfo.INVALID_PASSWORD_RESET_TOKEN))
          .when(passwordResetTokenService)
          .resetPassword(any(), any(ResetPasswordDto.class));

      // Act & Assert
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(resetPasswordDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenTokenIsExpired() throws Exception {
      // Arrange — token exists but has expired (consume returns 0 rows)
      doThrow(new AppException(ErrorInfo.INVALID_PASSWORD_RESET_TOKEN))
          .when(passwordResetTokenService)
          .resetPassword(any(), any(ResetPasswordDto.class));

      // Act & Assert
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(resetPasswordDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnInternalServerError_whenDatabaseErrorOccurs() throws Exception {
      // Arrange — simulates a DB failure when consuming the token or updating the password
      doThrow(new RuntimeException("Database error"))
          .when(passwordResetTokenService)
          .resetPassword(any(), any(ResetPasswordDto.class));

      // Act & Assert
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(resetPasswordDto)))
          .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnOk_whenResetPasswordIsSuccessful() throws Exception {
      // Act & Assert
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(resetPasswordDto)))
          .andExpect(status().isOk());

      verify(passwordResetTokenService).resetPassword(any(), any(ResetPasswordDto.class));
    }

    @Test
    void shouldReturnBadRequest_whenPasswordIsTooShort() throws Exception {
      // Arrange — password shorter than 8 characters
      ResetPasswordDto shortPasswordDto = new ResetPasswordDto("valid-token-123", "Ab1@");

      // Act & Assert
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(shortPasswordDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenPasswordMissingSpecialCharacter() throws Exception {
      // Arrange — password without special character from @$!%*?&
      ResetPasswordDto noSpecialCharDto = new ResetPasswordDto("valid-token-123", "Abcd1234");

      // Act & Assert
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(noSpecialCharDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenPasswordMissingUppercase() throws Exception {
      // Arrange — password without uppercase letter
      ResetPasswordDto noUppercaseDto = new ResetPasswordDto("valid-token-123", "abcd1234@");

      // Act & Assert
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(noUppercaseDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenPasswordMissingLowercase() throws Exception {
      // Arrange — password without lowercase letter
      ResetPasswordDto noLowercaseDto = new ResetPasswordDto("valid-token-123", "ABCD1234@");

      // Act & Assert
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(noLowercaseDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenPasswordMissingNumber() throws Exception {
      // Arrange — password without number
      ResetPasswordDto noNumberDto = new ResetPasswordDto("valid-token-123", "Abcdefg@");

      // Act & Assert
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(noNumberDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenTokenIsEmpty() throws Exception {
      // Arrange — empty token string
      ResetPasswordDto emptyTokenDto = new ResetPasswordDto("", "NewPass@1234");

      // Act & Assert
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(emptyTokenDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenJsonIsMalformed() throws Exception {
      // Arrange — malformed JSON with unquoted keys
      String malformedJson = "{rawToken: valid-token, newPassword: NewPass@1234}";

      // Act & Assert
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(malformedJson))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnsupportedMediaType_whenContentTypeIsMissing() throws Exception {
      // Act & Assert — no content type header returns 415
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .content(objectMapper.writeValueAsString(resetPasswordDto)))
          .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void shouldReturnMethodNotAllowed_whenGetRequest() throws Exception {
      // Act & Assert — GET request to POST-only endpoint returns 405
      mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isMethodNotAllowed());
    }

    // --- Additional suggested tests ---

    @Test
    void shouldReturnBadRequest_whenPasswordExceedsMaxLength() throws Exception {
      // Arrange — password longer than 100 characters violates @Size(max = 100)
      String longPassword = "A" + "a".repeat(97) + "1@"; // 101 characters
      ResetPasswordDto longPasswordDto = new ResetPasswordDto("valid-token-123", longPassword);

      // Act & Assert
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(longPasswordDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenNewPasswordIsNull() throws Exception {
      // Arrange — explicitly set newPassword to null (violates @NotBlank)
      ResetPasswordDto nullPasswordDto = new ResetPasswordDto("valid-token-123", null);

      // Act & Assert
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(nullPasswordDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenRawTokenIsNull() throws Exception {
      // Arrange — explicitly set rawToken to null (violates @NotBlank)
      ResetPasswordDto nullTokenDto = new ResetPasswordDto(null, "NewPass@1234");

      // Act & Assert
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(nullTokenDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOk_whenServiceIsCalledWithCorrectDto() throws Exception {
      // Act
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(resetPasswordDto)))
          .andExpect(status().isOk());

      // Assert — verify the exact DTO is passed through, not just any()
      verify(passwordResetTokenService).resetPassword(any(), org.mockito.ArgumentMatchers.eq(resetPasswordDto));
    }

    @Test
    void shouldReturnInternalServerError_whenUserServiceFails() throws Exception {
      // Arrange — simulates userService.getUserById() failing inside resetPassword()
      // (e.g., user was deleted between token creation and reset)
      doThrow(new RuntimeException("User not found"))
          .when(passwordResetTokenService)
          .resetPassword(any(), any(ResetPasswordDto.class));

      // Act & Assert
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(resetPasswordDto)))
          .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnBadRequest_whenPasswordContainsDisallowedSpecialChar() throws Exception {
      // Arrange — password with disallowed special character '#' (only @$!%*?& are allowed)
      ResetPasswordDto disallowedSpecialCharDto = new ResetPasswordDto("valid-token-123", "Aa1#test");

      // Act & Assert
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(disallowedSpecialCharDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOk_whenPasswordIsMinimumValidLength() throws Exception {
      // Arrange — minimum valid password: 8 characters with all required constraints
      ResetPasswordDto minPasswordDto = new ResetPasswordDto("valid-token-123", "Aa1@test");

      // Act & Assert
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(minPasswordDto)))
          .andExpect(status().isOk());
    }
  }

  @Nested
  class LogoutTests {

    private final String LOGOUT_ENDPOINT = "/auth/logout";

    @Test
    void shouldReturnUnauthorized_whenAuthUserIsNull() throws Exception {
      // Arrange — @AuthenticationPrincipal resolves to null in @WebMvcTest slice without a configured
      // AuthenticationPrincipalArgumentResolver. The controller passes null to authService.logoutUser,
      // which throws AppException(INVALID_TOKEN) via Guard.requireNotNull.
      doThrow(new AppException(ErrorInfo.INVALID_TOKEN))
          .when(authService)
          .logoutUser(any(), org.mockito.ArgumentMatchers.isNull());

      // Act & Assert
      mockMvc.perform(post(LOGOUT_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnInternalServerError_whenDatabaseErrorOccurs() throws Exception {
      // Arrange — simulates a DB failure when removing the active refresh token
      doThrow(new RuntimeException("Database error"))
          .when(authService)
          .logoutUser(any(), any());

      // Act & Assert
      mockMvc.perform(post(LOGOUT_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnOk_whenLogoutIsSuccessful() throws Exception {
      // Act & Assert — in @WebMvcTest slice, @AuthenticationPrincipal resolves to null,
      // so authService.logoutUser is called with null authUser. The mock service does not throw,
      // so the controller returns 200 OK.
      mockMvc.perform(post(LOGOUT_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isOk());

      verify(authService).logoutUser(any(), any());
    }
  }
}
