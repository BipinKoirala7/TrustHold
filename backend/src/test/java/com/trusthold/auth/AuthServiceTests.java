package com.trusthold.auth;

import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.auth.exception.EmailAlreadyExistsException;
import com.trusthold.auth.exception.UsernameAlreadyExistsException;
import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.auth.service.AuthService;
import com.trusthold.user.model.AuthProvider;
import com.trusthold.user.model.Role;
import com.trusthold.user.model.User;
import com.trusthold.user.repository.UserRepository;
import com.trusthold.user.service.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {

  @Mock
  private UserMapper userMapper;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private AuthService underTest;

  private RegisterUserDto registerUserDto;
  private User user;

  @Captor
  private ArgumentCaptor<User> userArgumentCaptor;

  @BeforeEach
  public void setUp() {
    registerUserDto = new RegisterUserDto(
        "Test",
        "User",
        "testUser_123",
        "testuser@example.com",
        "testPassword@123",
        "+1234567890",
        RegistrationRole.TENANT
    );

    user = new User();
    user.setId(UUID.randomUUID());
    user.setFirstName(registerUserDto.firstName());
    user.setLastName(registerUserDto.lastName());
    user.setUsername(registerUserDto.username());
    user.setEmail(registerUserDto.email());
    user.setPassword(registerUserDto.password());
    user.setRole(Role.valueOf(registerUserDto.registrationRole().name()));

  }

  @Nested
  class RegisterLocalUserTests {

    @Test
    void shouldThrowUsernameAlreadyExists_whenUsernameIsTaken() {
      // Arrange
      when(userRepository.existsByUsername(registerUserDto.username())).thenReturn(true);

      // Act & Assert
      assertThrows(UsernameAlreadyExistsException.class, () -> underTest.registerLocalUser(registerUserDto));
      verify(userRepository).existsByUsername(registerUserDto.username());
      verify(userRepository, never()).existsByEmail(registerUserDto.email());
      verify(userMapper, never()).createUserDtoToUser(registerUserDto);
      verify(passwordEncoder, never()).encode(registerUserDto.password());
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowEmailAlreadyExists_whenEmailIsTaken() {
      // Arrange
      when(userRepository.existsByUsername(registerUserDto.username())).thenReturn(false);
      when(userRepository.existsByEmail(registerUserDto.email())).thenReturn(true);

      // Act & Assert
      assertThrows(EmailAlreadyExistsException.class, () -> underTest.registerLocalUser(registerUserDto));
      verify(userRepository).existsByUsername(registerUserDto.username());
      verify(userRepository).existsByEmail(registerUserDto.email());
      verify(userMapper, never()).createUserDtoToUser(registerUserDto);
      verify(passwordEncoder, never()).encode(registerUserDto.password());
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldSaveUserWithEncodedPassword_whenRegistrationIsValid() {
      // Arrange
      String encodedPassword = "encodedPassword";
      when(userRepository.existsByUsername(registerUserDto.username())).thenReturn(false);
      when(userRepository.existsByEmail(registerUserDto.email())).thenReturn(false);
      when(userMapper.createUserDtoToUser(registerUserDto)).thenReturn(user);
      when(passwordEncoder.encode(registerUserDto.password())).thenReturn(encodedPassword);

      // Act
      underTest.registerLocalUser(registerUserDto);

      // Assert
      verify(passwordEncoder, times(1)).encode(registerUserDto.password());
      verify(userRepository).save(userArgumentCaptor.capture());

      User savedUser = userArgumentCaptor.getValue();

      assertSame(user, savedUser);
      assertEquals(encodedPassword, savedUser.getPassword());
      assertEquals(AuthProvider.LOCAL, savedUser.getAuthProvider());
    }
  }
}
