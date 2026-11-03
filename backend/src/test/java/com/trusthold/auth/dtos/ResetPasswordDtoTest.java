package com.trusthold.auth.dtos;

import com.trusthold.shared.dtos.DtoValidationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ResetPasswordDtoTest extends DtoValidationTestSupport {

  private static final String PASSWORD_PATTERN_MESSAGE =
      "Password must be at least 8 characters long and contain at least one uppercase letter, "
          + "one lowercase letter, one number, and one special character.";

  @Test
  @DisplayName("valid token and password pass")
  void validDto() {
    assertThat(validate(new ResetPasswordDto("some-raw-token", VALID_PASSWORD))).isEmpty();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  @DisplayName("blank token fails with 'Invalid token'")
  void blankToken(String token) {
    assertThat(messagesFor(new ResetPasswordDto(token, VALID_PASSWORD), "rawToken"))
        .containsExactly("Invalid token");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  @DisplayName("blank password fails with 'Password is required'")
  void blankPassword(String password) {
    assertThat(messagesFor(new ResetPasswordDto("token", password), "newPassword"))
        .contains("Password is required");
  }

  @Test
  @DisplayName("password shorter than 8 characters fails the size constraint")
  void tooShort() {
    assertThat(messagesFor(new ResetPasswordDto("token", "Pa1@"), "newPassword"))
        .contains("Password must be between 8 and 100 characters");
  }

  @Test
  @DisplayName("password longer than 100 characters fails the size constraint")
  void tooLong() {
    String password = "Aa1@" + "a".repeat(97); // 101 chars
    assertThat(messagesFor(new ResetPasswordDto("token", password), "newPassword"))
        .contains("Password must be between 8 and 100 characters");
  }

  @Test
  @DisplayName("password of exactly 8 and exactly 100 characters passes")
  void boundaryLengths() {
    assertThat(validate(new ResetPasswordDto("token", "Passw0r@"))).isEmpty();
    assertThat(validate(new ResetPasswordDto("token", "Aa1@" + "a".repeat(96)))).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {"password1@", "PASSWORD1@", "Password@@", "Password12", "Password1#", "Pass word1@"})
  @DisplayName("password missing a required character class fails the pattern")
  void patternViolations(String password) {
    assertThat(messagesFor(new ResetPasswordDto("token", password), "newPassword"))
        .contains(PASSWORD_PATTERN_MESSAGE);
  }

  @Test
  @DisplayName("all-null DTO reports both fields")
  void allNull() {
    assertThat(violatedProperties(new ResetPasswordDto(null, null)))
        .containsExactlyInAnyOrder("rawToken", "newPassword");
  }
}