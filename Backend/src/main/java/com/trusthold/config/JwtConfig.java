package com.trusthold.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("app.jwt")
@Getter
public class JwtConfig {

  private String applicationSignature;
  private long accessTokenExpiration;
  private long refreshTokenExpiration;
  private String audience;
  private String issuer;
}
