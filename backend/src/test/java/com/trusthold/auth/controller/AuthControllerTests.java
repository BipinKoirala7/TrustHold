package com.trusthold.auth.controller;

import com.trusthold.auth.dtos.*;
import com.trusthold.auth.filters.AccessTokenFilter;
import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.auth.service.AuthService;
import com.trusthold.auth.service.PasswordResetTokenService;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.controller.ControllerTestSupport;
import com.trusthold.shared.exception.AppException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice tests for AuthController: HTTP contract and wiring only.
 * <p>
 * Business rules live in AuthServiceTests / PasswordResetTokenServiceTests.
 * DTO validation rules live in the DTO validation unit tests.
 * Here we only prove: @Valid is wired, the service is called with the right
 * arguments, and the exception handler maps each kind of failure to a status.
 */
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTests extends ControllerTestSupport {

  @MockitoBean
  private AuthService authService;

  @MockitoBean
  private PasswordResetTokenService passwordResetTokenService;

  @MockitoBean
  private AccessTokenFilter accessTokenFilter;


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
    void shouldReturnCreated_whenRegistrationIsSuccessful() throws Exception {
      mockMvc.perform(post(REGISTER_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(registerUserDto)))
          .andExpect(status().isCreated());

      verify(authService).registerLocalUser(eq(registerUserDto));
    }

    @Test
    void shouldReturnBadRequest_whenBodyFailsValidation() throws Exception {
      // Proves @Valid is wired on this endpoint; the individual rules are tested on the DTO.
      mockMvc.perform(post(REGISTER_USER_ENDPOINT)
              .contentType("application/json")
              .content("{}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflict_whenAppExceptionIsAConflict() throws Exception {
      // One representative case: proves AppException(conflict) -> 409 in the handler.
      doThrow(new AppException(ErrorInfo.USERNAME_ALREADY_EXISTS))
          .when(authService)
          .registerLocalUser(any(RegisterUserDto.class));

      mockMvc.perform(post(REGISTER_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(registerUserDto)))
          .andExpect(status().isConflict());
    }
  }

  @Nested
  class BeginGoogleRegistrationTests {

    private final String GOOGLE_REGISTER_ENDPOINT = "/auth/oauth/register";

    @ParameterizedTest
    @EnumSource(RegistrationRole.class)
    void shouldReturnOkAndStoreRoleInSession_whenRoleIsValid(RegistrationRole role) throws Exception {
      GoogleRegistrationRequest request = new GoogleRegistrationRequest(role);

      mockMvc.perform(post(GOOGLE_REGISTER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(request().sessionAttribute("registrationRole", role.name()));
    }

    @Test
    void shouldReturnBadRequest_whenBodyFailsValidation() throws Exception {
      mockMvc.perform(post(GOOGLE_REGISTER_ENDPOINT)
              .contentType("application/json")
              .content("{}"))
          .andExpect(status().isBadRequest());
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
    void shouldReturnOk_whenLoginIsSuccessful() throws Exception {
      mockMvc.perform(post(LOGIN_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(loginUserDto)))
          .andExpect(status().isOk());

      verify(authService).loginUser(eq(loginUserDto), any());
    }

    @Test
    void shouldReturnBadRequest_whenBodyFailsValidation() throws Exception {
      mockMvc.perform(post(LOGIN_ENDPOINT)
              .contentType("application/json")
              .content("{}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorized_whenAppExceptionIsInvalidCredentials() throws Exception {
      // One representative case: proves AppException(INVALID_CREDENTIALS) -> 401 in the handler.
      doThrow(new AppException(ErrorInfo.INVALID_CREDENTIALS))
          .when(authService)
          .loginUser(any(LoginUserDto.class), any());

      mockMvc.perform(post(LOGIN_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(loginUserDto)))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  class TokenServiceRefreshTests {

    private final String REFRESH_TOKEN_ENDPOINT = "/auth/token/refresh";

    @Test
    void shouldReturnOk_whenTokenRefreshIsSuccessful() throws Exception {
      mockMvc.perform(post(REFRESH_TOKEN_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isOk());

      verify(authService).refreshToken(any(), any());
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
    void shouldReturnOk_whenForgotPasswordIsSuccessful() throws Exception {
      mockMvc.perform(post(FORGOT_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(forgotPasswordDto)))
          .andExpect(status().isOk());

      verify(passwordResetTokenService).forgotPassword(eq(forgotPasswordDto));
    }

    @Test
    void shouldReturnBadRequest_whenEmailFailsValidation() throws Exception {
      mockMvc.perform(post(FORGOT_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(new ForgotPasswordDto("not-an-email"))))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFound_whenAppExceptionIsUserNotFound() throws Exception {
      // Pins current behavior (404 lets callers enumerate accounts). Revisit and change
      // this test together with the handler/service if you switch to a uniform 200.
      doThrow(new AppException(ErrorInfo.USER_NOT_FOUND))
          .when(passwordResetTokenService)
          .forgotPassword(any(ForgotPasswordDto.class));

      mockMvc.perform(post(FORGOT_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(forgotPasswordDto)))
          .andExpect(status().isNotFound());
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
    void shouldReturnOk_whenResetPasswordIsSuccessful() throws Exception {
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(resetPasswordDto)))
          .andExpect(status().isOk());

      verify(passwordResetTokenService).resetPassword(any(), eq(resetPasswordDto));
    }

    @Test
    void shouldReturnBadRequest_whenBodyFailsValidation() throws Exception {
      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content("{}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenAppExceptionIsInvalidResetToken() throws Exception {
      // Covers both "not found" and "expired": the controller can't tell them apart.
      doThrow(new AppException(ErrorInfo.INVALID_PASSWORD_RESET_TOKEN))
          .when(passwordResetTokenService)
          .resetPassword(any(), any(ResetPasswordDto.class));

      mockMvc.perform(post(RESET_PASSWORD_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(resetPasswordDto)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  class LogoutTests {

    private final String LOGOUT_ENDPOINT = "/auth/logout";

    @Test
    void shouldReturnOk_whenLogoutIsSuccessful() throws Exception {
      mockMvc.perform(post(LOGOUT_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isOk());

      verify(authService).logoutUser(any(), any());
    }

    @Test
    void shouldReturnUnauthorized_whenAppExceptionIsInvalidToken() throws Exception {
      // Proves AppException(INVALID_TOKEN) -> 401 in the handler. The real
      // "no/expired cookie" path is covered by the integration journey, not here.
      doThrow(new AppException(ErrorInfo.INVALID_TOKEN))
          .when(authService)
          .logoutUser(any(), any());

      mockMvc.perform(post(LOGOUT_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isUnauthorized());
    }
  }

  /**
   * Every endpoint shares the exception handler, so its generic fallbacks
   * are tested here once instead of once per endpoint.
   */
  @Nested
  class ExceptionHandlingTests {

    @Test
    void shouldReturnInternalServerError_whenUnexpectedExceptionIsThrown() throws Exception {
      doThrow(new RuntimeException("Database error"))
          .when(authService)
          .registerLocalUser(any(RegisterUserDto.class));

      RegisterUserDto dto = new RegisterUserDto(
          "Test", "User", "testuser123", "testuser@example.com",
          "Test@Password1", "+1234567890", RegistrationRole.TENANT);

      mockMvc.perform(post("/auth/register")
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(dto)))
          .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnInternalServerError_whenJwtExceptionIsThrown() throws Exception {
      // Pins CURRENT behavior. An invalid/expired refresh token should probably be 401;
      // when you fix the handler, this is the single test to update.
      doThrow(new JwtException("Invalid signature"))
          .when(authService)
          .refreshToken(any(), any());

      mockMvc.perform(post("/auth/token/refresh")
              .contentType("application/json"))
          .andExpect(status().isInternalServerError());
    }
  }
}