package com.trusthold.auth.dtos;

import com.trusthold.shared.dtos.DtoValidationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ForgotPasswordDtoTest extends DtoValidationTestSupport {

  @Test
  @DisplayName("valid email passes")
  void validEmail() {
    assertThat(validate(new ForgotPasswordDto(VALID_EMAIL))).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("invalidEmails")
  @DisplayName("malformed email fails with the configured message")
  void invalidEmail(String email, String validation) {
    assertThat(messagesFor(new ForgotPasswordDto(email), "email")).containsExactly(validation);
  }

  private static Stream<Arguments> invalidEmails() {
    String validation1 = "Email is required";
    String validation2 = "Email must be a valid email address";
    return Stream.of(
        Arguments.of(null, validation1),
        Arguments.of("", validation1),
        Arguments.of("plainaddress", validation2),
        Arguments.of("@no-local.com", validation2),
        Arguments.of("user@@example.com", validation2),
        Arguments.of("user name@example.com", validation2)
    );
  }
}
