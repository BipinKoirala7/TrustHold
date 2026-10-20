package com.trusthold.user;

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

  @Transactional
  public void updateUserLastLoginTime(@NotNull UUID userId) {
    log.debug("Updating user's last login time");
    userRepository.updateUserLastLoginTime(userId, Instant.now());
  }
}
