package com.trusthold.auth.service;

import com.trusthold.auth.dtos.LoginUserDto;
import com.trusthold.auth.exception.IncorrectCredentialsException;
import com.trusthold.auth.exception.UserNotFoundException;
import com.trusthold.user.*;
import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.auth.exception.EmailAlreadyExistsException;
import com.trusthold.auth.exception.UsernameAlreadyExistsException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class AuthService {

  private final UserMapper userMapper;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public void registerLocalUser(@NotNull RegisterUserDto registerUserDto) {
    log.debug("Registering New Local User: {}", registerUserDto.getEmail());

    if(userRepository.existsByUsername(registerUserDto.getUsername())) {
      log.debug("Attempt to create user with existing username: {}", registerUserDto.getUsername());
      throw new UsernameAlreadyExistsException("Username is already taken");
    }

    if(userRepository.existsByEmail(registerUserDto.getEmail())) {
      log.debug("Attempt to create user with existing email: {}", registerUserDto.getEmail());
      throw new EmailAlreadyExistsException("A User with this email already exists");
    }

    User user = userMapper.createUserDtoToUser(registerUserDto);
    user.setAuthProvider(AuthProvider.LOCAL);
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    userRepository.save(user);

    log.debug("User registered successfully: {}", user.getEmail());
  }

  public void loginUser(@NotNull LoginUserDto loginUserDto) {
    log.debug("Logging In Local User : {}", loginUserDto.getEmail());

    User user = userRepository
        .findByEmail(loginUserDto.getEmail())
        .orElseThrow(() -> {
          log.debug("User with given email doesn't exists");
          return new UserNotFoundException("User with given email doesn't exists");
        });

    if(!passwordEncoder.matches(loginUserDto.getPassword(), user.getPassword())) {
      log.debug("Invalid password for user");
      throw new IncorrectCredentialsException("Invalid password");
    }

    // Todo: Create JWT token and create cookie and add that to response object and return
    log.debug("User logged in successfully");
  }
}
