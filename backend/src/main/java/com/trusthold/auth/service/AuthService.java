package com.trusthold.auth.service;

import com.trusthold.auth.dtos.LoginUserDto;
import com.trusthold.auth.dtos.RegisterOAuthUserDto;
import com.trusthold.auth.exception.*;
import com.trusthold.auth.model.AuthUser;
import com.trusthold.mail.EventListener;
import com.trusthold.mail.MailService;
import com.trusthold.mail.UserRegisteredEvent;
import com.trusthold.shared.Guard;
import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.user.model.AuthProvider;
import com.trusthold.user.model.User;
import com.trusthold.user.repository.UserRepository;
import com.trusthold.user.service.UserMapper;
import com.trusthold.user.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Handles authentication use case: local and Google OAuth user registration, login
 * logout, and token refresh
 *
 * <p>Tokens are delivered as HttpOnly cookies (see {@link CookieService}), never in
 * response bodies. Each user has exactly one active refresh token at a time
 * (see {@link ActiveRefreshTokenService}), so logging in again on a second
 * device replaces the first session's refresh token.
 * </p>
 *
 * <p>User Mapper maps the dtos to entities (see {@link UserMapper}).</p>
 * <p>Token creation and verification lives in {@link JwtService}. This class only uses it.</p>
 *
 * @see UserService
 * @see UserRepository
 * @see UserMapper
 * @see PasswordEncoder
 * @see JwtService
 * @see CookieService
 * @see ActiveRefreshTokenService
 */
