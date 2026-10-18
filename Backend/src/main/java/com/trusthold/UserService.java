package com.trusthold;

import com.trusthold.user.User;
import com.trusthold.user.UserMapper;
import com.trusthold.user.dtos.CreateUserDto;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Transactional
  public void createUser(@NotNull @NonNull CreateUserDto createUserDto) {
    if(userRepository.existsByUsername(createUserDto.getUsername())) {
      throw new IllegalArgumentException("Username already exists");
    }

    if(userRepository.existsByEmail(createUserDto.getEmail())) {
      throw new IllegalArgumentException("Email already exists");
    }

    User user = userMapper.createUserDtoToUser(createUserDto);
    userRepository.save(user);
  }
}
