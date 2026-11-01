package com.trusthold.auth.dtos;

import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.shared.dtos.DtoValidationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterUserDtoTest extends DtoValidationTestSupport {

  private static final RegistrationRole ROLE = RegistrationRole.values()[0];
  private static final String VALID_PHONE = "+9779812345678";

  private static final String USERNAME_PATTERN_MESSAGE =
      "Username can contain only lowercase letters, numbers, underscores, and hyphens";
  private static final String PASSWORD_PATTERN_MESSAGE =
      "Password must be at least 8 characters long and contain at least one uppercase letter, "
          + "one lowercase letter, one number, and one special character.";
  private static final String PHONE_PATTERN_MESSAGE =
      "Phone number must be a valid international phone number";

  private RegisterUserDto dto(String firstName, String lastName, String username, String email,
                              String password, String phone, RegistrationRole role) {
    return new RegisterUserDto(firstName, lastName, username, email, password, phone, role);
  }

  private RegisterUserDto valid() {
    return dto("John", "Doe", "john_doe-1", VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, ROLE);
  }

  @Test
  @DisplayName("valid DTO passes")
  void validDto() {
    assertThat(validate(valid())).isEmpty();
  }

  @Test
  @DisplayName("all-null DTO reports every field")
  void allNull() {
    assertThat(violatedProperties(dto(null, null, null, null, null, null, null)))
        .containsExactlyInAnyOrder(
            "firstName", "lastName", "username", "email", "password", "phone", "registrationRole");
  }

  @Nested
  class Names {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void blankFirstName(String value) {
      assertThat(messagesFor(dto(value, "Doe", "johndoe", VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, ROLE), "firstName"))
          .contains("First name is required");
    }

    @Test
    void firstNameLength() {
      String msg = "First name must be between 3 and 50 characters";
      assertThat(messagesFor(dto("Jo", "Doe", "johndoe", VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, ROLE), "firstName")).contains(msg);
      assertThat(messagesFor(dto("a".repeat(51), "Doe", "johndoe", VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, ROLE), "firstName")).contains(msg);
      assertThat(validate(dto("Joe", "Doe", "johndoe", VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, ROLE))).isEmpty();
      assertThat(validate(dto("a".repeat(50), "Doe", "johndoe", VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, ROLE))).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void blankLastName(String value) {
      assertThat(messagesFor(dto("John", value, "johndoe", VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, ROLE), "lastName"))
          .contains("Last name is required");
    }

    @Test
    void lastNameLength() {
      String msg = "Last name must be between 3 and 50 characters";
      assertThat(messagesFor(dto("John", "Do", "johndoe", VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, ROLE), "lastName")).contains(msg);
      assertThat(messagesFor(dto("John", "a".repeat(51), "johndoe", VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, ROLE), "lastName")).contains(msg);
      assertThat(validate(dto("John", "Doe", "johndoe", VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, ROLE))).isEmpty();
      assertThat(validate(dto("John", "a".repeat(50), "johndoe", VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, ROLE))).isEmpty();
    }
  }

  @Nested
  class Username {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void blankUsername(String value) {
      assertThat(messagesFor(dto("John", "Doe", value, VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, ROLE), "username"))
          .contains("Username is required");
    }

    @Test
    void usernameLength() {
      String msg = "Username must be between 3 and 50 characters";
      assertThat(messagesFor(dto("John", "Doe", "ab", VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, ROLE), "username")).contains(msg);
      assertThat(messagesFor(dto("John", "Doe", "a".repeat(51), VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, ROLE), "username")).contains(msg);
      assertThat(validate(dto("John", "Doe", "abc", VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, ROLE))).isEmpty();
      assertThat(validate(dto("John", "Doe", "a".repeat(50), VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, ROLE))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"JohnDoe", "john doe", "john.doe", "john@doe", "jöhn"})
    @DisplayName("characters outside [a-z0-9_-] fail the pattern")
    void invalidCharacters(String username) {
      assertThat(messagesFor(dto("John", "Doe", username, VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, ROLE), "username"))
          .containsExactly(USERNAME_PATTERN_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"johndoe", "john_doe", "john-doe", "user123", "___", "---"})
    void validCharacters(String username) {
      assertThat(validate(dto("John", "Doe", username, VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, ROLE))).isEmpty();
    }
  }

  @Nested
  class Email {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void blankEmail(String value) {
      assertThat(messagesFor(dto("John", "Doe", "johndoe", value, VALID_PASSWORD, VALID_PHONE, ROLE), "email"))
          .contains("Email is required");
    }

    @ParameterizedTest
    @ValueSource(strings = {"plainaddress", "user@@example.com", "@example.com"})
    void malformedEmail(String value) {
      assertThat(messagesFor(dto("John", "Doe", "johndoe", value, VALID_PASSWORD, VALID_PHONE, ROLE), "email"))
          .containsExactly("Email must be a valid email address");
    }
  }

  @Nested
  class Password {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void blankPassword(String value) {
      assertThat(messagesFor(dto("John", "Doe", "johndoe", VALID_EMAIL, value, VALID_PHONE, ROLE), "password"))
          .contains("Password is required");
    }

    @Test
    void tooShort() {
      assertThat(messagesFor(dto("John", "Doe", "johndoe", VALID_EMAIL, "Pa1@", VALID_PHONE, ROLE), "password"))
          .contains("Password must be between 8 and 100 characters");
    }

    @Test
    void tooLong() {
      String password = "Aa1@" + "a".repeat(97); // 101 chars
      assertThat(messagesFor(dto("John", "Doe", "johndoe", VALID_EMAIL, password, VALID_PHONE, ROLE), "password"))
          .contains("Password must be between 8 and 100 characters");
    }

    @Test
    void boundaryLengths() {
      assertThat(validate(dto("John", "Doe", "johndoe", VALID_EMAIL, "Passw0r@", VALID_PHONE, ROLE))).isEmpty();
      assertThat(validate(dto("John", "Doe", "johndoe", VALID_EMAIL, "Aa1@" + "a".repeat(96), VALID_PHONE, ROLE))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"password1@", "PASSWORD1@", "Password@@", "Password12", "Password1#", "Pass word1@"})
    void patternViolations(String password) {
      assertThat(messagesFor(dto("John", "Doe", "johndoe", VALID_EMAIL, password, VALID_PHONE, ROLE), "password"))
          .contains(PASSWORD_PATTERN_MESSAGE);
    }
  }

  @Nested
  class Phone {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void blankPhone(String value) {
      assertThat(messagesFor(dto("John", "Doe", "johndoe", VALID_EMAIL, VALID_PASSWORD, value, ROLE), "phone"))
          .contains("Phone number is required");
    }

    @ParameterizedTest
    @ValueSource(strings = {"+9779812345678", "+14155552671", "+12", "+123456789012345"})
    @DisplayName("E.164-style numbers (+, then 2-15 digits, first digit non-zero) pass")
    void validPhones(String phone) {
      assertThat(validate(dto("John", "Doe", "johndoe", VALID_EMAIL, VALID_PASSWORD, phone, ROLE))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "9779812345678",    // missing leading +
        "+0123456789",      // leading zero after +
        "+1",               // too few digits
        "+977 9812345678",  // whitespace
        "+977-981-2345678", // hyphens
        "+97798abc45678"    // letters
    })
    @DisplayName("numbers not matching the international format fail the pattern")
    void invalidPhones(String phone) {
      assertThat(messagesFor(dto("John", "Doe", "johndoe", VALID_EMAIL, VALID_PASSWORD, phone, ROLE), "phone"))
          .contains(PHONE_PATTERN_MESSAGE);
    }

    @Test
    @DisplayName("phone longer than 16 characters fails size and pattern")
    void tooLong() {
      String phone = "+1234567890123456"; // 17 chars
      assertThat(messagesFor(dto("John", "Doe", "johndoe", VALID_EMAIL, VALID_PASSWORD, phone, ROLE), "phone"))
          .containsExactlyInAnyOrder(
              "Phone number must be at most 16 characters long",
              PHONE_PATTERN_MESSAGE);
    }
  }

  @Nested
  class Role {

    @ParameterizedTest
    @EnumSource(RegistrationRole.class)
    void anyRoleIsValid(RegistrationRole role) {
      assertThat(validate(dto("John", "Doe", "johndoe", VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, role))).isEmpty();
    }

    @Test
    void nullRole() {
      assertThat(messagesFor(dto("John", "Doe", "johndoe", VALID_EMAIL, VALID_PASSWORD, VALID_PHONE, null), "registrationRole"))
          .containsExactly("User role is required");
    }
  }
}