package com.trusthold.auth;

import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.auth.exception.EmailAlreadyExistsException;
import com.trusthold.auth.exception.UsernameAlreadyExistsException;
import com.trusthold.user.*;
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
    registerUserDto = new RegisterUserDto();
    registerUserDto.setFirstName("Test");
    registerUserDto.setLastName("User");
    registerUserDto.setUsername("testUser_123");
    registerUserDto.setEmail("testuser@example.com");
    registerUserDto.setPassword("testPassword@123");
    registerUserDto.setRegistrationRole(RegistrationRole.TENANT);

    user = new User();
    user.setId(UUID.randomUUID());
    user.setFirstName(registerUserDto.getFirstName());
    user.setLastName(registerUserDto.getLastName());
    user.setUsername(registerUserDto.getUsername());
    user.setEmail(registerUserDto.getEmail());
    user.setPassword(registerUserDto.getPassword());
    user.setRole(Role.valueOf(registerUserDto.getRegistrationRole().name()));

  }

  @Nested
  class RegisterLocalUserTests {

    @Test
    void shouldThrowUsernameAlreadyExists_whenUsernameIsTaken() {
      // Arrange
      when(userRepository.existsByUsername(registerUserDto.getUsername())).thenReturn(true);

      // Act & Assert
      assertThrows(UsernameAlreadyExistsException.class, () -> underTest.registerLocalUser(registerUserDto));
      verify(userRepository).existsByUsername(registerUserDto.getUsername());
      verify(userRepository, never()).existsByEmail(registerUserDto.getEmail());
      verify(userMapper, never()).createUserDtoToUser(registerUserDto);
      verify(passwordEncoder, never()).encode(registerUserDto.getPassword());
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowEmailAlreadyExists_whenEmailIsTaken() {
      // Arrange
      when(userRepository.existsByUsername(registerUserDto.getUsername())).thenReturn(false);
      when(userRepository.existsByEmail(registerUserDto.getEmail())).thenReturn(true);

      // Act & Assert
      assertThrows(EmailAlreadyExistsException.class, () -> underTest.registerLocalUser(registerUserDto));
      verify(userRepository).existsByUsername(registerUserDto.getUsername());
      verify(userRepository).existsByEmail(registerUserDto.getEmail());
      verify(userMapper, never()).createUserDtoToUser(registerUserDto);
      verify(passwordEncoder, never()).encode(registerUserDto.getPassword());
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldSaveUserWithEncodedPassword_whenRegistrationIsValid() {
      // Arrange
      String encodedPassword = "encodedPassword";
      when(userRepository.existsByUsername(registerUserDto.getUsername())).thenReturn(false);
      when(userRepository.existsByEmail(registerUserDto.getEmail())).thenReturn(false);
      when(userMapper.createUserDtoToUser(registerUserDto)).thenReturn(user);
      when(passwordEncoder.encode(registerUserDto.getPassword())).thenReturn(encodedPassword);

      // Act
      underTest.registerLocalUser(registerUserDto);

      // Assert
      verify(passwordEncoder, times(1)).encode(registerUserDto.getPassword());
      verify(userRepository).save(userArgumentCaptor.capture());

      User savedUser = userArgumentCaptor.getValue();

      assertSame(user, savedUser);
      assertEquals(encodedPassword, savedUser.getPassword());
      assertEquals(AuthProvider.LOCAL, savedUser.getAuthProvider());
    }
  }
}
