package com.trusthold.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration for authentication public endpoints.
 */
@Component
@ConfigurationProperties("app.auth")
@Getter
@Setter
public class AuthConfig {

  // Public endpoints where there is no need to authenticate.
  // Or if the request is an OAuth2 request, health check or a Swagger request,
  // Refresh token path checks for refresh token, not access token.
	private List<String> publicEndpoints;
}
