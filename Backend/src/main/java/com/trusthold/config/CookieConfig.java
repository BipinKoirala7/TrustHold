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
  private String refreshCookiePath;
  private String accessCookiePath;
  private String sameSite;
  private Boolean httpOnly;
  private Boolean secure;
}
