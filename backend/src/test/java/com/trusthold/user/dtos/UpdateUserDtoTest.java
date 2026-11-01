package com.trusthold.user.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UpdateUserDtoTest {

  private static ValidatorFactory validatorFactory;
  private static Validator validator;

  @BeforeAll
  static void setUp() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @AfterAll
  static void tearDown() {
    validatorFactory.close();
  }

  @Test
  void shouldPassValidation_whenDataIsValid() {
    UpdateUserDto dto = new UpdateUserDto(
        "Bipin",
        "Koirala",
        "bipin_koirala",
        "+9779812345678"
    );

    Set<ConstraintViolation<UpdateUserDto>> violations =
        validator.validate(dto);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldFail_whenFirstNameIsNull() {
    UpdateUserDto dto = new UpdateUserDto(
        null,
        "Koirala",
        "bipin_koirala",
        "+9779812345678"
    );

    assertViolation(dto, "firstName", "First name is required");
  }

  @Test
  void shouldFail_whenLastNameIsNull() {
    UpdateUserDto dto = new UpdateUserDto(
        "Bipin",
        null,
        "bipin_koirala",
        "+9779812345678"
    );

    assertViolation(dto, "lastName", "Last name is required");
  }

  @Test
  void shouldFail_whenUsernameIsNull() {
    UpdateUserDto dto = new UpdateUserDto(
        "Bipin",
        "Koirala",
        null,
        "+9779812345678"
    );

    assertViolation(dto, "username", "Username is required");
  }

  @Test
  void shouldFail_whenPhoneIsNullOrBlank() {
    UpdateUserDto nullPhone = new UpdateUserDto(
        "Bipin", "Koirala", "bipin_koirala", null
    );

    UpdateUserDto blankPhone = new UpdateUserDto(
        "Bipin", "Koirala", "bipin_koirala", " "
    );

    assertViolation(
        nullPhone, "phone", "Phone number is required"
    );

    assertViolation(
        blankPhone, "phone", "Phone number is required"
    );
  }

  @Test
  void shouldFail_whenFirstNameIsTooShortOrTooLong() {
    UpdateUserDto tooShort = new UpdateUserDto(
        "Ab", "Koirala", "bipin_koirala", "+9779812345678"
    );

    UpdateUserDto tooLong = new UpdateUserDto(
        "A".repeat(51), "Koirala",
        "bipin_koirala", "+9779812345678"
    );

    assertViolation(
        tooShort,
        "firstName",
        "First name must be between 3 and 50 characters"
    );

    assertViolation(
        tooLong,
        "firstName",
        "First name must be between 3 and 50 characters"
    );
  }

  @Test
  void shouldFail_whenLastNameIsTooShortOrTooLong() {
    UpdateUserDto tooShort = new UpdateUserDto(
        "Bipin", "Ab", "bipin_koirala", "+9779812345678"
    );

    UpdateUserDto tooLong = new UpdateUserDto(
        "Bipin", "A".repeat(51),
        "bipin_koirala", "+9779812345678"
    );

    assertViolation(
        tooShort,
        "lastName",
        "Last name must be between 3 and 50 characters"
    );

    assertViolation(
        tooLong,
        "lastName",
        "Last name must be between 3 and 50 characters"
    );
  }

  @ParameterizedTest
  @MethodSource("invalidUsernames")
  void shouldFail_whenUsernameIsTooShortOrTooLong(String username) {
    UpdateUserDto dto = new UpdateUserDto(
        "Bipin",
        "Koirala",
        username,
        "+9779812345678"
    );

    assertViolation(
        dto,
        "username",
        "Username must be between 3 and 50 characters"
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "Bipin",             // Uppercase letters
      "bipin koirala",     // Spaces
      "bipin@koirala",     // Special character @
      "bipin.koirala",     // Period
      "bipin!koirala",     // Special character !
      "bipin#123",         // Special character #
      "bipin+koirala",     // Special character +
      "bipin/koirala",     // Forward slash
      "bipin$koirala"      // Special character $
  })
  void shouldFail_whenUsernameDoesNotMatchPattern(String username) {
    UpdateUserDto dto = new UpdateUserDto(
        "Bipin",
        "Koirala",
        username,
        "+9779812345678"
    );

    assertViolation(
        dto,
        "username",
        "Username can only contain lowercase letters, numbers, underscores, and hyphens"
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "bipin",
      "bipin_koirala",
      "bipin-koirala",
      "bipin123",
      "bipin_123",
      "bipin-123",
      "123",
      "abc",
  })
  void shouldPass_whenUsernameMatchesPatternAndLength(String username) {
    UpdateUserDto dto = new UpdateUserDto(
        "Bipin",
        "Koirala",
        username,
        "+9779812345678"
    );

    var violations = validator.validate(dto);

    assertTrue(
        violations.stream().noneMatch(
            violation -> violation.getPropertyPath()
                .toString()
                .equals("username")
        ),
        () -> "Expected valid username but got violations: " + violations
    );
  }

  @Test
  void shouldFail_whenPhoneNumberIsTooLong() {
    UpdateUserDto dto = new UpdateUserDto(
        "Bipin",
        "Koirala",
        "bipin_koirala",
        "+" + "1".repeat(16)
    );

    assertViolation(
        dto,
        "phone",
        "Phone number must be at most 16 characters long"
    );
  }

  @Test
  void shouldFail_whenPhoneNumberFormatIsInvalid() {
    String[] invalidPhones = {
        "9779812345678",   // Missing +
        "+09812345678",    // First digit after + is zero
        "+977-9812345678", // Contains hyphen
        "+977 9812345678", // Contains whitespace
        "+abc123456",      // Contains letters
        "+1",              // Too few digits
        "+1234567890123456" // Too many digits
    };

    for (String phone : invalidPhones) {
      UpdateUserDto dto = new UpdateUserDto(
          "Bipin", "Koirala", "bipin_koirala", phone
      );

      assertTrue(
          validator.validate(dto).stream()
              .anyMatch(v ->
                  v.getPropertyPath().toString().equals("phone")
                      && v.getMessage().equals(
                      "Invalid phone number format"
                  )
              ),
          () -> "Expected phone validation to fail for: " + phone
      );
    }
  }

  @Test
  void shouldAcceptBoundaryLengthValues() {
    UpdateUserDto dto = new UpdateUserDto(
        "A".repeat(50),
        "B".repeat(50),
        "c".repeat(50),
        "+" + "1".repeat(15)
    );

    Set<ConstraintViolation<UpdateUserDto>> violations =
        validator.validate(dto);

    assertTrue(
        violations.isEmpty(),
        () -> "Unexpected violations: " + violations
    );
  }

  private void assertViolation(
      UpdateUserDto dto,
      String property,
      String expectedMessage
  ) {
    Set<ConstraintViolation<UpdateUserDto>> violations =
        validator.validate(dto);

    assertTrue(
        violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals(property)
                && v.getMessage().equals(expectedMessage)
        ),
        () -> "Expected violation on '%s' with message '%s', but got: %s"
            .formatted(property, expectedMessage, violations)
    );
  }

  static Stream<String> invalidUsernames() {
    return Stream.of(
        "ab",
        "a".repeat(51)
    );
  }
}
