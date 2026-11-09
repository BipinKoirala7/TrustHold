package com.trusthold.shared.controller;

import com.trusthold.auth.filters.AccessTokenFilter;
import com.trusthold.auth.handlers.CustomAuthenticationEntryPoint;
import com.trusthold.auth.handlers.OAuthFailureHandler;
import com.trusthold.auth.handlers.OAuthSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;


public class ControllerTestSupport {

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected MockMvc mockMvc;

  @MockitoBean
  protected AccessTokenFilter accessTokenFilter;

  @MockitoBean
  protected CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

  @MockitoBean
  private OAuthSuccessHandler oAuthSuccessHandler;

  @MockitoBean
  private OAuthFailureHandler oAuthFailureHandler;
}
