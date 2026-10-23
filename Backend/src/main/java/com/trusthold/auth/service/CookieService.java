package com.trusthold.auth.service;

import com.trusthold.config.CookieConfig;
import com.trusthold.config.JwtConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.trusthold.config.CookieConfig.ACCESS_COOKIE;
import static com.trusthold.config.CookieConfig.REFRESH_COOKIE;

/**
 * Create, delete & update cookies.
 *
 * <p>
 *   Two types of cookies exits.
 *   <ul>
 *     <li>Access Token Cookie: short-lived, contains information for authenticating requests</li>
 *     <li>Refresh Token Cookie: long-lived, contains trivial information</li>
 *   </ul>
 * </p>
 *
 * <p>While an access token is sent by the browser on every request, a refresh token is sent only
 *  on a path: /auth/token/refresh
 * </p>
 *
 *  <p>It has its own config (see {@link CookieConfig}), but it also takes some configuration form jwt
 *    (see {@link JwtConfig}
 *  </p>
 *
 * @see CookieConfig
 * @see JwtConfig
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CookieService {

  private final CookieConfig cookieConfig;
  private final JwtConfig jwtConfig;

  /**
   * Gets the Access token from the request cookies.
   * Returns null if an access token cookie is not found.
   * @param request current HTTP request
   * @return value of the filtered cookie
   */
  public String getAccessTokenCookie(HttpServletRequest request){
    log.debug("Fetching access token cookie");

    Cookie[] cookies = request.getCookies();
    if (cookies == null) return null;
    for(Cookie cookie : cookies){
      if(cookie.getName().equals(ACCESS_COOKIE)){
        return cookie.getValue();
      }
    }
    return null;
  }

  /**
   * Gets the Refresh token from the request cookies.
   * Returns null if a refresh token cookie is not found.
   * @param request current HTTP request
   * @return value of the filtered cookie
   */
  public String getRefreshTokenCookie(HttpServletRequest request){
    log.debug("Fetching refresh token cookie");

    Cookie[] cookies = request.getCookies();
    if (cookies == null) return null;
    for(Cookie cookie : cookies){
      if(cookie.getName().equals(REFRESH_COOKIE)){
        return cookie.getValue();
      }
    }
    return null;
  }

  public Cookie createRefreshTokenCookie(@NotNull String refreshToken){
    return createCookie(REFRESH_COOKIE, refreshToken, cookieConfig.getRefreshCookiePath(), (int) jwtConfig.getRefreshTokenExpiration(), cookieConfig.getSameSite());
  }

  public Cookie createAccessTokenCookie(@NotNull String accessToken){
    return createCookie(ACCESS_COOKIE, accessToken, cookieConfig.getAccessCookiePath(), (int) jwtConfig.getAccessTokenExpiration(), cookieConfig.getSameSite());
  }

  public void deleteRefreshTokenCookie(HttpServletResponse response){
    response.addCookie(createCookie(REFRESH_COOKIE, null, cookieConfig.getRefreshCookiePath(), 0, cookieConfig.getSameSite()));
  }

  public void deleteAccessTokenCookie(HttpServletResponse response){
    response.addCookie(createCookie(ACCESS_COOKIE, null, cookieConfig.getAccessCookiePath(), 0, cookieConfig.getSameSite()));
  }

  private Cookie createCookie(String name, String value, String path, int maxAge, String sameSite) {
    Cookie cookie = new Cookie(name, value);
    cookie.setPath(path);
    cookie.setHttpOnly(true);
    cookie.setSecure(cookieConfig.getSecure());
    cookie.setMaxAge(maxAge);
    cookie.setAttribute("SameSite", sameSite);
    return cookie;
  }

}
