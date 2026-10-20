package com.trusthold.auth.filters;

import com.trusthold.auth.exception.InValidTokenException;
import com.trusthold.auth.model.JwtAuthenticationToken;
import com.trusthold.auth.service.CookieService;
import com.trusthold.auth.service.JwtService;
import com.trusthold.shared.dtos.ApiResponse;
import com.trusthold.user.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Component
public class AccessTokenFilter extends OncePerRequestFilter {

  private final ObjectMapper objectMapper;
  private final CookieService cookieService;
  private final JwtService jwtService;

  private final List<String> SHOULD_NOT_FILTER = List.of(
      "/auth/", "/oauth2/", "/login/", "/actuator/", "/oauth/", "/swagger-ui/", "/swagger-ui.html", "v3/api-docs"
  );

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    return SHOULD_NOT_FILTER.stream().anyMatch(path::startsWith);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

    log.debug("Request Start ...");
    log.debug("Request path: {}", request.getServletPath());

    try {
      String accessToken = cookieService.getAccessTokenCookie(request);
      if (accessToken == null) {
        log.debug("Access token cookie not found");
        throw new JwtException("Access token cookie not found");
      }

      Claims claims = jwtService.extractAccessClaims(accessToken);
      String roleClaim = claims.get("role", String.class);

      if (roleClaim == null) throw new JwtException("Missing role");

      UUID userId = UUID.fromString(claims.getSubject());
      Role role = Role.valueOf(roleClaim);

      SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(userId, accessToken, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
      log.debug("Access token validated and user authenticated");
    } catch (JwtException | InValidTokenException | IllegalArgumentException e) {
      log.error("Error validating access token", e);
      SecurityContextHolder.clearContext();
      sendErrorResponse(response, "Please log in");
      return;
    }
    filterChain.doFilter(request, response);
  }

  private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
    if(response.isCommitted()){
      logger.debug("Response is already committed, cannot send error response");
      return;
    }

    log.debug("Sending error response: {}", message);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(message)));
  }
}
