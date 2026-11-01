package com.trusthold.auth.dtos;

import com.trusthold.shared.dtos.DtoValidationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

public class ForgotPasswordDtoTest extends DtoValidationTestSupport {

  @Test
  @DisplayName("valid email passes")
  void validEmail() {
    assertThat(validate(new ForgotPasswordDto(VALID_EMAIL))).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {"plainaddress", "@no-local.com", "user@@example.com", "user name@example.com"})
  @DisplayName("malformed email fails with the configured message")
  void invalidEmail(String email) {
    assertThat(messagesFor(new ForgotPasswordDto(email), "email"))
        .containsExactly("Invalid email address");
  }

  @Test
  @DisplayName("null email is accepted because @Email does not imply @NotNull")
  void nullEmailIsAccepted() {
    assertThat(validate(new ForgotPasswordDto(null))).isEmpty();
  }

  @Test
  @DisplayName("empty email is accepted because @Email treats empty as valid")
  void emptyEmailIsAccepted() {
    assertThat(validate(new ForgotPasswordDto(""))).isEmpty();
  }
}
