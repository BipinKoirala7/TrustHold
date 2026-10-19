package com.trusthold.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("app.jwt")
@Getter
public class JwtConfig {
  public static final String REFRESH_TYPE = "refreshToken";
  public static final String ACCESS_TYPE = "accessToken";
  public static final String TOKEN_TYPE = "TOKEN_TYPE";

  private String applicationSignature;
  private long accessTokenExpiration;
  private long refreshTokenExpiration;
  private String audience;
  private String issuer;
}
