package com.trusthold.auth;

import com.trusthold.user.*;
import com.trusthold.user.dtos.RegisterUserDto;
import com.trusthold.user.exception.EmailAlreadyExists;
import com.trusthold.user.exception.UsernameAlreadyExists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class AuthService {

  private final UserMapper userMapper;
  private final UserService userService;
  private final UserRepository userRepository;

  public void registerLocalUser(RegisterUserDto registerUserDto) {
    if(userRepository.existsByUsername(registerUserDto.getUsername())) {
      log.warn("Attempt to create user with existing username: {}", registerUserDto.getUsername());
      throw new UsernameAlreadyExists("Username is already taken");
    }

    if(userRepository.existsByEmail(registerUserDto.getEmail())) {
      log.warn("Attempt to create user with existing email: {}", registerUserDto.getEmail());
      throw new EmailAlreadyExists("A User with this email already exists");
    }

    User user = userMapper.createUserDtoToUser(registerUserDto);
    user.setAuthProvider(AuthProvider.LOCAL);

    userRepository.save(user);
  }
}
