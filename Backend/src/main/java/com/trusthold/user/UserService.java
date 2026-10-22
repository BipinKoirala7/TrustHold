package com.trusthold.user;

import com.trusthold.auth.exception.UserNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;


  public User getUserById(@NotNull UUID userId) {
    return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
  }

  public User getUserByEmail(@NotNull String email) {
    return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
  }

  @Transactional
  public void updateUserLastLoginTime(@NotNull UUID userId) {
    log.debug("Updating user's last login time");
    userRepository.updateUserLastLoginTime(userId, Instant.now());
  }
}
