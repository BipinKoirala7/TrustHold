package com.trusthold.user;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.exception.AppException;
import com.trusthold.user.dtos.PublicUserDto;
import com.trusthold.user.dtos.UpdateUserDto;
import com.trusthold.user.model.Role;
import com.trusthold.user.model.User;
import com.trusthold.user.repository.UserRepository;
import com.trusthold.user.service.UserMapper;
import com.trusthold.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

  @InjectMocks
  private UserService userService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  private User sampleUser;
  private AuthUser sampleAuthUser;
  private UUID userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    sampleUser = new User();
    sampleUser.setId(userId);
    sampleUser.setFirstName("Jane");
    sampleUser.setLastName("Smith");
    sampleUser.setUsername("jane_smith");
    sampleUser.setEmail("jane@example.com");
    sampleUser.setPhone("+1234567890");
    sampleUser.setRole(Role.TENANT);

    sampleAuthUser = new AuthUser(userId, "dummy-token", Role.TENANT);
  }

  @Nested
  class GetAuthenticatedUserTests {

    @Test
    void shouldThrowAppException_whenAuthUserIsNull() {
      // Act & Assert
      assertThatThrownBy(() -> userService.getAuthenticatedUser(null))
          .isInstanceOf(AppException.class)
          .extracting("errorInfo")
          .isEqualTo(ErrorInfo.INVALID_TOKEN);
    }

    @Test
    void shouldThrowAppException_whenUserNotFoundInDatabase() {
      // Arrange
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> userService.getAuthenticatedUser(sampleAuthUser))
          .isInstanceOf(AppException.class)
          .extracting("errorInfo")
          .isEqualTo(ErrorInfo.USER_NOT_FOUND);
    }

    @Test
    void shouldReturnPublicUserDto_whenUserExists() {
      // Arrange
      PublicUserDto expectedDto = new PublicUserDto(
          userId, "Jane", "Smith", "jane_smith", "jane@example.com",
          "+1234567890", Role.TENANT, null, null, null, null
      );

      when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
      when(userMapper.userToPublicUserDto(sampleUser)).thenReturn(expectedDto);

      // Act
      PublicUserDto result = userService.getAuthenticatedUser(sampleAuthUser);

      // Assert
      assertThat(result).isEqualTo(expectedDto);
      verify(userRepository).findById(userId);
      verify(userMapper).userToPublicUserDto(sampleUser);
    }
  }

  @Nested
  class UpdateUserTests {

    @Test
    void shouldThrowAppException_whenAuthUserIsNull() {
      // Arrange
      UpdateUserDto updateUserDto = new UpdateUserDto("John", "Doe", "john_doe", "+1234567890");

      // Act & Assert
      assertThatThrownBy(() -> userService.updateUser(null, updateUserDto))
          .isInstanceOf(AppException.class)
          .extracting("errorInfo")
          .isEqualTo(ErrorInfo.INVALID_TOKEN);
    }

    @Test
    void shouldPropagateError_whenUserSaveFails() {
      // Arrange
      UpdateUserDto updateUserDto = new UpdateUserDto("John", "Doe", "john_doe", "+1234567890");

      when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
      doThrow(new RuntimeException("Database connection failed"))
          .when(userRepository).save(any(User.class));

      // Act & Assert
      assertThatThrownBy(() -> userService.updateUser(sampleAuthUser, updateUserDto))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Database connection failed");

      verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldUpdateUserSuccessfully() {
      // Arrange
      UpdateUserDto updateUserDto = new UpdateUserDto("John", "Doe", "john_doe", "+1234567890");

      when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
      when(userRepository.save(any(User.class))).thenReturn(sampleUser);

      // Act
      userService.updateUser(sampleAuthUser, updateUserDto);

      // Assert
      verify(userRepository).findById(userId);
      verify(userMapper).updateUserFromDto(updateUserDto, sampleUser);
      verify(userRepository).save(sampleUser);
    }
  }

  @Nested
  class UpdateUserLastLoginTimeTests {

    @Test
    void shouldUpdateLastLoginTimeSuccessfully() {
      // Act
      userService.updateUserLastLoginTime(userId);

      // Assert
      verify(userRepository).updateUserLastLoginTime(eq(userId), any(Instant.class));
    }
  }

  @Nested
  class DeleteUserTests {

    @Test
    void shouldThrowAppException_whenAuthUserIsNull() {
      // Act & Assert
      assertThatThrownBy(() -> userService.deleteUser(null))
          .isInstanceOf(AppException.class)
          .extracting("errorInfo")
          .isEqualTo(ErrorInfo.INVALID_TOKEN);
    }

    @Test
    void shouldDeleteUserSuccessfully() {
      // Arrange
      doNothing().when(userRepository).deleteById(userId);

      // Act
      userService.deleteUser(sampleAuthUser);

      // Assert
      verify(userRepository).deleteById(userId);
    }
  }
}
