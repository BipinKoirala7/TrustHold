package com.trusthold.user.integration;

import com.trusthold.auth.dtos.LoginUserDto;
import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.shared.dtos.ApiResponse;
import com.trusthold.shared.dtos.IntegrationTestSupport;
import com.trusthold.user.dtos.PublicUserDto;
import com.trusthold.user.dtos.UpdateUserDto;
import com.trusthold.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class UserIntegrationTest extends IntegrationTestSupport {

  private final String username = "bipin123";
  private final String email = "bipin@example.com";
  private final String password = "Password123!";

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();

    String firstName = "Bipin";
    String lastName = "Koirala";
    String phone = "+1234567890";

    validRegisterUserDto = new RegisterUserDto(firstName, lastName, username, email, password, phone, RegistrationRole.TENANT);
    validLoginUserDto = new LoginUserDto(email, password);

    assertThat(register(validRegisterUserDto).getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
  }

  @Nested
  class GetAuthenticatedUserTests{

    private final String GET_AUTHENTICATED_USER = "/user/me";

    @Test
    void shouldReturnAuthenticatedUser() {
      ResponseEntity<ApiResponse<PublicUserDto>> response = get(GET_AUTHENTICATED_USER, getAuthenticatedHeaders(), PublicUserDto.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
//      assertThat(response.getBody().data()).isNotNull();
      assertThat(response.getBody().data().username()).isEqualTo(username);
      assertThat(response.getBody().data().email()).isEqualTo(email);
    }

    @Test
    void shouldFail_whenRequestIsNotAuthenticated() {
      ResponseEntity<ApiResponse<PublicUserDto>> response = get(GET_AUTHENTICATED_USER, getHeaders(), PublicUserDto.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().data()).isNull();
      assertThat(response.getBody().error()).isNotNull();
    }
  }

  @Nested
  class UpdateUserTest{

    private final String UPDATE_USER = "/user";

    @Test
    void shouldUpdateUser(){
      String GET_AUTHENTICATED_USER = "/user/me";

      String updatedFirstName = "Bibek";
      String updatedLastName = "Tiwari";
      String updatedUsername = "bibek123";
      String updatedPhone = "+1238565656556";

      UpdateUserDto updateUserDto = new UpdateUserDto(updatedFirstName, updatedLastName, updatedUsername, updatedPhone);
      assertThat(put(UPDATE_USER, getAuthenticatedHeaders(), updateUserDto).getStatusCode()).isEqualTo(HttpStatus.OK);

      ResponseEntity<ApiResponse<PublicUserDto>> response = get(GET_AUTHENTICATED_USER, getAuthenticatedHeaders(), PublicUserDto.class);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().data().firstName()).isEqualTo(updatedFirstName);
      assertThat(response.getBody().data().lastName()).isEqualTo(updatedLastName);
      assertThat(response.getBody().data().username()).isEqualTo(updatedUsername);
      assertThat(response.getBody().data().phone()).isEqualTo(updatedPhone);
    }

    @Test
    void shouldReturnUnauthorized_whenUserIsNotAuthenticated(){
      String updatedFirstName = "Bibek";
      String updatedLastName = "Tiwari";
      String updatedUsername = "bibek123";
      String updatedPhone = "+0987654321";

      UpdateUserDto updateUserDto = new UpdateUserDto(updatedFirstName, updatedLastName, updatedUsername, updatedPhone);
      ResponseEntity<ApiResponse<Void>> response = put(UPDATE_USER, getHeaders(), updateUserDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().data()).isNull();
      assertThat(response.getBody().error()).isNotNull();
    }
  }
}
