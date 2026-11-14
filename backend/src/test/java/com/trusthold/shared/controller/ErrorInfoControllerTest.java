package com.trusthold.shared.controller;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.controller.UserController;
import com.trusthold.user.dtos.UpdateUserDto;
import com.trusthold.user.model.Role;
import com.trusthold.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ErrorInfoControllerTest extends ControllerTestSupport {

  @MockitoBean
  private UserService userService;

  private UpdateUserDto sampleUpdateUserDto;
  private AuthUser sampleAuthUser;

  @BeforeEach
  void setUp() {
    sampleAuthUser = new AuthUser(UUID.randomUUID(), "some-access-token", Role.TENANT);
    sampleUpdateUserDto = new UpdateUserDto("some-name", "some-email", "blah_123", "+56");

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(sampleAuthUser, null, List.of()));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private static final String BASE_URL = "/user";

  @Test
  void shouldReturnMalformedRequest_whenBodyIsMalformedJson() throws Exception {
    // Act & Assert
    mockMvc.perform(put(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{ not-json"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));

    verifyNoInteractions(userService);
  }

  @Test
  void shouldReturnUnsupportedMediaType_whenContentTypeIsNotJson() throws Exception {
    // Act & Assert
    mockMvc.perform(put(BASE_URL)
            .contentType(MediaType.TEXT_PLAIN))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_MEDIA_TYPE"));

    verifyNoInteractions(userService);
  }

  @ParameterizedTest
  @EnumSource(ErrorInfo.class)
  void shouldReturnErrorInfoResponse_whenServiceThrowsAppException(ErrorInfo errorInfo) throws Exception {
    // Arrange
    doThrow(new AppException(errorInfo))
        .when(userService).updateUser(eq(sampleAuthUser), eq(sampleUpdateUserDto));

    // Act & Assert
    mockMvc.perform(put(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleUpdateUserDto)))
        .andExpect(status().is(errorInfo.getHttpStatus().value()))
        .andExpect(jsonPath("$.error.code").value(errorInfo.getErrCode()))
        .andExpect(jsonPath("$.error.message").value(errorInfo.getMessage()));
  }

  @Test
  void shouldReturnInternalServerError_whenServiceThrowsUnexpectedException() throws Exception {
    // Arrange
    doThrow(new RuntimeException("Database connection failed"))
        .when(userService).updateUser(eq(sampleAuthUser), eq(sampleUpdateUserDto));

    // Act & Assert
    mockMvc.perform(put(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleUpdateUserDto)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error.code").value("INTERNAL_SERVER_ERROR"))
        .andExpect(jsonPath("$.error.message").value(
            "An unexpected error occurred. Please try again later."));
  }
}
