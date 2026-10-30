package com.trusthold.auth.handlers;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class OAuthFailureHandlerTests {

  @InjectMocks
  private OAuthFailureHandler handler;

  @Test
  void redirectsToFrontendLoginWithErrorFlag() throws Exception {
    ReflectionTestUtils.setField(handler, "frontendUrl", "http://localhost:3000");

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    handler.onAuthenticationFailure(
        request, response, new BadCredentialsException("bad"));

    assertThat(response.getRedirectedUrl())
        .isEqualTo("http://localhost:3000/login?error=oauth");
  }
}
