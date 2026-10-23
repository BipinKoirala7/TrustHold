package com.trusthold.auth;

import com.trusthold.auth.controller.AuthController;
import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.auth.exception.EmailAlreadyExistsException;
import com.trusthold.auth.exception.UsernameAlreadyExistsException;
import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.auth.service.AuthService;
import com.trusthold.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTests {

  @MockitoBean
  private AuthService authService;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Nested
  class RegisterLocalUserTests {

    private RegisterUserDto registerUserDto;
    private final String REGISTER_USER_ENDPOINT = "/api/v1/auth/register";

    @BeforeEach
    void setUp() {
      registerUserDto = new RegisterUserDto(
          "Test",
          "User",
          "testUser123",
          "testuser@example.com",
          "Test@Password1",
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
      doThrow(new UsernameAlreadyExistsException("Username already exists"))
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
      doThrow(new EmailAlreadyExistsException("Email already exists"))
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
    }
  }
}
