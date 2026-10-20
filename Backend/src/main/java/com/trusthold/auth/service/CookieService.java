package com.trusthold.auth.service;

import com.trusthold.config.CookieConfig;
import com.trusthold.config.JwtConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.trusthold.config.JwtConfig.ACCESS_TYPE;
import static com.trusthold.config.JwtConfig.REFRESH_TYPE;

@Service
@RequiredArgsConstructor
public class CookieService {

  private final CookieConfig cookieConfig;
  private final JwtConfig jwtConfig;

  public String getAccessTokenCookie(HttpServletRequest request){
    Cookie[] cookies = request.getCookies();
    for(Cookie cookie : cookies){
      if(cookie.getName().equals(ACCESS_TYPE)){
        return cookie.getValue();
      }
    }
    return null;
  }

  public String getRefreshTokenCookie(HttpServletRequest request){
    Cookie[] cookies = request.getCookies();
    for(Cookie cookie : cookies){
      if(cookie.getName().equals(REFRESH_TYPE)){
        return cookie.getValue();
      }
    }
    return null;
  }

  public Cookie createRefreshTokenCookie(@NotNull String refreshToken){
    Cookie cookie = new Cookie(REFRESH_TYPE, refreshToken);
    cookie.setPath(cookieConfig.getREFRESH_COOKIE_PATH());
    cookie.setHttpOnly(cookieConfig.getHttpOnly());
    cookie.setSecure(cookieConfig.getSecure());
    cookie.setDomain(cookieConfig.getDomain());
    cookie.setMaxAge((int) jwtConfig.getRefreshTokenExpiration());

    return cookie;
  }

  public Cookie createAccessTokenCookie(@NotNull String accessToken){
    Cookie cookie = new Cookie(ACCESS_TYPE, accessToken);
    cookie.setPath(cookieConfig.getACCESS_COOKIE_PATH());
    cookie.setHttpOnly(cookieConfig.getHttpOnly());
    cookie.setSecure(cookieConfig.getSecure());
    cookie.setDomain(cookieConfig.getDomain());
    cookie.setMaxAge((int) jwtConfig.getAccessTokenExpiration());

    return cookie;
  }

  public void deleteRefreshTokenCookie(HttpServletResponse response){
    Cookie cookie = new Cookie(REFRESH_TYPE, null);
    cookie.setPath(cookieConfig.getREFRESH_COOKIE_PATH());
    cookie.setHttpOnly(cookieConfig.getHttpOnly());
    cookie.setSecure(cookieConfig.getSecure());
    cookie.setDomain(cookieConfig.getDomain());
    cookie.setMaxAge(0);

    response.addCookie(cookie);
  }

  public void deleteAccessTokenCookie(HttpServletResponse response){
    Cookie cookie = new Cookie(ACCESS_TYPE, null);
    cookie.setPath(cookieConfig.getACCESS_COOKIE_PATH());
    cookie.setHttpOnly(cookieConfig.getHttpOnly());
    cookie.setSecure(cookieConfig.getSecure());
    cookie.setDomain(cookieConfig.getDomain());
    cookie.setMaxAge(0);

    response.addCookie(cookie);
  }

}
