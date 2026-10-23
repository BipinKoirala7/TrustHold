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
    log.debug("logging in oauth user");

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

    if (oAuth2User == null) {
      log.warn("OAuth2User is null");
      response.sendRedirect(frontendUrl + "/login");
      return;
    }

    String providerId = oAuth2User.getAttribute("sub");

    if (providerId == null) {
      log.warn("Provider ID is null");
      response.sendRedirect(frontendUrl + "/login");
      return;
    }

    String email = oAuth2User.getAttribute("email");
    String givenName = oAuth2User.getAttribute("given_name");
    String familyName = oAuth2User.getAttribute("family_name");
    String imageUrl = oAuth2User.getAttribute("picture");

    if(email == null) {
      log.warn("Email is null");
      response.sendRedirect(frontendUrl + "/login");
      return;
    }

    if(givenName == null) {
      log.warn("Given name is null");
      response.sendRedirect(frontendUrl + "/login");
      return;
    }

    if(familyName == null) {
      log.warn("Family name is null");
      response.sendRedirect(frontendUrl + "/login");
      return;
    }

    log.warn("Email: {}", email);
    log.warn("Given name: {}", givenName);
    log.warn("Family name: {}", familyName);
    log.warn("Image URL: {}", imageUrl);

    Optional<User> user = userService.getByEmail(email);

    if(user.isEmpty()) {
      log.warn("User not found");
      log.warn("Registering user first");

      Object roleObj = request.getSession(false).getAttribute("registrationRole");
      if(roleObj == null) {
        log.warn("Role from the session is null");
        response.sendRedirect(frontendUrl + "/login");
        return;
      }

      var role = RegistrationRole.valueOf((String) roleObj);
      log.warn("Role from the session: {}", role);


      String username = givenName.toLowerCase() + "_" + familyName.toLowerCase();
      RegisterOAuthUserDto registerOAuthUserDto = new RegisterOAuthUserDto(providerId, givenName, familyName, username, email, role);

      User registerdOAuthUser = authService.registerOAuthUser(registerOAuthUserDto);
      String newRefreshToken = jwtService.generateRefreshToken(registerdOAuthUser);
      String newAccessToken = jwtService.generateAccessToken(registerdOAuthUser);

      activeRefreshTokenService.storeActiveRefreshToken(registerdOAuthUser.getId(), newRefreshToken);
      response.addCookie(cookieService.createRefreshTokenCookie(newRefreshToken));
      response.addCookie(cookieService.createAccessTokenCookie(newAccessToken));

      userService.updateUserLastLoginTime(registerdOAuthUser.getId());

      response.sendRedirect(frontendUrl + "/dashboard");
    } else {
      if(!Objects.equals(user.get().getProviderId(), providerId)){
        log.warn("Token is invalid");
        response.sendRedirect(frontendUrl + "/login");
        return;
      }
      String newRefreshToken = jwtService.generateRefreshToken(user.get());
      String newAccessToken = jwtService.generateAccessToken(user.get());

      activeRefreshTokenService.storeActiveRefreshToken(user.get().getId(), newRefreshToken);

      response.addCookie(cookieService.createRefreshTokenCookie(newRefreshToken));
      response.addCookie(cookieService.createAccessTokenCookie(newAccessToken));

      userService.updateUserLastLoginTime(user.get().getId());

      response.sendRedirect(frontendUrl + "/dashboard");
    }
  }
}