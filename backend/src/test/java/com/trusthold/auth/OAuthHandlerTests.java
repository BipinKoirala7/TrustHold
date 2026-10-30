package com.trusthold.auth;

import com.trusthold.auth.handlers.OAuthFailureHandler;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class OAuthFailureHandlerTest {

  @Test
  void redirectsToFrontendLoginWithErrorFlag() throws Exception {
    OAuthFailureHandler handler = new OAuthFailureHandler();
    ReflectionTestUtils.setField(handler, "frontendUrl", "http://localhost:3000");

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    handler.onAuthenticationFailure(
        request, response, new BadCredentialsException("bad"));

    assertThat(response.getRedirectedUrl())
        .isEqualTo("http://localhost:3000/login?error=oauth");
  }
}
