package com.trusthold.property.controller;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.property.dtos.CreatePropertyDto;
import com.trusthold.property.model.PropertyType;
import com.trusthold.property.service.PropertyService;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.controller.ControllerTestSupport;
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.model.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PropertyController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PropertyControllerTests extends ControllerTestSupport {

  private static final String BASE_URL = "/property";

  @MockitoBean
  private PropertyService propertyService;

  private AuthUser sampleAuthUser;
  private CreatePropertyDto createPropertyDto;

  @BeforeEach
  void setUp() {
    sampleAuthUser = new AuthUser(UUID.randomUUID(), "dummy-token", Role.LANDLORD);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            sampleAuthUser, null, List.of(new SimpleGrantedAuthority("ROLE_LANDLORD"))));

    createPropertyDto = new CreatePropertyDto(
        "Lakeside Apartment",
        PropertyType.values()[0],
        "Lakeside Road 12",
        "Pokhara",
        "Two bedroom apartment near the lake"
    );
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Nested
  class CreatePropertyTests {

    @Test
    void shouldReturnCreated_whenRequestIsValid() throws Exception {
      // Act & Assert
      mockMvc.perform(post(BASE_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(createPropertyDto)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.timestamp").exists())
          .andExpect(jsonPath("$.data").doesNotExist())
          .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void shouldReturnMalformedRequest_whenBodyIsMalformedJson() throws Exception {
      // Act & Assert
      mockMvc.perform(post(BASE_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content("{ not-json"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));

      verifyNoInteractions(propertyService);
    }

    @Test
    void shouldReturnUnsupportedMediaType_whenContentTypeIsNotJson() throws Exception {
      // Act & Assert
      mockMvc.perform(post(BASE_URL)
              .contentType(MediaType.TEXT_PLAIN)
              .content(""))
          .andExpect(status().isUnsupportedMediaType())
          .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_MEDIA_TYPE"));

      verifyNoInteractions(propertyService);
    }

    @ParameterizedTest
    @EnumSource(ErrorInfo.class)
    void shouldReturnErrorInfoResponse_whenServiceThrowsAppException(ErrorInfo errorInfo) throws Exception {
      // Arrange
      doThrow(new AppException(errorInfo))
          .when(propertyService).createProperty(eq(sampleAuthUser), any(CreatePropertyDto.class));

      // Act & Assert
      mockMvc.perform(post(BASE_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(createPropertyDto)))
          .andExpect(status().is(errorInfo.getHttpStatus().value()))
          .andExpect(jsonPath("$.error.code").value(errorInfo.getErrCode()))
          .andExpect(jsonPath("$.error.message").value(errorInfo.getMessage()));
    }

    @Test
    void shouldReturnInternalServerError_whenServiceThrowsUnexpectedException() throws Exception {
      // Arrange
      doThrow(new RuntimeException("Database connection failed"))
          .when(propertyService).createProperty(eq(sampleAuthUser), any(CreatePropertyDto.class));

      // Act & Assert
      mockMvc.perform(post(BASE_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(createPropertyDto)))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.error.code").value("INTERNAL_SERVER_ERROR"))
          .andExpect(jsonPath("$.error.message").value(
              "An unexpected error occurred. Please try again later."));
    }
  }
}