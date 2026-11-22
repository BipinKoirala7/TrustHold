package com.trusthold.property.controller;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.property.dtos.CreatePropertyDto;
import com.trusthold.property.model.PropertyType;
import com.trusthold.property.service.PropertyService;
import com.trusthold.shared.controller.ControllerTestSupport;
import com.trusthold.shared.exception.AppException;
import com.trusthold.shared.exception.AuthException;
import com.trusthold.user.model.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PropertyController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PropertyControllerTests extends ControllerTestSupport {

  private static final String BASE_URL = "/property";

  @MockitoBean
  private PropertyService propertyService;

  private AuthUser sampleAuthUser;
  private UUID propertyId;
  private CreatePropertyDto createPropertyDto;

  @BeforeEach
  void setUp() {
    sampleAuthUser = new AuthUser(UUID.randomUUID(), "dummy-token", Role.LANDLORD);
    propertyId = UUID.randomUUID();

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
    void shouldReturnUnauthorized_whenServiceThrowsUnauthorized() throws Exception {
      // Arrange
      doThrow(new AppException(AuthException.UNAUTHORIZED))
          .when(propertyService).createProperty(eq(sampleAuthUser), any(CreatePropertyDto.class));

      // Act & Assert
      mockMvc.perform(post(BASE_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(createPropertyDto)))
          .andExpect(status().is(AuthException.UNAUTHORIZED.getHttpStatus().value()))
          .andExpect(jsonPath("$.error.code").value(AuthException.UNAUTHORIZED.getErrCode()))
          .andExpect(jsonPath("$.error.message").value(AuthException.UNAUTHORIZED.getMessage()));
    }

  }

  @Nested
  class GetAllPropertiesTests {

    @Test
    void shouldReturnUnauthorized_whenServiceThrowsUnauthorized() throws Exception {
      // Arrange
      when(propertyService.getAllProperties(eq(sampleAuthUser)))
          .thenThrow(new AppException(AuthException.UNAUTHORIZED));

      // Act & Assert
      mockMvc.perform(get(BASE_URL))
          .andExpect(status().is(AuthException.UNAUTHORIZED.getHttpStatus().value()))
          .andExpect(jsonPath("$.error.code").value(AuthException.UNAUTHORIZED.getErrCode()))
          .andExpect(jsonPath("$.error.message").value(AuthException.UNAUTHORIZED.getMessage()));
    }

  }

  @Nested
  class GetPropertyByIdTests {

    @Test
    void shouldReturnUnauthorized_whenServiceThrowsUnauthorized() throws Exception {
      // Arrange
      when(propertyService.getPropertyInfo(eq(sampleAuthUser), eq(propertyId)))
          .thenThrow(new AppException(AuthException.UNAUTHORIZED));

      // Act & Assert
      mockMvc.perform(get(BASE_URL + "/{id}", propertyId))
          .andExpect(status().is(AuthException.UNAUTHORIZED.getHttpStatus().value()))
          .andExpect(jsonPath("$.error.code").value(AuthException.UNAUTHORIZED.getErrCode()))
          .andExpect(jsonPath("$.error.message").value(AuthException.UNAUTHORIZED.getMessage()));
    }

    @Test
    void shouldReturnClientError_whenIdIsNotAValidUuid() throws Exception {
      // Act & Assert
      mockMvc.perform(get(BASE_URL + "/{id}", "not-a-uuid"))
          .andExpect(status().isBadRequest());

      verifyNoInteractions(propertyService);
    }
  }

  @Nested
  class ArchivePropertyTests {

    @Test
    void shouldReturnUnauthorized_whenServiceThrowsUnauthorized() throws Exception {
      // Arrange
      doThrow(new AppException(AuthException.UNAUTHORIZED))
          .when(propertyService).archiveProperty(eq(sampleAuthUser), eq(propertyId));

      // Act & Assert
      mockMvc.perform(delete(BASE_URL + "/{id}", propertyId))
          .andExpect(status().is(AuthException.UNAUTHORIZED.getHttpStatus().value()))
          .andExpect(jsonPath("$.error.code").value(AuthException.UNAUTHORIZED.getErrCode()))
          .andExpect(jsonPath("$.error.message").value(AuthException.UNAUTHORIZED.getMessage()));
    }

    @Test
    void shouldReturnClientError_whenIdIsNotAValidUuid() throws Exception {
      // Act & Assert
      mockMvc.perform(delete(BASE_URL + "/{id}", "not-a-uuid"))
          .andExpect(status().isBadRequest());

      verifyNoInteractions(propertyService);
    }
  }
}