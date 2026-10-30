package com.trusthold.auth.handlers;

import com.trusthold.auth.dtos.RegisterOAuthUserDto;
import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.auth.service.ActiveRefreshTokenService;
import com.trusthold.auth.service.AuthService;
import com.trusthold.auth.service.CookieService;
import com.trusthold.auth.service.JwtService;
import com.trusthold.user.model.User;
import com.trusthold.user.service.UserService;
import jakarta.servlet.ServletException;
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
   * @throws ServletException if an error occurs while authenticating the user
   */
  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
    log.info("logging in oauth user");

    try{
      OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

      if (oAuth2User == null) {
        log.warn("OAuth2User is null");
        response.sendRedirect(frontendUrl + "/login?error=oauth");
        return;
      }

      String providerId = oAuth2User.getAttribute("sub");

      if (providerId == null) {
        log.warn("Provider ID is null");
        response.sendRedirect(frontendUrl + "/login?error=provider");
        return;
      }

      String email = oAuth2User.getAttribute("email");
      String givenName = oAuth2User.getAttribute("given_name");
      String familyName = oAuth2User.getAttribute("family_name");

      if(email == null) {
        log.warn("Email is null");
        response.sendRedirect(frontendUrl + "/login?error=email");
        return;
      }

      Boolean emailVerified = oAuth2User.getAttribute("email_verified");

      if (!Boolean.TRUE.equals(emailVerified)) {
        log.warn("Google email is not verified");
        response.sendRedirect(frontendUrl + "/login");
        return;
      }

      if(givenName == null) {
        log.warn("Given name is null");
        response.sendRedirect(frontendUrl + "/login?error=firstname");
        return;
      }

      if(familyName == null) {
        log.warn("Family name is null");
        response.sendRedirect(frontendUrl + "/login?error=lastname");
        return;
      }

      log.debug("Processing Google OAuth login");

      Optional<User> user = userService.getByEmail(email);

      if(user.isEmpty()) {
        log.warn("User not found");
        log.warn("Registering user first");

        HttpSession session = request.getSession(false);
        Object roleObj = session != null ? session.getAttribute("registrationRole") : null;
        if(roleObj == null) {
          log.warn("Role from the session is null");
          response.sendRedirect(frontendUrl + "/login?error=role");
          return;
        }

        var role = RegistrationRole.valueOf((String) roleObj);
        log.debug("Role from the session: {}", role);

        session.invalidate();

        String username = givenName.toLowerCase() + "_" + familyName.toLowerCase() + (int) (Math.random() * 9000 + 1000);
        // Create username until it is unique
        while(userService.existsByUsername(username)) {
          username = givenName.toLowerCase() + "_" + familyName.toLowerCase() + (int) (Math.random() * 9000 + 1000);
        }

        RegisterOAuthUserDto registerOAuthUserDto = new RegisterOAuthUserDto(providerId, givenName, familyName, username, email, role);

        loginAndRedirect(authService.registerOAuthUser(registerOAuthUserDto), response);
      } else {
        if(!Objects.equals(user.get().getProviderId(), providerId)){
          log.debug("Provider Id is not matching");
          response.sendRedirect(frontendUrl + "/login?error=provider");
          return;
        }
        loginAndRedirect(user.get(), response);
      }
    } catch (Exception e) {
      log.error("Error occurred while processing OAuth authentication", e);
      if (!response.isCommitted()) {
        response.sendRedirect(frontendUrl + "/login?error=internal");
      }
    }
  }

  private void loginAndRedirect(User user, HttpServletResponse response) throws IOException{
    String newRefreshToken = jwtService.generateRefreshToken(user);
    String newAccessToken = jwtService.generateAccessToken(user);

    activeRefreshTokenService.storeActiveRefreshToken(user.getId(), newRefreshToken);

    response.addCookie(cookieService.createRefreshTokenCookie(newRefreshToken));
    response.addCookie(cookieService.createAccessTokenCookie(newAccessToken));

    userService.updateUserLastLoginTime(user.getId());

    response.sendRedirect(frontendUrl + "/google/callback");
  }
}