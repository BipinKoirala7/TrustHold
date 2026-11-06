package com.trusthold.user.controller;

import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.controller.ControllerTestSupport;
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.dtos.UpdateUserDto;
import com.trusthold.user.service.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice tests for UserController: HTTP contract and wiring only.
 * <p>
 * Business rules live in UserServiceTests. Field-level validation rules live in
 * the UpdateUserDto validation unit test. Generic handler fallbacks (500, 405,
 * 415, malformed JSON) are covered once in AuthControllerTests.ExceptionHandlingTests.
 * Auth/401 behavior with the real filter is covered by the integration journey.
 */
public class UserControllerTests extends ControllerTestSupport {

  @MockitoBean
  private UserService userService;

  @Nested
  class GetAuthenticatedUserTests {

    private final String GET_ME_ENDPOINT = "/user/me";

    @Test
    void shouldReturnOk_whenGetAuthenticatedUserIsSuccessful() throws Exception {
      mockMvc.perform(get(GET_ME_ENDPOINT))
          .andExpect(status().isOk());

      verify(userService).getAuthenticatedUser(any());
    }

    @Test
    void shouldReturnNotFound_whenAppExceptionIsUserNotFound() throws Exception {
      doThrow(new AppException(ErrorInfo.USER_NOT_FOUND))
          .when(userService)
          .getAuthenticatedUser(any());

      mockMvc.perform(get(GET_ME_ENDPOINT))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  class UpdateUserTests {

    private final UpdateUserDto updateUserDto = new UpdateUserDto(
        "Test",
        "User",
        "testuser123",
        "+1234567890");
    private final String UPDATE_USER_ENDPOINT = "/user";

    @Test
    void shouldReturnOk_whenUpdateUserIsSuccessful() throws Exception {
      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(updateUserDto)))
          .andExpect(status().isOk());

      verify(userService).updateUser(any(), eq(updateUserDto));
    }

    @Test
    void shouldReturnBadRequest_whenBodyFailsValidation() throws Exception {
      // Proves @Valid is wired; individual field rules are tested on the DTO.
      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content("{}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFound_whenAppExceptionIsUserNotFound() throws Exception {
      doThrow(new AppException(ErrorInfo.USER_NOT_FOUND))
          .when(userService)
          .updateUser(any(), any(UpdateUserDto.class));

      mockMvc.perform(put(UPDATE_USER_ENDPOINT)
              .contentType("application/json")
              .content(objectMapper.writeValueAsString(updateUserDto)))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  class DeleteUserTests {

    @Test
    void shouldReturnOk_whenDeleteUserIsSuccessful() throws Exception {
      mockMvc.perform(delete("/user"))
          .andExpect(status().isOk());

      verify(userService).deleteUser(any());
    }
  }
}