package com.trusthold.auth.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class OAuthFailureHandlerTests {

  private OAuthFailureHandler handler;

  @BeforeEach
  void setUp() {
    handler = new OAuthFailureHandler();
    ReflectionTestUtils.setField(handler, "frontendUrl", "http://localhost:3000");
  }

  @Test
  void redirectsToFrontendLoginWithErrorFlag() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    handler.onAuthenticationFailure(request, response, new BadCredentialsException("bad"));

    assertThat(response.getRedirectedUrl())
        .isEqualTo("http://localhost:3000/login?error=oauth");
  }
}
