package com.trusthold.user.controller;

import com.trusthold.auth.filters.AccessTokenFilter;
import com.trusthold.config.CustomAuthenticationEntryPoint;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.dtos.UpdateUserDto;
import com.trusthold.user.service.UserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTests {

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private AccessTokenFilter accessTokenFilter;

  @MockitoBean
  private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Nested
  class GetAuthenticatedUserTests {

    private final String GET_ME_ENDPOINT = "/user/me";

    @Test
    void shouldReturnOk_whenGetAuthenticatedUserIsSuccessful() throws Exception {
      // Act & Assert
      mockMvc.perform(get(GET_ME_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isOk());

      verify(userService).getAuthenticatedUser(any());
    }

    @Test
    void shouldReturnUnauthorized_whenAuthUserIsNull() throws Exception {
      // Arrange — @AuthenticationPrincipal resolves to null in @WebMvcTest slice,
      // so userService.getAuthenticatedUser is called with null authUser, which throws INVALID_TOKEN
      doThrow(new AppException(ErrorInfo.INVALID_TOKEN))
          .when(userService)
          .getAuthenticatedUser(org.mockito.ArgumentMatchers.isNull());

      // Act & Assert
      mockMvc.perform(get(GET_ME_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnNotFound_whenUserNotFound() throws Exception {
      // Arrange — user id from token does not exist in the database
      doThrow(new AppException(ErrorInfo.USER_NOT_FOUND))
          .when(userService)
          .getAuthenticatedUser(any());

      // Act & Assert
      mockMvc.perform(get(GET_ME_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnInternalServerError_whenDatabaseErrorOccurs() throws Exception {
      // Arrange — simulates a DB failure when fetching the user
      doThrow(new RuntimeException("Database error"))
          .when(userService)
          .getAuthenticatedUser(any());

      // Act & Assert
      mockMvc.perform(get(GET_ME_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnMethodNotAllowed_whenPostRequest() throws Exception {
      // Act & Assert — POST request to GET-only endpoint returns 405
      mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(GET_ME_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isMethodNotAllowed());
    }
  }

  @Nested
  class UpdateUserTests {

    private UpdateUserDto updateUserDto;
    private final String UPDATE_USER_ENDPOINT = "/user";

    @BeforeEach
    void setUp() {
      updateUserDto = new UpdateUserDto(
          "Test",
          "User",
          "testuser123",
          "+1234567890");
    }

    @Test
    void shouldReturnOk_whenUpdateUserIsSuccessful() throws Exception {
      // Act & Assert
      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(updateUserDto)))
          .andExpect(status().isOk());

      verify(userService).updateUser(any(), any(UpdateUserDto.class));
    }

    @Test
    void shouldReturnBadRequest_whenRequestBodyIsNull() throws Exception {
      // Arrange & Act — send empty JSON object so @Valid fails on required fields
      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content("{}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenFirstNameIsNull() throws Exception {
      // Arrange — firstName is @NotNull
      UpdateUserDto invalidDto = new UpdateUserDto(
          null,
          "User",
          "testuser123",
          "+1234567890");

      // Act & Assert
      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(invalidDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenLastNameIsNull() throws Exception {
      // Arrange — lastName is @NotNull
      UpdateUserDto invalidDto = new UpdateUserDto(
          "Test",
          null,
          "testuser123",
          "+1234567890");

      // Act & Assert
      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(invalidDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenUsernameIsNull() throws Exception {
      // Arrange — username is @NotNull
      UpdateUserDto invalidDto = new UpdateUserDto(
          "Test",
          "User",
          null,
          "+1234567890");

      // Act & Assert
      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(invalidDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenPhoneIsNull() throws Exception {
      // Arrange — phone is @NotBlank
      UpdateUserDto invalidDto = new UpdateUserDto(
          "Test",
          "User",
          "testuser123",
          null);

      // Act & Assert
      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(invalidDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenFirstNameIsTooShort() throws Exception {
      // Arrange — firstName must be at least 3 characters
      UpdateUserDto shortDto = new UpdateUserDto(
          "Te",
          "User",
          "testuser123",
          "+1234567890");

      // Act & Assert
      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(shortDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenFirstNameIsTooLong() throws Exception {
      // Arrange — firstName must be at most 50 characters
      String longName = "A".repeat(51);
      UpdateUserDto longDto = new UpdateUserDto(
          longName,
          "User",
          "testuser123",
          "+1234567890");

      // Act & Assert
      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(longDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenLastNameIsTooShort() throws Exception {
      // Arrange — lastName must be at least 3 characters
      UpdateUserDto shortDto = new UpdateUserDto(
          "Test",
          "Us",
          "testuser123",
          "+1234567890");

      // Act & Assert
      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(shortDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenUsernameIsTooShort() throws Exception {
      // Arrange — username must be at least 3 characters
      UpdateUserDto shortDto = new UpdateUserDto(
          "Test",
          "User",
          "te",
          "+1234567890");

      // Act & Assert
      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(shortDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenPhoneFormatIsInvalid() throws Exception {
      // Arrange — phone must match ^\+[1-9]\d{1,14}$
      UpdateUserDto invalidPhoneDto = new UpdateUserDto(
          "Test",
          "User",
          "testuser123",
          "1234567890");

      // Act & Assert
      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(invalidPhoneDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenPhoneExceedsMaxLength() throws Exception {
      // Arrange — phone must be at most 16 characters
      UpdateUserDto longPhoneDto = new UpdateUserDto(
          "Test",
          "User",
          "testuser123",
          "+12345678901234567");

      // Act & Assert
      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(longPhoneDto)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorized_whenAuthUserIsNull() throws Exception {
      // Arrange — @AuthenticationPrincipal resolves to null in @WebMvcTest slice,
      // so userService.updateUser is called with null authUser, which throws INVALID_TOKEN
      doThrow(new AppException(ErrorInfo.INVALID_TOKEN))
          .when(userService)
          .updateUser(org.mockito.ArgumentMatchers.isNull(), any(UpdateUserDto.class));

      // Act & Assert
      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(updateUserDto)))
          .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnNotFound_whenUserNotFound() throws Exception {
      // Arrange — user id from token does not exist in the database
      doThrow(new AppException(ErrorInfo.USER_NOT_FOUND))
          .when(userService)
          .updateUser(any(), any(UpdateUserDto.class));

      // Act & Assert
      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(updateUserDto)))
          .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnInternalServerError_whenDatabaseErrorOccurs() throws Exception {
      // Arrange — simulates a DB failure when saving the user
      doThrow(new RuntimeException("Database error"))
          .when(userService)
          .updateUser(any(), any(UpdateUserDto.class));

      // Act & Assert
      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(updateUserDto)))
          .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnBadRequest_whenJsonIsMalformed() throws Exception {
      // Arrange — malformed JSON with unquoted keys
      String malformedJson = "{firstName: Test, lastName: User, username: testuser123, phone: +1234567890}";

      // Act & Assert
      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content(malformedJson))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnsupportedMediaType_whenContentTypeIsMissing() throws Exception {
      // Act & Assert — no content type header returns 415
      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .content(objectMapper.writeValueAsString(updateUserDto)))
          .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void shouldReturnMethodNotAllowed_whenGetRequest() throws Exception {
      // Act & Assert — GET request to PUT-only endpoint returns 405
      mockMvc.perform(get(UPDATE_USER_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void shouldReturnMethodNotAllowed_whenPostRequest() throws Exception {
      // Act & Assert — POST request to PUT-only endpoint returns 405
      mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(updateUserDto)))
          .andExpect(status().isMethodNotAllowed());
    }
  }

  @Nested
  class DeleteUserTests {

    private final String DELETE_USER_ENDPOINT = "/user";

    @Test
    void shouldReturnOk_whenDeleteUserIsSuccessful() throws Exception {
      // Act & Assert
      mockMvc.perform(delete(DELETE_USER_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isOk());

      verify(userService).deleteUser(any());
    }

    @Test
    void shouldReturnUnauthorized_whenAuthUserIsNull() throws Exception {
      // Arrange — @AuthenticationPrincipal resolves to null in @WebMvcTest slice,
      // so userService.deleteUser is called with null authUser, which throws INVALID_TOKEN
      doThrow(new AppException(ErrorInfo.INVALID_TOKEN))
          .when(userService)
          .deleteUser(org.mockito.ArgumentMatchers.isNull());

      // Act & Assert
      mockMvc.perform(delete(DELETE_USER_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnInternalServerError_whenDatabaseErrorOccurs() throws Exception {
      // Arrange — simulates a DB failure when deleting the user
      doThrow(new RuntimeException("Database error"))
          .when(userService)
          .deleteUser(any());

      // Act & Assert
      mockMvc.perform(delete(DELETE_USER_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnMethodNotAllowed_whenGetRequest() throws Exception {
      // Act & Assert — GET request to DELETE-only endpoint returns 405
      mockMvc.perform(get(DELETE_USER_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void shouldReturnMethodNotAllowed_whenPostRequest() throws Exception {
      // Act & Assert — POST request to DELETE-only endpoint returns 405
      mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(DELETE_USER_ENDPOINT)
              .contentType("application/json"))
          .andExpect(status().isMethodNotAllowed());
    }
  }
}
