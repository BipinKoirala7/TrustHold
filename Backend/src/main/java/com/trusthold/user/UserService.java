package com.trusthold.user;

import com.trusthold.user.dtos.CreateUserDto;
import com.trusthold.user.exception.EmailAlreadyExists;
import com.trusthold.user.exception.UsernameAlreadyExists;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Transactional
  public void createUser(@NotNull @NonNull CreateUserDto createUserDto) {
    if(userRepository.existsByUsername(createUserDto.getUsername())) {
      log.warn("Attempt to create user with existing username: {}", createUserDto.getUsername());
      throw new UsernameAlreadyExists("Username is already taken");
    }

    if(userRepository.existsByEmail(createUserDto.getEmail())) {
      log.warn("Attempt to create user with existing email: {}", createUserDto.getEmail());
      throw new EmailAlreadyExists("A User with this email already exists");
    }

    User user = userMapper.createUserDtoToUser(createUserDto);
    user.setRole(Role.TENANT); // Set default role to USER

    userRepository.save(user);
  }
}
