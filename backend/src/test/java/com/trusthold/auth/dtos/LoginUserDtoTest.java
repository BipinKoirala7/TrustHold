package com.trusthold.auth.dtos;

import com.trusthold.shared.dtos.DtoValidationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class LoginUserDtoTest extends DtoValidationTestSupport {

  private static final String PASSWORD_PATTERN_MESSAGE =
      "Password must be at least 8 characters long and contain at least one uppercase letter, "
          + "one lowercase letter, one number, and one special character.";

  @Test
  @DisplayName("valid email and password pass")
  void validDto() {
    assertThat(validate(new LoginUserDto(VALID_EMAIL, VALID_PASSWORD))).isEmpty();
  }

  @Nested
  class Email {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("blank email fails with 'Email is required'")
    void blankEmail(String email) {
      assertThat(messagesFor(new LoginUserDto(email, VALID_PASSWORD), "email"))
          .contains("Email is required");
    }

    @ParameterizedTest
    @ValueSource(strings = {"plainaddress", "user@@example.com", "@example.com"})
    @DisplayName("malformed email fails with the format message")
    void malformedEmail(String email) {
      assertThat(messagesFor(new LoginUserDto(email, VALID_PASSWORD), "email"))
          .containsExactly("Email must be a valid email address");
    }
  }

  @Nested
  class Password {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("blank password fails with 'Password is required'")
    void blankPassword(String password) {
      assertThat(messagesFor(new LoginUserDto(VALID_EMAIL, password), "password"))
          .contains("Password is required");
    }

    @Test
    @DisplayName("password shorter than 8 characters fails the size constraint")
    void tooShort() {
      assertThat(messagesFor(new LoginUserDto(VALID_EMAIL, "Pa1@"), "password"))
          .contains("Password must be between 8 and 100 characters");
    }

    @Test
    @DisplayName("password longer than 100 characters fails the size constraint")
    void tooLong() {
      String password = "Aa1@" + "a".repeat(97); // 101 chars
      assertThat(messagesFor(new LoginUserDto(VALID_EMAIL, password), "password"))
          .contains("Password must be between 8 and 100 characters");
    }

    @Test
    @DisplayName("password of exactly 8 and exactly 100 characters passes")
    void boundaryLengths() {
      String min = "Passw0r@";                // 8 chars
      String max = "Aa1@" + "a".repeat(96);   // 100 chars
      assertThat(validate(new LoginUserDto(VALID_EMAIL, min))).isEmpty();
      assertThat(validate(new LoginUserDto(VALID_EMAIL, max))).isEmpty();
    }
  }

  @Test
  @DisplayName("all-null DTO reports both fields")
  void allNull() {
    assertThat(violatedProperties(new LoginUserDto(null, null)))
        .containsExactlyInAnyOrder("email", "password");
  }
}