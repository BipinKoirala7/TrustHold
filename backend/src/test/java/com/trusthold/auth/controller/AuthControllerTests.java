package com.trusthold.auth.controller;

import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.auth.filters.AccessTokenFilter;
import com.trusthold.auth.handlers.OAuthFailureHandler;
import com.trusthold.auth.handlers.OAuthSuccessHandler;
import com.trusthold.auth.service.PasswordResetTokenService;
import com.trusthold.config.CustomAuthenticationEntryPoint;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.exception.AppException;
import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.auth.service.AuthService;
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
}