@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class AuthService {

  private final UserMapper userMapper;
  private final UserService userService;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final CookieService cookieService;
  private final ActiveRefreshTokenService activeRefreshTokenService;
  private final ApplicationEventPublisher applicationEventPublisher;

  /**
   * Registers a user with email and password.
   * The password is hashed before saving; the raw value is never stored or logged.
   *
   * @param registerUserDto the new user's registration details
   * @throws UsernameAlreadyExistsException if the username is taken
   * @throws EmailAlreadyExistsException    if the email is already registered
   */
  @Transactional
  public void registerLocalUser(@NotNull RegisterUserDto registerUserDto) {
    log.debug("Registering local user");

    if(userRepository.existsByUsername(registerUserDto.username())) {
      log.debug("Registration local user rejected: username already taken");
      throw new UsernameAlreadyExistsException("Username is already taken");
    }

    if(userRepository.existsByEmail(registerUserDto.email())) {
      log.debug("Registration local user rejected: email already taken");
      throw new EmailAlreadyExistsException("A User with this email already exists");
    }

    User user = userMapper.createUserDtoToUser(registerUserDto);
    user.setAuthProvider(AuthProvider.LOCAL);
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    User savedUser = userRepository.save(user);

    log.info("User registered: userId={}, provider={}", savedUser.getId(), AuthProvider.LOCAL);

    log.info("Sending confirmation email  to {}", savedUser.getEmail());
    applicationEventPublisher.publishEvent(new UserRegisteredEvent(savedUser.getEmail(), savedUser.getRole().toString(), savedUser.getUsername()));
  }

  /**
   * Creates a user from Google account details. No password is set, because
   * Google verifies the identity.
   *
   * @return the saved user, so the OAuth success handler can issue tokens for it
   * @throws UsernameAlreadyExistsException if the username is taken
   * @throws EmailAlreadyExistsException    if the email is already registered
   */
  @Transactional
  public User registerOAuthUser(@NotNull RegisterOAuthUserDto registerOAuthUserDto){
    log.debug("Registering OAuth User");

    if(userRepository.existsByUsername(registerOAuthUserDto.username())) {
      log.debug("OAuth registration rejected: username already taken");
      throw new UsernameAlreadyExistsException("Username is already taken");
    }

    if(userRepository.existsByEmail(registerOAuthUserDto.email())) {
      log.debug("OAuth registration rejected: email already taken");
      throw new EmailAlreadyExistsException("A User with this email already exists");
    }

    User user = userMapper.createOAuthUserDtoToUser(registerOAuthUserDto);
    user.setAuthProvider(AuthProvider.GOOGLE);
    User savedUser = userRepository.save(user);
    log.info("OAuth User registered: userId={}, provider={}", savedUser.getId(), AuthProvider.GOOGLE);

    log.info("Sending confirmation email to {}", savedUser.getEmail());
    applicationEventPublisher.publishEvent(new UserRegisteredEvent(savedUser.getEmail(), savedUser.getRole().toString(), savedUser.getUsername()));

    return savedUser;
  }

  /**
   * Authenticates a local user and starts a session.
   *
   * <p>On success: access and refresh tokens are written to cookies on the response,
   * the refresh token becomes the user's single active one, and the last-login
   * time is updated.
   *
   * @param loginUserDto email and raw password from the request
   * @param response     receives the token cookies
   * @throws UserNotFoundException         if no user has this email
   * @throws IncorrectCredentialsException if the password does not match
   */
  @Transactional
  public void loginUser(@NotNull LoginUserDto loginUserDto, HttpServletResponse response) {
    log.debug("Login attempt");

    User user = userService.getUserByEmail(loginUserDto.email());

    if(!passwordEncoder.matches(loginUserDto.password(), user.getPassword())) {
      log.warn("Login failed: wrong password, userId={}", user.getId());
      throw new IncorrectCredentialsException("Invalid password");
    }

    String newRefreshToken = jwtService.generateRefreshToken(user);
    String newAccessToken = jwtService.generateAccessToken(user);

    activeRefreshTokenService.storeActiveRefreshToken(user.getId(), newRefreshToken);
    userService.updateUserLastLoginTime(user.getId());

    response.addCookie(cookieService.createRefreshTokenCookie(newRefreshToken));
    response.addCookie(cookieService.createAccessTokenCookie(newAccessToken));

    log.info("User logged in: userId={}", user.getId());
  }

  /**
   * Issues a new access/refresh token pair from the refresh-token cookie.
   * The old refresh token is invalidated (rotation), so a stolen token can be
   * used at most once.
   *
   * @throws JwtException          if the cookie is missing, or the token is expired/tampered with
   * @throws InValidTokenException if the token is not a refresh token or is no longer active
   */
  @Transactional
  public void refreshToken(HttpServletRequest request, HttpServletResponse response){
    log.debug("Refreshing Token");

    String refreshToken = cookieService.getRefreshTokenCookie(request);

    if(refreshToken == null) throw new JwtException("Refresh token is null");
    if(activeRefreshTokenService.isRefreshTokenActive(refreshToken)) throw new JwtException("Refresh token is not active");

    User user = jwtService.validateRefreshToken(refreshToken);
    String newRefreshToken = jwtService.generateRefreshToken(user);
    String newAccessToken = jwtService.generateAccessToken(user);

    activeRefreshTokenService.removeActiveRefreshToken(user.getId());
    activeRefreshTokenService.storeActiveRefreshToken(user.getId(), newRefreshToken);

    response.addCookie(cookieService.createRefreshTokenCookie(newRefreshToken));
    response.addCookie(cookieService.createAccessTokenCookie(newAccessToken));

    log.info("Token refreshed successfully");
  }

  /**
   * Ends the session: deletes both token cookies and invalidates the active
   * refresh token, so it cannot be used again even if it was copied elsewhere.
   *
   * @throws InValidTokenException if there is no authenticated user
   */
  @Transactional
  public void logoutUser(HttpServletResponse response, AuthUser authUser){
    log.info("Logging Out User");

    Guard.requireNotNull(authUser, new InValidTokenException("Token is invalid"));

    cookieService.deleteRefreshTokenCookie(response);
    cookieService.deleteAccessTokenCookie(response);
    activeRefreshTokenService.removeActiveRefreshToken(authUser.id());
  }
}
