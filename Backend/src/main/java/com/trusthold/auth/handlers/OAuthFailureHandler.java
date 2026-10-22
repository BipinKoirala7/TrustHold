package com.trusthold.auth.handlers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class OAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

  @Value("${app.frontend.url}")
  private String frontendUrl;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
    log.error("Authentication failed for OAuth with message: {}", exception.getMessage());

    response.sendRedirect(frontendUrl + "/login?error=true");
  }
}
