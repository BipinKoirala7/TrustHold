package com.trusthold.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.cookie")
@Getter
public class CookieConfig {
  private String REFRESH_COOKIE_PATH;
  private String ACCESS_COOKIE_PATH;
  private Boolean sameSite;
  private Boolean httpOnly;
  private Boolean secure;
  private String domain;
}
