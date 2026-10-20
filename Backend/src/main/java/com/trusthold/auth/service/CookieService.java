package com.trusthold.auth.service;

import com.trusthold.config.CookieConfig;
import com.trusthold.config.JwtConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.trusthold.config.CookieConfig.ACCESS_COOKIE;
import static com.trusthold.config.CookieConfig.REFRESH_COOKIE;

@Service
@RequiredArgsConstructor
public class CookieService {

  private final CookieConfig cookieConfig;
  private final JwtConfig jwtConfig;

  public String getAccessTokenCookie(HttpServletRequest request){
    Cookie[] cookies = request.getCookies();
    if (cookies == null) return null;
    for(Cookie cookie : cookies){
      if(cookie.getName().equals(ACCESS_COOKIE)){
        return cookie.getValue();
      }
    }
    return null;
  }

  public String getRefreshTokenCookie(HttpServletRequest request){
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
