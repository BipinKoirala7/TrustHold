package com.trusthold.user.service;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.shared.ErrorInfo;
import com.trusthold.shared.Guard;
import com.trusthold.shared.exception.AppException;
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

/**
 * User lookups and profile updates.
 *
 * <p>Authentication itself (login, tokens, registration) lives in {@link com.trusthold.auth.service.AuthService};
 * this class only reads and modifies existing user records.
 *
 * <p>The {@code getXxx} lookups throw {@link AppException} when nothing is
 * found. The {@code Optional}-returning lookup is for callers that need to handle
 * "not found" themselves (for example, deciding between login and registration).
 */
@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  /**
   * Returns the profile of the currently authenticated user.
   * The result is a {@link PublicUserDto}, so internal fields such as the
   * password hash are never exposed.
   *
   * @throws AppException if there is no authenticated user or the user is not found
   */
  public PublicUserDto getAuthenticatedUser(AuthUser authUser){
    log.debug("Getting authenticated user");
    Guard.requireNotNull(authUser, new AppException(ErrorInfo.INVALID_TOKEN));

    User user = getUserById(authUser.id());
    return userMapper.userToPublicUserDto(user);
  }

  /**
   * Updates the authenticated user's own profile with the fields in the DTO.
   * Users can only update themselves: the target is taken from the token,
   * never from the request body.
   *
   * @throws AppException if there is no authenticated user or the user is not found
   */
  @Transactional
  public void updateUser(AuthUser authUser, @NotNull UpdateUserDto updateUserDto){
    log.debug("Updating user");
    Guard.requireNotNull(authUser, new AppException(ErrorInfo.INVALID_TOKEN));

    User user = getUserById(authUser.id());
    userMapper.updateUserFromDto(updateUserDto, user);
    userRepository.save(user);

    log.info("User profile updated: userId={}", user.getId());
  }

  /**
   * Records the current time as the user's last login.
   * Uses a direct update query, so the full user entity is not loaded.
   *
   * @param userId the id of the user to update
   */
  @Transactional
  public void updateUserLastLoginTime(@NotNull UUID userId) {
    log.debug("Updating last login time: userId={}", userId);
    userRepository.updateUserLastLoginTime(userId, Instant.now());
  }

  /**
   * Deletes a user's account and all associated data.
   *
   * @param authUser an authenticated user
   */
  @Transactional
  public void deleteUser(@NotNull AuthUser authUser) {
    // TODO: Have a mechanism of deleting userId after confirming though the email.

    log.debug("Deleting user: userId={}", authUser.id());
    userRepository.deleteById(authUser.id());
  }

  // ---- Look up helpers ----

  /** @return the user, or empty if no user has this email */
  public Optional<User> getByEmail(@NotNull String email) {
    return userRepository.findByEmail(email);
  }

  /** @throws AppException if no user has this id */
  public User getUserById(@NotNull UUID userId) {
    return userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorInfo.USER_NOT_FOUND));
  }

  /** @throws AppException if no user has this email */
  public User getUserByEmail(@NotNull String email) {
    return userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorInfo.USER_NOT_FOUND));
  }

  /**
   * Checks whether an account exists for this email that is linked to the given
   * OAuth provider id (for example, a Google account ID).
   */
  public boolean existsByEmailAndProviderId(@NotNull String email, @NotNull String providerId) {
    return userRepository.existsByEmailAndProviderId(email, providerId);
  }
}