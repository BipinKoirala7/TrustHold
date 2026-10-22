package com.trusthold.auth.filters;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.auth.service.CookieService;
import com.trusthold.auth.service.JwtService;
import com.trusthold.shared.dtos.ApiResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenFilter extends OncePerRequestFilter {

  private final List<String> SHOULD_FILTER = List.of("/auth/token/refresh");

  private final ObjectMapper objectMapper;
  private final CookieService cookieService;
  private final JwtService jwtService;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    return SHOULD_FILTER.stream().noneMatch(path::startsWith);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    log.warn("Request path: {}", request.getServletPath());

    try{
      String refreshToken = cookieService.getRefreshTokenCookie(request);

      if(refreshToken == null){
        throw new JwtException("Refresh token is null");
      }

      Claims claims = jwtService.extractClaims(refreshToken);

      AuthUser authUser = new AuthUser(UUID.fromString(claims.getSubject()), refreshToken, null);
      var auth = new UsernamePasswordAuthenticationToken(authUser, null, List.of());
      SecurityContextHolder.getContext().setAuthentication(auth);

      log.warn("Refresh token is valid");
    } catch (JwtException e){
      log.error("Error validating access token", e);
      SecurityContextHolder.clearContext();
      sendErrorResponse(response, "Please log in");
      return;
    }
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
