package com.trusthold.auth.handlers;

import com.trusthold.auth.dtos.RegisterOAuthUserDto;
import com.trusthold.auth.exception.InValidTokenException;
import com.trusthold.auth.exception.UserNotFoundException;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final UserService userService;
  private final AuthService authService;
  private final JwtService jwtService;
  private final CookieService cookieService;

  @Value("${app.frontend.url}")
  private String frontendUrl;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
    log.info("OAuth Success Handler...");

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

    if (oAuth2User == null) {
      log.warn("OAuth2User is null");
      response.sendRedirect(frontendUrl + "/login");
      return;
    }

    String providerId = oAuth2User.getAttribute("providerId");

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

    // Todo: First check with email and then move with providerId if that matches or not
    Optional<User> user = userService.getByEmail(email);

    if(user.isEmpty()) {
      log.warn("User not found");

      RegisterOAuthUserDto registerOAuthUserDto = new RegisterOAuthUserDto();
      registerOAuthUserDto.setProviderId(providerId);
      registerOAuthUserDto.setFirstName(givenName);
      registerOAuthUserDto.setLastName(familyName);
      registerOAuthUserDto.setEmail(email);

      authService.registerOAuthUser(registerOAuthUserDto);
    } else {
      if(!Objects.equals(user.get().getProviderId(), providerId)){
        log.warn("Token is invalid");
        response.sendRedirect(frontendUrl + "/login");
        return;
      }
    }

    String newRefreshToken = jwtService.generateRefreshToken(user.get());
    String newAccessToken = jwtService.generateAccessToken(user.get());

    response.addCookie(cookieService.createRefreshTokenCookie(newRefreshToken));
    response.addCookie(cookieService.createAccessTokenCookie(newAccessToken));

    response.sendRedirect(frontendUrl + "/dashboard");
    log.warn("User found: {}", user.get().getEmail());
  }
}