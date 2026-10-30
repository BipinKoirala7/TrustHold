package com.trusthold.auth.filters;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.auth.service.CookieService;
import com.trusthold.auth.service.JwtService;
import com.trusthold.config.AuthConfig;
import com.trusthold.shared.ErrorInfo;
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
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Authenticates each request using token from the cookies and populates the {@link SecurityContextHolder}.
 *
 * <p>Paths in {@code SHOULD_NOT_FILTER} are skipped. On any token problem the
 * filter writes a 401 JSON response and does not continue the chain.
 * Also sets {@code RequestId}, {@code RequestMethod}, {@code RequestPath} (and
 * {@code UserId} after login) in the MDC so all logs of a request can be correlated.
 *
 * @see JwtService
 * @see CookieService
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccessTokenFilter extends OncePerRequestFilter {

  private final ObjectMapper objectMapper;
  private final CookieService cookieService;
  private final JwtService jwtService;
  private final AuthConfig authConfig;

  /**
   * Checks if the request should not be authenticated.
   *
   * <p>
   *   Filters all the requests except listed on {@code SHOULD_NOT_FILTER}.
   * </p>
   * @param request current HTTP request
   * @return if it should not be authenticated
   */
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    return authConfig.getPublicEndpoints().stream().anyMatch(path::startsWith);
  }

  /**
   * Authenticates the request using the access token from the cookie.
   *
   * @param request current HTTP request
   * @param response current HTTP response
   * @param filterChain filter chain
   * @throws ServletException if an error occurs during the filter chain processing
   * @throws IOException if an I/O error occurs during the filter chain processing
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    MDC.put("RequestId", UUID.randomUUID().toString());
    MDC.put("RequestMethod", request.getMethod());
    MDC.put("RequestPath", request.getServletPath());

    try{
      log.debug("Request Start");
      try {
        String accessToken = cookieService.getAccessTokenCookie(request);
        if (accessToken == null) throw new JwtException("Access token cookie not found");

        Claims claims = jwtService.extractAccessClaims(accessToken);
        String roleClaim = claims.get("role", String.class);

        if (roleClaim == null) throw new JwtException("Missing role claim");

        AuthUser authUser = new AuthUser(UUID.fromString(claims.getSubject()), accessToken, Role.valueOf(roleClaim));
        // "ROLE_" prefix is required by Spring Security's hasRole() check
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + authUser.role().toString()));
        var auth = new UsernamePasswordAuthenticationToken(authUser, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(auth);
        MDC.put("UserId", authUser.id().toString());

      } catch (JwtException | IllegalArgumentException e) {
        log.warn("Access Token Filter Error - {}", e.getMessage());
        SecurityContextHolder.clearContext();
        sendErrorResponse(response, "Please log in");
        return;
      }
      filterChain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }

  private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
    if(response.isCommitted()){
      log.debug("Response is already committed, cannot send error response");
      return;
    }

    log.debug("Sending error response: {}", message);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(ErrorInfo.UNAUTHORIZED)));
  }
}
