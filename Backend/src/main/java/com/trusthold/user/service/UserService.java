package com.trusthold.user.service;

import com.trusthold.auth.exception.InValidTokenException;
import com.trusthold.auth.exception.UserNotFoundException;
import com.trusthold.auth.model.AuthUser;
import com.trusthold.shared.Guard;
import com.trusthold.user.dtos.PublicUserDto;
import com.trusthold.user.dtos.UpdateUserDto;
import com.trusthold.user.model.User;
import com.trusthold.user.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public PublicUserDto getAuthenticatedUser(AuthUser authUser){
    log.debug("Getting authenticated user");
    Guard.requireNotNull(authUser, new InValidTokenException("Token is invalid"));

    User user = getUserById(authUser.id());
    return userMapper.userToPublicUserDto(user);
  }

  public void updateUser(AuthUser authUser, @NotNull UpdateUserDto updateUserDto){
    log.debug("Updating user");
    Guard.requireNotNull(authUser, new InValidTokenException("Token is invalid"));

    User user = getUserById(authUser.id());
    userMapper.updateUserFromDto(updateUserDto, user);
    userRepository.save(user);
  }

  @Transactional
  public void updateUserLastLoginTime(@NotNull UUID userId) {
    log.debug("Updating user's last login time");
    userRepository.updateUserLastLoginTime(userId, Instant.now());
  }

  // Helper methods
  public Optional<User> getByEmail(@NotNull String email) {
    return userRepository.findByEmail(email);
  }

  public User getUserById(@NotNull UUID userId) {
    return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
  }

  public User getUserByEmail(@NotNull String email) {
    return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
  }

  public boolean existsByEmailAndProviderId(@NotNull String email, @NotNull String providerId) {
    return userRepository.existsByEmailAndProviderId(email, providerId);
  }
}
