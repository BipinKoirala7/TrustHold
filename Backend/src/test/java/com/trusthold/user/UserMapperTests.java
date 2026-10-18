package com.trusthold.user;

import com.trusthold.user.dtos.RegisterUserDto;
import com.trusthold.user.dtos.UpdateUserDto;
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
  }

  @Nested
  class RegisterUserDtoToUser {

    @Test
    void shouldMapCreateUserDtoToUser() {
      // Arrange
      RegisterUserDto registerUserDto = new RegisterUserDto();
      registerUserDto.setFirstName("John");
      registerUserDto.setLastName("Doe");
      registerUserDto.setUsername("john_doe");
      registerUserDto.setEmail("john@example.com");

      // Act
      User result = underTest.createUserDtoToUser(registerUserDto);

      // Assert
      assertThat(result.getFirstName()).isEqualTo("John");
      assertThat(result.getLastName()).isEqualTo("Doe");
      assertThat(result.getUsername()).isEqualTo("john_doe");
      assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldMapCreateUserDtoToUserWithNullFields() {
      // Arrange
      RegisterUserDto registerUserDto = new RegisterUserDto();
      registerUserDto.setFirstName(null);
      registerUserDto.setLastName(null);
      registerUserDto.setUsername(null);
      registerUserDto.setEmail(null);

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
      UpdateUserDto updateUserDto = new UpdateUserDto();
      updateUserDto.setFirstName("John");
      updateUserDto.setLastName("Doe");
      updateUserDto.setUsername("john_doe123");

      // Act
      underTest.updateUserFromDto(updateUserDto, user);

      // Assert
      assertThat(user.getFirstName()).isEqualTo("John");
      assertThat(user.getLastName()).isEqualTo("Doe");
      assertThat(user.getUsername()).isEqualTo("john_doe123");
    }

    @Test
    void shouldNotUpdateFieldsWhenAllDtoFieldsAreNull() {
      // Arrange
      UpdateUserDto updateUserDto = new UpdateUserDto();
      updateUserDto.setFirstName(null);
      updateUserDto.setLastName(null);
      updateUserDto.setUsername(null);

      // Act
      underTest.updateUserFromDto(updateUserDto, user);

      // Assert
      assertThat(user.getFirstName()).isEqualTo("Jane");
      assertThat(user.getLastName()).isEqualTo("Smith");
      assertThat(user.getUsername()).isEqualTo("jane_smith");
    }

    @Test
    void shouldUpdateFirstNameOnlyWhenOtherFieldsAreNull() {
      // Arrange
      UpdateUserDto dto = new UpdateUserDto();
      dto.setFirstName("John");
      dto.setLastName(null);
      dto.setUsername(null);

      // Act
      underTest.updateUserFromDto(dto, user);

      // Assert
      assertThat(user.getFirstName()).isEqualTo("John");
      assertThat(user.getLastName()).isEqualTo("Smith");
      assertThat(user.getUsername()).isEqualTo("jane_smith");
    }

    @Test
    void shouldUpdateLastNameOnlyWhenOtherFieldsAreNull() {
      // Arrange
      UpdateUserDto dto = new UpdateUserDto();
      dto.setFirstName(null);
      dto.setLastName("Doe");
      dto.setUsername(null);

      // Act
      underTest.updateUserFromDto(dto, user);

      // Assert
      assertThat(user.getFirstName()).isEqualTo("Jane");
      assertThat(user.getLastName()).isEqualTo("Doe");
      assertThat(user.getUsername()).isEqualTo("jane_smith");
    }

    @Test
    void shouldUpdateUsernameOnlyWhenUpdateUserDtoHasOnlyUsername() {
      // Arrange
      UpdateUserDto updateUserDto = new UpdateUserDto();
      updateUserDto.setFirstName(null);
      updateUserDto.setLastName(null);
      updateUserDto.setUsername("new_username");

      // Act
      underTest.updateUserFromDto(updateUserDto, user);

      // Assert
      assertThat(user.getFirstName()).isEqualTo("Jane");
      assertThat(user.getLastName()).isEqualTo("Smith");
      assertThat(user.getUsername()).isEqualTo("new_username");
    }

    @Test
    void shouldNotModifyUserId() {
      // Arrange
      UUID originalId = user.getId();

      UpdateUserDto dto = new UpdateUserDto();
      dto.setFirstName("John");
      dto.setLastName("Doe");
      dto.setUsername("john_doe");

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

      UpdateUserDto dto = new UpdateUserDto();
      dto.setFirstName("John");
      dto.setLastName("Doe");
      dto.setUsername("john_doe");

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
