package com.trusthold.auth.model;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

@Getter
public class JwtAuthenticationToken extends AbstractAuthenticationToken {
  private final UUID userId;
  private final String accessToken;

  public JwtAuthenticationToken(UUID userId, String accessToken, @Nullable Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.userId = userId;
    this.accessToken = accessToken;
    this.setAuthenticated(true);
  }

  @Override
  public @Nullable Object getCredentials() {
    return null;
  }

  @Override
  public @Nullable Object getPrincipal() {
    return null;
  }
}
