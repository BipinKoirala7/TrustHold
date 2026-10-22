package com.trusthold.auth.filters;

import com.trusthold.auth.exception.InValidTokenException;
import com.trusthold.auth.model.AuthUser;
import com.trusthold.auth.service.CookieService;
import com.trusthold.auth.service.JwtService;
import com.trusthold.shared.dtos.ApiResponse;
import com.trusthold.user.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
      "/auth/login", "/auth/register", "/oauth/", "/oauth2",
      "/actuator/",
      "/swagger-ui/", "/swagger-ui.html", "/v3/api-docs"
  );

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    return SHOULD_NOT_FILTER.stream().anyMatch(path::startsWith);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

    log.warn("Request Start ...");
    log.warn("Request path: {}", request.getServletPath());

    try {
      String accessToken = cookieService.getAccessTokenCookie(request);
      if (accessToken == null) {
        log.warn("Access token cookie not found");
        throw new JwtException("Access token cookie not found");
      }

      Claims claims = jwtService.extractAccessClaims(accessToken);
      String roleClaim = claims.get("role", String.class);

      if (roleClaim == null) {
        log.warn("Role is not present in the token claim");
        throw new JwtException("Missing role");
      }

      AuthUser authUser = new AuthUser(UUID.fromString(claims.getSubject()), accessToken, Role.valueOf(roleClaim));
      var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + authUser.role().toString()));
      var auth = new UsernamePasswordAuthenticationToken(authUser, null, authorities);

      SecurityContextHolder.getContext().setAuthentication(auth);
      log.warn("Access token validated and user authenticated");
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
      log.warn("Response is already committed, cannot send error response");
      return;
    }

    log.debug("Sending error response: {}", message);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(message)));
  }
}
