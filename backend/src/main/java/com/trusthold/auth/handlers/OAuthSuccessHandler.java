package com.trusthold.auth.handlers;

import com.trusthold.auth.dtos.RegisterOAuthUserDto;
import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.auth.service.ActiveRefreshTokenService;
import com.trusthold.auth.service.AuthService;
import com.trusthold.auth.service.CookieService;
import com.trusthold.auth.service.JwtService;
import com.trusthold.user.model.User;
import com.trusthold.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Handles successful OAuth authentication.
 *
 * <p>
 *   This handler is responsible for processing successful OAuth authentication attempts.
 *   It retrieves user information, generates access and refresh tokens, and sets them as cookies.
 * </p>
 * <p>
 *   If a user is not found in the database, it creates a new user in the database and then proceeds to
 *   log in the saved user.
 * </p>
 *
 * @see UserService
 * @see AuthService
 * @see JwtService
 * @see CookieService
 * @see ActiveRefreshTokenService
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final UserService userService;
  private final AuthService authService;
  private final JwtService jwtService;
  private final CookieService cookieService;
  private final ActiveRefreshTokenService activeRefreshTokenService;

  @Value("${app.frontend.url}")
  private String frontendUrl;

  /**
   * Handles when the OAuth authentication is successful for the oauth provider part.
   *
   * @param request the request that caused the successful authentication
   * @param response the response
   * @param authentication the <tt>Authentication</tt> object which was created during
   * the authentication process.
   * @throws IOException if an I/O error occurs while authenticating the user
   */
  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
    log.debug("logging in oauth user");

    try{
      OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

      if (oAuth2User == null) {
        fail(response, "OAuth User is null", "user");
        return;
      }

      String providerId = oAuth2User.getAttribute("sub");

      if (providerId == null) {
        fail(response, "Provider ID is null", "provider");
        return;
      }

      String email = oAuth2User.getAttribute("email");
      String givenName = oAuth2User.getAttribute("given_name");
      String familyName = oAuth2User.getAttribute("family_name");

      if(email == null || email.isBlank()) {
        fail(response, "Email is null", "email");
        return;
      }

      Boolean emailVerified = oAuth2User.getAttribute("email_verified");

      if (!Boolean.TRUE.equals(emailVerified)) {
        fail(response, "Google email is not verified", null);
        return;
      }

      if(givenName == null || givenName.isBlank()) {
        fail(response, "Given name is null", "firstname");
        return;
      }

      if(familyName == null || familyName.isBlank()) {
        fail(response, "Family name is null", "lastname");
        return;
      }

      log.debug("Processing Google OAuth login");

      Optional<User> user = userService.getByEmail(email);

      if(user.isEmpty()) {
        log.debug("User not found");
        log.debug("Registering user first before logging in");

        HttpSession session = request.getSession(false);
        Object roleObj = session != null ? session.getAttribute("registrationRole") : null;

        if(roleObj == null) {
          fail(response, "registration role is null", "role");
          return;
        }

        RegistrationRole role;
        try {
          role = RegistrationRole.valueOf((String) roleObj);
          log.debug("Role from the session: {}", role);
          session.invalidate();
        } catch (IllegalArgumentException | ClassCastException e) {
          fail(response, "invalid registration role in session", "role");
          return;
        }

        String username = givenName.toLowerCase() + "_" + familyName.toLowerCase() + (int) (Math.random() * 9000 + 1000);
        // Create username until it is unique
        while(userService.existsByUsername(username)) {
          username = givenName.toLowerCase() + "_" + familyName.toLowerCase() + (int) (Math.random() * 9000 + 1000);
        }

        RegisterOAuthUserDto registerOAuthUserDto = new RegisterOAuthUserDto(providerId, givenName, familyName, username, email, role);
        loginAndRedirect(authService.registerOAuthUser(registerOAuthUserDto), response);
        log.info("OAuth User registered and logged in");
      } else {
        if (user.get().getProviderId() == null) {
          fail(response, "account {} exists without linked provider", "wrong_provider", user.get().getId());
          return;
        }

        if (!Objects.equals(user.get().getProviderId(), providerId)) {
          fail(response, "provider id mismatch for user {}", "provider", user.get().getId());
          return;
        }

        loginAndRedirect(user.get(), response);
        log.info("OAuth User logged in for user: userId={}", user.get().getId());
      }
    } catch (Exception e) {
      log.error("OAuth login failed unexpectedly", e);
      if (!response.isCommitted()) {
        response.sendRedirect(frontendUrl + "/login?error=internal");
      }
    }
  }

  private void loginAndRedirect(User user, HttpServletResponse response) throws IOException{
    log.info("Logging in user: userId={}", user.getId());
    String newAccessToken = jwtService.generateAccessToken(user);
    String newRefreshToken = jwtService.generateRefreshToken(user);

    activeRefreshTokenService.storeActiveRefreshToken(user.getId(), newRefreshToken);
    userService.updateUserLastLoginTime(user.getId());

    response.addCookie(cookieService.createRefreshTokenCookie(newRefreshToken));
    response.addCookie(cookieService.createAccessTokenCookie(newAccessToken));

    log.info("OAuth login succeeded: userId={}, provider=google", user.getId());
    response.sendRedirect(frontendUrl + "/google/callback");
  }

  private void fail(HttpServletResponse response, String reason, String errorCode, Object... ctx) throws IOException {
    log.warn("OAuth login rejected: " + reason, ctx);
    response.sendRedirect(frontendUrl + "/login" + (errorCode != null ? "?error=" + errorCode : ""));
  }
}