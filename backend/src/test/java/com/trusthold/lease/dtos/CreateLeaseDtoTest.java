package com.trusthold.lease.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class CreateLeaseDtoTest {

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
    Instant start = Instant.now().plus(10, ChronoUnit.DAYS);
    CreateLeaseDto dto = new CreateLeaseDto(
        UUID.randomUUID(), "tenant@example.com",
        new BigDecimal("15000.00"), new BigDecimal("30000.00"),
        start);

    Set<ConstraintViolation<CreateLeaseDto>> violations = validator.validate(dto);

    assertThat(violations).isEmpty();
  }

  @Test
  void rentAndDepositAtMaximumPrecision_isValid() {
    Instant start = Instant.now().plus(10, ChronoUnit.DAYS);
    CreateLeaseDto dto = new CreateLeaseDto(
        UUID.randomUUID(), "tenant@example.com",
        new BigDecimal("9999999999.99"), new BigDecimal("9999999999.99"),
        start);

    Set<ConstraintViolation<CreateLeaseDto>> violations = validator.validate(dto);

    assertThat(violations).isEmpty();
  }

  // ---------- required fields ----------

  @Test
  void nullPropertyId_isRejected() {
    Instant start = Instant.now().plus(10, ChronoUnit.DAYS);
    CreateLeaseDto dto = new CreateLeaseDto(
        null, "tenant@example.com",
        new BigDecimal("15000.00"), new BigDecimal("30000.00"),
        start);

    Set<ConstraintViolation<CreateLeaseDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("propertyId", "Property is required"));
  }

  @Test
  void nullMonthlyRent_isRejected() {
    Instant start = Instant.now().plus(10, ChronoUnit.DAYS);
    CreateLeaseDto dto = new CreateLeaseDto(
        UUID.randomUUID(), "tenant@example.com",
        null, new BigDecimal("30000.00"),
        start);

    Set<ConstraintViolation<CreateLeaseDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("monthlyRent", "Monthly rent is required"));
  }

  @Test
  void nullDepositAmount_isRejected() {
    Instant start = Instant.now().plus(10, ChronoUnit.DAYS);
    CreateLeaseDto dto = new CreateLeaseDto(
        UUID.randomUUID(), "tenant@example.com",
        new BigDecimal("15000.00"), null,
        start);

    Set<ConstraintViolation<CreateLeaseDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("depositAmount", "Deposit amount is required"));
  }

  @Test
  void nullStartDate_isRejected() {
    CreateLeaseDto dto = new CreateLeaseDto(
        UUID.randomUUID(), "tenant@example.com",
        new BigDecimal("15000.00"), new BigDecimal("30000.00"),
        null);

    Set<ConstraintViolation<CreateLeaseDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("startDate", "Start date is required"));
  }

  // ---------- tenant email ----------

  @ParameterizedTest
  @NullAndEmptySource
  void nullOrEmptyTenantEmail_isRejected(String email) {
    Instant start = Instant.now().plus(10, ChronoUnit.DAYS);
    CreateLeaseDto dto = new CreateLeaseDto(
        UUID.randomUUID(), email,
        new BigDecimal("15000.00"), new BigDecimal("30000.00"),
        start);

    Set<ConstraintViolation<CreateLeaseDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("tenantEmail", "Tenant is required"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"not-an-email", "missing-domain@", "@missing-local.com", "plain.com"})
  void invalidTenantEmailFormat_isRejected(String email) {
    Instant start = Instant.now().plus(10, ChronoUnit.DAYS);
    CreateLeaseDto dto = new CreateLeaseDto(
        UUID.randomUUID(), email,
        new BigDecimal("15000.00"), new BigDecimal("30000.00"),
        start);

    Set<ConstraintViolation<CreateLeaseDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("tenantEmail", "Tenant email must be a valid email address"));
  }

  // ---------- monthly rent ----------

  @ParameterizedTest
  @ValueSource(strings = {"0", "0.00", "-1", "-15000.50"})
  void nonPositiveMonthlyRent_isRejected(String rent) {
    Instant start = Instant.now().plus(10, ChronoUnit.DAYS);
    CreateLeaseDto dto = new CreateLeaseDto(
        UUID.randomUUID(), "tenant@example.com",
        new BigDecimal(rent), new BigDecimal("30000.00"),
        start);

    Set<ConstraintViolation<CreateLeaseDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("monthlyRent", "Monthly rent must be greater than zero"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"15000.123", "12345678901", "12345678901.00"})
  void monthlyRentWithTooManyDigits_isRejected(String rent) {
    Instant start = Instant.now().plus(10, ChronoUnit.DAYS);
    CreateLeaseDto dto = new CreateLeaseDto(
        UUID.randomUUID(), "tenant@example.com",
        new BigDecimal(rent), new BigDecimal("30000.00"),
        start);

    Set<ConstraintViolation<CreateLeaseDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("monthlyRent",
            "Monthly rent can have at most 10 digits and 2 decimal places"));
  }

  // ---------- deposit amount ----------

  @ParameterizedTest
  @ValueSource(strings = {"0", "0.00", "-1", "-30000.50"})
  void nonPositiveDepositAmount_isRejected(String deposit) {
    Instant start = Instant.now().plus(10, ChronoUnit.DAYS);
    CreateLeaseDto dto = new CreateLeaseDto(
        UUID.randomUUID(), "tenant@example.com",
        new BigDecimal("15000.00"), new BigDecimal(deposit),
        start);

    Set<ConstraintViolation<CreateLeaseDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("depositAmount", "Deposit amount must be greater than zero"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"30000.123", "12345678901", "12345678901.00"})
  void depositAmountWithTooManyDigits_isRejected(String deposit) {
    Instant start = Instant.now().plus(10, ChronoUnit.DAYS);
    CreateLeaseDto dto = new CreateLeaseDto(
        UUID.randomUUID(), "tenant@example.com",
        new BigDecimal("15000.00"), new BigDecimal(deposit),
        start);

    Set<ConstraintViolation<CreateLeaseDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("depositAmount",
            "Deposit amount can have at most 10 digits and 2 decimal places"));
  }

  // ---------- start date ----------

  @Test
  void startDateInThePast_isRejected() {
    Instant start = Instant.now().minus(2, ChronoUnit.DAYS);
    CreateLeaseDto dto = new CreateLeaseDto(
        UUID.randomUUID(), "tenant@example.com",
        new BigDecimal("15000.00"), new BigDecimal("30000.00"),
        start);

    Set<ConstraintViolation<CreateLeaseDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("startDate", "Start date cannot be in the past"));
  }

  // ---------- several errors at once ----------

  @Test
  void multipleInvalidFields_reportEachViolation() {
    Instant start = Instant.now().plus(10, ChronoUnit.DAYS);
    CreateLeaseDto dto = new CreateLeaseDto(
        null, "not-an-email",
        BigDecimal.ZERO, new BigDecimal("-5"),
        start);

    Set<ConstraintViolation<CreateLeaseDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsExactlyInAnyOrder(
            "Property is required",
            "Tenant email must be a valid email address",
            "Monthly rent must be greater than zero",
            "Deposit amount must be greater than zero");
  }
}