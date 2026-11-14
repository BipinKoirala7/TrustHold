package com.trusthold.lease.dtos;

import com.trusthold.shared.dtos.DtoValidationTestSupport;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class LeaseActivityEventTests extends DtoValidationTestSupport {

  private static final UUID LEASE_ID = UUID.randomUUID();

  @Test
  @DisplayName("valid event has no violations")
  void validEventHasNoViolations() {
    LeaseActivityEvent event =
        new LeaseActivityEvent(LEASE_ID, "MOVE_OUT_INITIATED", "Tenant started move-out");

    assertThat(validator.validate(event)).isEmpty();
  }

  @Test
  @DisplayName("null leaseId is rejected")
  void nullLeaseIdIsRejected() {
    LeaseActivityEvent event =
        new LeaseActivityEvent(null, "LEASE_CREATED", "Lease created");

    Set<ConstraintViolation<LeaseActivityEvent>> violations = validator.validate(event);

    assertThat(violations).hasSize(1);
    ConstraintViolation<LeaseActivityEvent> violation = violations.iterator().next();
    assertThat(violation.getPropertyPath()).hasToString("leaseId");
    assertThat(violation.getMessage()).isEqualTo("Lease is required");
  }

  @ParameterizedTest(name = "activity = [{0}] is rejected")
  @NullAndEmptySource
  @ValueSource(strings = {" ", "   ", "\t", "\n"})
  @DisplayName("blank or null activity is rejected")
  void blankActivityIsRejected(String activity) {
    LeaseActivityEvent event =
        new LeaseActivityEvent(LEASE_ID, activity, "Some description");

    Set<ConstraintViolation<LeaseActivityEvent>> violations = validator.validate(event);

    assertThat(violations).hasSize(1);
    ConstraintViolation<LeaseActivityEvent> violation = violations.iterator().next();
    assertThat(violation.getPropertyPath()).hasToString("activity");
    assertThat(violation.getMessage()).isEqualTo("Activity is required");
  }

  @ParameterizedTest(name = "description = [{0}] is rejected")
  @NullAndEmptySource
  @ValueSource(strings = {" ", "   ", "\t", "\n"})
  @DisplayName("blank or null description is rejected")
  void blankDescriptionIsRejected(String description) {
    LeaseActivityEvent event =
        new LeaseActivityEvent(LEASE_ID, "LEASE_CREATED", description);

    Set<ConstraintViolation<LeaseActivityEvent>> violations = validator.validate(event);

    assertThat(violations).hasSize(1);
    ConstraintViolation<LeaseActivityEvent> violation = violations.iterator().next();
    assertThat(violation.getPropertyPath()).hasToString("description");
    assertThat(violation.getMessage()).isEqualTo("Description is required");
  }

  @Test
  @DisplayName("all invalid fields are reported together")
  void allViolationsAreReported() {
    LeaseActivityEvent event = new LeaseActivityEvent(null, "", " ");

    Set<ConstraintViolation<LeaseActivityEvent>> violations = validator.validate(event);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactlyInAnyOrder(
            org.assertj.core.groups.Tuple.tuple("leaseId", "Lease is required"),
            org.assertj.core.groups.Tuple.tuple("activity", "Activity is required"),
            org.assertj.core.groups.Tuple.tuple("description", "Description is required"));
  }

  @Test
  @DisplayName("record accessors return the constructed values")
  void accessorsReturnValues() {
    LeaseActivityEvent event =
        new LeaseActivityEvent(LEASE_ID, "SETTLED", "Admin closed the lease");

    assertThat(event.leaseId()).isEqualTo(LEASE_ID);
    assertThat(event.activity()).isEqualTo("SETTLED");
    assertThat(event.description()).isEqualTo("Admin closed the lease");
  }
}
