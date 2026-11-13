package com.trusthold.auth.filters;

import com.trusthold.auth.model.AuthUser;
import com.trusthold.auth.service.CookieService;
import com.trusthold.auth.service.JwtService;
import com.trusthold.config.AuthConfig;
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
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

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

  private final CookieService cookieService;
  private final JwtService jwtService;
  private final AuthConfig authConfig;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

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
    boolean shouldNotFilter = authConfig.getPublicEndpoints().stream().anyMatch(pattern -> pathMatcher.match(pattern, path));

    log.debug("Access token filter {} for {}", shouldNotFilter ? "skipped" : "applied", path);
    return shouldNotFilter;
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
      try {
        String accessToken = cookieService.getAccessTokenCookie(request);
        if (accessToken == null || accessToken.isBlank()) throw new JwtException("Access token cookie not found");

        log.debug("Token Found & Extracting claims");

        Claims claims = jwtService.extractAccessClaims(accessToken);
        UUID userId;
        Role role;
        try {
          userId = UUID.fromString(claims.getSubject());
          role = Role.valueOf(claims.get("role", String.class));
        } catch (IllegalArgumentException e) {
          throw new JwtException("Signed token has invalid subject or role claim", e);
        }

        AuthUser authUser = new AuthUser(userId, accessToken, role);
        // "ROLE_" prefix is required by Spring Security's hasRole() check
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + authUser.role().toString()));
        var auth = new UsernamePasswordAuthenticationToken(authUser, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(auth);

        log.debug("Authentication set");
        MDC.put("UserId", authUser.id().toString());

      } catch (JwtException | IllegalArgumentException e) {
        log.warn("Exception : {} ", e.getMessage());
        SecurityContextHolder.clearContext();
      }
      filterChain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }
}
