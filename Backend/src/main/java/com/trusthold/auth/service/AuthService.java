package com.trusthold.auth.service;

import com.trusthold.auth.dtos.LoginUserDto;
import com.trusthold.auth.exception.IncorrectCredentialsException;
import com.trusthold.auth.exception.UserNotFoundException;
import com.trusthold.user.*;
import com.trusthold.auth.dtos.RegisterUserDto;
import com.trusthold.auth.exception.EmailAlreadyExistsException;
import com.trusthold.auth.exception.UsernameAlreadyExistsException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

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

  @Transactional
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

  @Transactional
  public void loginUser(@NotNull LoginUserDto loginUserDto, HttpServletResponse response) {
    log.debug("Logging In User : {}", loginUserDto.getEmail());

    User user = userRepository
        .findByEmail(loginUserDto.getEmail())
        .orElseThrow(() -> {
          log.debug("Logging In User Failed - User with given email doesn't exists");
          return new UserNotFoundException("User with given email doesn't exists");
        });

    if(!passwordEncoder.matches(loginUserDto.getPassword(), user.getPassword())) {
      log.debug("Logging In User Failed - Invalid password for user");
      throw new IncorrectCredentialsException("Invalid password");
    }

    // Create Jwt Tokens
    String newRefreshToken = jwtService.generateRefreshToken(user);
    String newAccessToken = jwtService.generateAccessToken(user);

    // Create and Add cookies to the response object with token inside them
    response.addCookie(cookieService.createRefreshTokenCookie(newRefreshToken));
    response.addCookie(cookieService.createAccessTokenCookie(newAccessToken));

    // Storing active refresh token
    activeRefreshTokenService.storeActiveRefreshToken(user.getId(), newRefreshToken);

    // Update user's last login time
    userService.updateUserLastLoginTime(user.getId());

    log.debug("User logged in successfully");
  }

  @Transactional
  public void logoutUser(HttpServletResponse response){
    log.debug("Logging Out User");

    // replace it with User id and refresh Token from Security Context
    UUID userId = UUID.randomUUID();
    String refreshToken = "";

    // Delete all the cookies from response
    cookieService.deleteRefreshTokenCookie(response);
    cookieService.deleteAccessTokenCookie(response);

    // Remove the active refresh token
    activeRefreshTokenService.removeActiveRefreshToken(userId, refreshToken);
  }
}
