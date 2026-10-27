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

/**
 * User lookups and profile updates.
 *
 * <p>Authentication itself (login, tokens, registration) lives in {@link com.trusthold.auth.service.AuthService};
 * this class only reads and modifies existing user records.
 *
 * <p>The {@code getXxx} lookups throw {@link UserNotFoundException} when nothing is
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
   * @throws InValidTokenException if there is no authenticated user
   * @throws UserNotFoundException if the user in the token no longer exists
   */
  public PublicUserDto getAuthenticatedUser(AuthUser authUser){
    log.debug("Getting authenticated user");
    Guard.requireNotNull(authUser, new InValidTokenException("Token is invalid"));

    User user = getUserById(authUser.id());
    return userMapper.userToPublicUserDto(user);
  }

  /**
   * Updates the authenticated user's own profile with the fields in the DTO.
   * Users can only update themselves: the target is taken from the token,
   * never from the request body.
   *
   * @throws InValidTokenException if there is no authenticated user
   * @throws UserNotFoundException if the user in the token no longer exists
   */
  @Transactional
  public void updateUser(AuthUser authUser, @NotNull UpdateUserDto updateUserDto){
    log.debug("Updating user");
    Guard.requireNotNull(authUser, new InValidTokenException("Token is invalid"));

    User user = getUserById(authUser.id());
    userMapper.updateUserFromDto(updateUserDto, user);
    userRepository.save(user);

    log.info("User profile updated: userId={}", user.getId());
  }

  /**
   * Records the current time as the user's last login.
   * Uses a direct update query, so the full user entity is not loaded.
   */
  @Transactional
  public void updateUserLastLoginTime(@NotNull UUID userId) {
    log.debug("Updating last login time: userId={}", userId);
    userRepository.updateUserLastLoginTime(userId, Instant.now());
  }

  @Transactional
  public void deleteUser(@NotNull AuthUser authUser) {
    // TODO: Have a mechanism of deleting userId after confirming though the mail.

    log.debug("Deleting user: userId={}", authUser.id());
    userRepository.deleteById(authUser.id());
  }

  // ---- Look up helpers ----

  /** @return the user, or empty if no user has this email */
  public Optional<User> getByEmail(@NotNull String email) {
    return userRepository.findByEmail(email);
  }

  /** @throws UserNotFoundException if no user has this id */
  public User getUserById(@NotNull UUID userId) {
    return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
  }

  /** @throws UserNotFoundException if no user has this email */
  public User getUserByEmail(@NotNull String email) {
    return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
  }

  /**
   * Checks whether an account exists for this email that is linked to the given
   * OAuth provider id (for example, a Google account ID).
   */
  public boolean existsByEmailAndProviderId(@NotNull String email, @NotNull String providerId) {
    return userRepository.existsByEmailAndProviderId(email, providerId);
  }
}