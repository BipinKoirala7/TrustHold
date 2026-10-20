package com.trusthold.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.cookie")
@Getter
@Setter
public class CookieConfig {
  public static final String ACCESS_COOKIE = "accessToken";
  public static final String REFRESH_COOKIE = "refreshToken";

  private String refreshCookiePath;
  private String accessCookiePath;
  private String sameSite;
  private Boolean httpOnly;
  private Boolean secure;
}
