package com.trusthold.config;

import com.trusthold.auth.filters.AccessTokenFilter;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final AccessTokenFilter accessTokenFilter;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

  @Bean
  public SecurityFilterChain httpSecurityFilterChain(@NotNull @NonNull HttpSecurity http) {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(AbstractHttpConfigurer::disable)
        .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(accessTokenFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(customAuthenticationEntryPoint))
        .oauth2Login(Customizer.withDefaults())
        .build();
  }
}
