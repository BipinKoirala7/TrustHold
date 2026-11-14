package com.trusthold.auth.dtos;

import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.shared.dtos.DtoValidationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterOAuthUserDtoTest extends DtoValidationTestSupport {

  private static final RegistrationRole ROLE = RegistrationRole.values()[0];

  private RegisterOAuthUserDto dto(String providerId, String firstName, String lastName,
                                   String username, String email, RegistrationRole role) {
    return new RegisterOAuthUserDto(providerId, firstName, lastName, username, email, role);
  }

  private RegisterOAuthUserDto valid() {
    return dto("google-123", "John", "Doe", "johndoe", VALID_EMAIL, ROLE);
  }

  @Test
  @DisplayName("valid DTO passes")
  void validDto() {
    assertThat(validate(valid())).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(RegistrationRole.class)
  @DisplayName("every RegistrationRole value is accepted")
  void anyRole(RegistrationRole role) {
    assertThat(validate(dto("google-123", "John", "Doe", "johndoe", VALID_EMAIL, role))).isEmpty();
  }

  @Test
  @DisplayName("null role fails with 'User role is required'")
  void nullRole() {
    assertThat(messagesFor(dto("google-123", "John", "Doe", "johndoe", VALID_EMAIL, null), "registrationRole"))
        .containsExactly("User role is required");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  "})
  @DisplayName("blank providerId fails with 'Provider ID is required'")
  void blankProviderId(String providerId) {
    assertThat(messagesFor(dto(providerId, "John", "Doe", "johndoe", VALID_EMAIL, ROLE), "providerId"))
        .containsExactly("Provider ID is required");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  "})
  @DisplayName("blank first name fails with 'First name is required'")
  void blankFirstName(String firstName) {
    assertThat(messagesFor(dto("g", firstName, "Doe", "johndoe", VALID_EMAIL, ROLE), "firstName"))
        .contains("First name is required");
  }

  @Test
  @DisplayName("first name must be 3-50 characters")
  void firstNameLength() {
    String msg = "First name must be between 3 and 50 characters";
    assertThat(messagesFor(dto("g", "Jo", "Doe", "johndoe", VALID_EMAIL, ROLE), "firstName")).contains(msg);
    assertThat(messagesFor(dto("g", "a".repeat(51), "Doe", "johndoe", VALID_EMAIL, ROLE), "firstName")).contains(msg);
    assertThat(validate(dto("g", "Joe", "Doe", "johndoe", VALID_EMAIL, ROLE))).isEmpty();
    assertThat(validate(dto("g", "a".repeat(50), "Doe", "johndoe", VALID_EMAIL, ROLE))).isEmpty();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  "})
  @DisplayName("blank last name fails with 'Last name is required'")
  void blankLastName(String lastName) {
    assertThat(messagesFor(dto("g", "John", lastName, "johndoe", VALID_EMAIL, ROLE), "lastName"))
        .contains("Last name is required");
  }

  @Test
  @DisplayName("last name must be 3-50 characters")
  void lastNameLength() {
    String msg = "Last name must be between 3 and 50 characters";
    assertThat(messagesFor(dto("g", "John", "Do", "johndoe", VALID_EMAIL, ROLE), "lastName")).contains(msg);
    assertThat(messagesFor(dto("g", "John", "a".repeat(51), "johndoe", VALID_EMAIL, ROLE), "lastName")).contains(msg);
    assertThat(validate(dto("g", "John", "Doe", "johndoe", VALID_EMAIL, ROLE))).isEmpty();
    assertThat(validate(dto("g", "John", "a".repeat(50), "johndoe", VALID_EMAIL, ROLE))).isEmpty();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  "})
  @DisplayName("blank username fails with 'Username is required'")
  void blankUsername(String username) {
    assertThat(messagesFor(dto("g", "John", "Doe", username, VALID_EMAIL, ROLE), "username"))
        .contains("Username is required");
  }

  @Test
  @DisplayName("username must be 3-50 characters")
  void usernameLength() {
    String msg = "Username must be between 3 and 50 characters";
    assertThat(messagesFor(dto("g", "John", "Doe", "ab", VALID_EMAIL, ROLE), "username")).contains(msg);
    assertThat(messagesFor(dto("g", "John", "Doe", "a".repeat(51), VALID_EMAIL, ROLE), "username")).contains(msg);
  }

  @Test
  @DisplayName("username has no character-set restriction (unlike RegisterUserDto)")
  void usernameAllowsUppercaseAndSymbols() {
    assertThat(validate(dto("g", "John", "Doe", "johndoe_99", VALID_EMAIL, ROLE))).isEmpty();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  "})
  @DisplayName("blank email fails with 'Email is required'")
  void blankEmail(String email) {
    assertThat(messagesFor(dto("g", "John", "Doe", "johndoe", email, ROLE), "email"))
        .contains("Email is required");
  }

  @Test
  @DisplayName("malformed email fails with the format message")
  void malformedEmail() {
    assertThat(messagesFor(dto("g", "John", "Doe", "johndoe", "not-an-email", ROLE), "email"))
        .containsExactly("Email must be a valid email address");
  }

  @Test
  @DisplayName("all-null DTO reports every field")
  void allNull() {
    assertThat(violatedProperties(dto(null, null, null, null, null, null)))
        .containsExactlyInAnyOrder(
            "providerId", "firstName", "lastName", "username", "email", "registrationRole");
  }
}