package com.trusthold.property.dtos;

import com.trusthold.property.model.PropertyType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class CreatePropertyDtoTest {

  private static ValidatorFactory factory;
  private static Validator validator;

  @BeforeAll
  static void setUp() {
    factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @AfterAll
  static void tearDown() {
    factory.close();
  }

  // ---------- happy path ----------

  @Test
  void validDto_hasNoViolations() {
    CreatePropertyDto dto = new CreatePropertyDto(
        "Lakeside Apartment", PropertyType.APARTMENT,
        "Lakeside Road 12", "Pokhara", "Two-bedroom flat near Phewa Lake");

    Set<ConstraintViolation<CreatePropertyDto>> violations = validator.validate(dto);

    assertThat(violations).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(PropertyType.class)
  void everyPropertyType_isAccepted(PropertyType type) {
    CreatePropertyDto dto = new CreatePropertyDto(
        "Lakeside Apartment", type,
        "Lakeside Road 12", "Pokhara", "Two-bedroom flat near Phewa Lake");

    Set<ConstraintViolation<CreatePropertyDto>> violations = validator.validate(dto);

    assertThat(violations).isEmpty();
  }

  @Test
  void fieldsAtMaximumLength_areValid() {
    CreatePropertyDto dto = new CreatePropertyDto(
        "a".repeat(150), PropertyType.HOUSE,
        "a".repeat(255), "a".repeat(100), "a".repeat(1000));

    Set<ConstraintViolation<CreatePropertyDto>> violations = validator.validate(dto);

    assertThat(violations).isEmpty();
  }

  // ---------- required fields ----------

  @Test
  void nullName_isRejected() {
    CreatePropertyDto dto = new CreatePropertyDto(
        null, PropertyType.APARTMENT,
        "Lakeside Road 12", "Pokhara", "Two-bedroom flat near Phewa Lake");

    Set<ConstraintViolation<CreatePropertyDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("name", "Property name is required"));
  }

  @Test
  void nullType_isRejected() {
    CreatePropertyDto dto = new CreatePropertyDto(
        "Lakeside Apartment", null,
        "Lakeside Road 12", "Pokhara", "Two-bedroom flat near Phewa Lake");

    Set<ConstraintViolation<CreatePropertyDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("type", "Property type is required"));
  }

  @Test
  void nullAddressLine_isRejected() {
    CreatePropertyDto dto = new CreatePropertyDto(
        "Lakeside Apartment", PropertyType.APARTMENT,
        null, "Pokhara", "Two-bedroom flat near Phewa Lake");

    Set<ConstraintViolation<CreatePropertyDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("addressLine", "Address line is required"));
  }

  @Test
  void nullCity_isRejected() {
    CreatePropertyDto dto = new CreatePropertyDto(
        "Lakeside Apartment", PropertyType.APARTMENT,
        "Lakeside Road 12", null, "Two-bedroom flat near Phewa Lake");

    Set<ConstraintViolation<CreatePropertyDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("city", "City is required"));
  }

  @Test
  void nullDescription_isRejected() {
    CreatePropertyDto dto = new CreatePropertyDto(
        "Lakeside Apartment", PropertyType.APARTMENT,
        "Lakeside Road 12", "Pokhara", null);

    Set<ConstraintViolation<CreatePropertyDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("description", "Description is required"));
  }

  // ---------- blank values ----------

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "   "})
  void blankName_isRejected(String name) {
    CreatePropertyDto dto = new CreatePropertyDto(
        name, PropertyType.APARTMENT,
        "Lakeside Road 12", "Pokhara", "Two-bedroom flat near Phewa Lake");

    Set<ConstraintViolation<CreatePropertyDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("name", "Property name is required"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "   "})
  void blankAddressLine_isRejected(String addressLine) {
    CreatePropertyDto dto = new CreatePropertyDto(
        "Lakeside Apartment", PropertyType.APARTMENT,
        addressLine, "Pokhara", "Two-bedroom flat near Phewa Lake");

    Set<ConstraintViolation<CreatePropertyDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("addressLine", "Address line is required"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "   "})
  void blankCity_isRejected(String city) {
    CreatePropertyDto dto = new CreatePropertyDto(
        "Lakeside Apartment", PropertyType.APARTMENT,
        "Lakeside Road 12", city, "Two-bedroom flat near Phewa Lake");

    Set<ConstraintViolation<CreatePropertyDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("city", "City is required"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "   "})
  void blankDescription_isRejected(String description) {
    CreatePropertyDto dto = new CreatePropertyDto(
        "Lakeside Apartment", PropertyType.APARTMENT,
        "Lakeside Road 12", "Pokhara", description);

    Set<ConstraintViolation<CreatePropertyDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("description", "Description is required"));
  }

  // ---------- length limits ----------

  @Test
  void nameLongerThan150Characters_isRejected() {
    CreatePropertyDto dto = new CreatePropertyDto(
        "a".repeat(151), PropertyType.APARTMENT,
        "Lakeside Road 12", "Pokhara", "Two-bedroom flat near Phewa Lake");

    Set<ConstraintViolation<CreatePropertyDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("name", "Property name must be at most 150 characters"));
  }

  @Test
  void addressLineLongerThan255Characters_isRejected() {
    CreatePropertyDto dto = new CreatePropertyDto(
        "Lakeside Apartment", PropertyType.APARTMENT,
        "a".repeat(256), "Pokhara", "Two-bedroom flat near Phewa Lake");

    Set<ConstraintViolation<CreatePropertyDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("addressLine", "Address must be at most 255 characters"));
  }

  @Test
  void cityLongerThan100Characters_isRejected() {
    CreatePropertyDto dto = new CreatePropertyDto(
        "Lakeside Apartment", PropertyType.APARTMENT,
        "Lakeside Road 12", "a".repeat(101), "Two-bedroom flat near Phewa Lake");

    Set<ConstraintViolation<CreatePropertyDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("city", "City must be at most 100 characters"));
  }

  @Test
  void descriptionLongerThan1000Characters_isRejected() {
    CreatePropertyDto dto = new CreatePropertyDto(
        "Lakeside Apartment", PropertyType.APARTMENT,
        "Lakeside Road 12", "Pokhara", "a".repeat(1001));

    Set<ConstraintViolation<CreatePropertyDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("description", "Description must be at most 1000 characters"));
  }

  // ---------- several errors at once ----------

  @Test
  void multipleInvalidFields_reportEachViolation() {
    CreatePropertyDto dto = new CreatePropertyDto(
        "", null, " ", "a".repeat(101), null);

    Set<ConstraintViolation<CreatePropertyDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsExactlyInAnyOrder(
            "Property name is required",
            "Property type is required",
            "Address line is required",
            "City must be at most 100 characters",
            "Description is required");
  }
}