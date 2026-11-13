package com.trusthold.config;

import com.trusthold.auth.filters.AccessTokenFilter;
import com.trusthold.auth.handlers.CustomAccessDeniedHandler;
import com.trusthold.auth.handlers.CustomAuthenticationEntryPoint;
import com.trusthold.auth.handlers.OAuthFailureHandler;
import com.trusthold.auth.handlers.OAuthSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration for security settings used by the application.
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

  private final AuthConfig authConfig;
  private final AccessTokenFilter accessTokenFilter;
  private final OAuthSuccessHandler oAuthSuccessHandler;
  private final OAuthFailureHandler oAuthFailureHandler;
  private final CustomAccessDeniedHandler customAccessDeniedHandler;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

  @Bean
  public SecurityFilterChain httpSecurityFilterChain(HttpSecurity http) {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(accessTokenFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(authConfig.getPublicEndpoints().toArray(String[]::new)).permitAll()
                .requestMatchers("/tenant/**").hasRole("TENANT")
                .requestMatchers("/landlord/**").hasRole("LANDLORD")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .authenticationEntryPoint(customAuthenticationEntryPoint)
            .accessDeniedHandler(customAccessDeniedHandler))
        .oauth2Login(oauth -> oauth
            .successHandler(oAuthSuccessHandler)
            .failureHandler(oAuthFailureHandler))
        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(){
    CorsConfiguration corsConfiguration = new CorsConfiguration();
    corsConfiguration.setAllowedOrigins(List.of("http://localhost:3000"));
    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    corsConfiguration.addAllowedHeader("*");
    corsConfiguration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfiguration);

    return source;
  }

  @Bean
  public FilterRegistrationBean<AccessTokenFilter> accessTokenFilterFilterRegistrationBean(AccessTokenFilter filter) {
    FilterRegistrationBean<AccessTokenFilter> registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }
}
