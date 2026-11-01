package com.trusthold.shared.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Shared Bean Validation setup for the auth DTO tests.
 */
public abstract class DtoValidationTestSupport {

  protected static final String VALID_PASSWORD = "Password1@";
  protected static final String VALID_EMAIL = "user@example.com";

  private static ValidatorFactory factory;
  protected static Validator validator;

  @BeforeAll
  static void setUpValidator() {
    factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @AfterAll
  static void tearDownValidator() {
    if (factory != null) {
      factory.close();
    }
  }

  public <T> Set<ConstraintViolation<T>> validate(T dto) {
    return validator.validate(dto);
  }

  public <T> Set<String> violatedProperties(T dto) {
    return validate(dto).stream()
        .map(v -> v.getPropertyPath().toString())
        .collect(Collectors.toSet());
  }

  public <T> Set<String> messagesFor(T dto, String property) {
    return validate(dto).stream()
        .filter(v -> v.getPropertyPath().toString().equals(property))
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toSet());
  }
}
