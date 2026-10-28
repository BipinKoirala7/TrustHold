package com.trusthold.user;

import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.user.dtos.UpdateUserDto;
import com.trusthold.user.model.Role;
import com.trusthold.user.model.User;
import com.trusthold.user.service.UserMapper;
import com.trusthold.user.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class UserMapperTests {

  private final UserMapper underTest = new UserMapperImpl();

  private final User user = new User();

  @BeforeEach
  void setUp() {
    user.setId(UUID.randomUUID());
    user.setFirstName("Jane");
    user.setLastName("Smith");
    user.setUsername("jane_smith");
    user.setPhone("+1234567890");
  }

  @Nested
  class RegisterUserDtoToUser {

    @Test
    void shouldMapCreateUserDtoToUser() {
      // Arrange
      RegisterUserDto registerUserDto = new RegisterUserDto(
          "John",
          "Doe",
          "john_doe",
          "john@example.com",
          "password@123",
          "+1234567890",
          RegistrationRole.TENANT
      );

      // Act
      User result = underTest.createUserDtoToUser(registerUserDto);

      // Assert
      assertThat(result.getFirstName()).isEqualTo("John");
      assertThat(result.getLastName()).isEqualTo("Doe");
      assertThat(result.getUsername()).isEqualTo("john_doe");
      assertThat(result.getEmail()).isEqualTo("john@example.com");
      assertThat(result.getPassword()).isEqualTo("password@123");
      assertThat(result.getRole().toString()).isEqualTo(RegistrationRole.TENANT.toString());
    }

    @Test
    void shouldMapCreateUserDtoToUserWithNullFields() {
      // Arrange
      RegisterUserDto registerUserDto = new RegisterUserDto(
          null,
          null,
          null,
          null,
          null,
          null,
          RegistrationRole.TENANT
      );

      // Act
      User result = underTest.createUserDtoToUser(registerUserDto);

      // Assert
      assertThat(result.getFirstName()).isNull();
      assertThat(result.getLastName()).isNull();
      assertThat(result.getUsername()).isNull();
      assertThat(result.getEmail()).isNull();
    }
  }

  @Nested
  class UpdatedUserFromUpdateUserDto {

    @Test
    void shouldUpdateUserFromUpdateUserDto() {
      // Arrange
      UpdateUserDto updateUserDto = new UpdateUserDto("John", "Doe", "john_doe123", "+9779846271578");

      // Act
      underTest.updateUserFromDto(updateUserDto, user);

      // Assert
      assertThat(user.getFirstName()).isEqualTo("John");
      assertThat(user.getLastName()).isEqualTo("Doe");
      assertThat(user.getUsername()).isEqualTo("john_doe123");
      assertThat(user.getPhone()).isEqualTo("+9779846271578");
    }

    @Test
    void shouldNotUpdateFieldsWhenAllDtoFieldsAreNull() {
      // Arrange
      UpdateUserDto updateUserDto = new UpdateUserDto(null, null, null, null);

      // Act
      underTest.updateUserFromDto(updateUserDto, user);

      // Assert
      assertThat(user.getFirstName()).isEqualTo("Jane");
      assertThat(user.getLastName()).isEqualTo("Smith");
      assertThat(user.getUsername()).isEqualTo("jane_smith");
      assertThat(user.getPhone()).isEqualTo("+1234567890");
    }

    @Test
    void shouldUpdateFirstNameOnlyWhenOtherFieldsAreNull() {
      // Arrange
      UpdateUserDto dto = new UpdateUserDto("John", null, null, null);

      // Act
      underTest.updateUserFromDto(dto, user);

      // Assert
      assertThat(user.getFirstName()).isEqualTo("John");
      assertThat(user.getLastName()).isEqualTo("Smith");
      assertThat(user.getUsername()).isEqualTo("jane_smith");
      assertThat(user.getPhone()).isEqualTo("+1234567890");
    }

    @Test
    void shouldUpdateLastNameOnlyWhenOtherFieldsAreNull() {
      // Arrange
      UpdateUserDto dto = new UpdateUserDto(null, "Doe", null, null);

      // Act
      underTest.updateUserFromDto(dto, user);

      // Assert
      assertThat(user.getFirstName()).isEqualTo("Jane");
      assertThat(user.getLastName()).isEqualTo("Doe");
      assertThat(user.getUsername()).isEqualTo("jane_smith");
      assertThat(user.getPhone()).isEqualTo("+1234567890");
    }

    @Test
    void shouldUpdateUsernameOnlyWhenUpdateUserDtoHasOnlyUsername() {
      // Arrange
      UpdateUserDto updateUserDto = new UpdateUserDto(null, null, "new_username", null);

      // Act
      underTest.updateUserFromDto(updateUserDto, user);

      // Assert
      assertThat(user.getFirstName()).isEqualTo("Jane");
      assertThat(user.getLastName()).isEqualTo("Smith");
      assertThat(user.getUsername()).isEqualTo("new_username");
      assertThat(user.getPhone()).isEqualTo("+1234567890");
    }

    @Test
    void shouldNotModifyUserId() {
      // Arrange
      UUID originalId = user.getId();

      UpdateUserDto dto = new UpdateUserDto("John", "Doe", "john_doe", "+1234567890");
      // Act
      underTest.updateUserFromDto(dto, user);

      // Assert
      assertThat(user.getId()).isEqualTo(originalId);
    }

    @Test
    void shouldNotModifyEmailOrRole() {
      // Arrange
      user.setEmail("jane@example.com");
      user.setRole(Role.TENANT);

      UpdateUserDto dto = new UpdateUserDto("John", "Doe", "john_doe", "+1234567890");

      // Act
      underTest.updateUserFromDto(dto, user);

      // Assert
      assertThat(user.getEmail()).isEqualTo("jane@example.com");
      assertThat(user.getRole()).isEqualTo(Role.TENANT);
    }

    @Test
    void shouldHandleNullUpdateDto() {
      // Act
      underTest.updateUserFromDto(null, user);

      // Assert
      assertThat(user.getFirstName()).isEqualTo("Jane");
      assertThat(user.getLastName()).isEqualTo("Smith");
      assertThat(user.getUsername()).isEqualTo("jane_smith");
    }
  }
}
