package com.trusthold.lease.dtos;

import com.trusthold.lease.model.LeasePhotoPhase;
import com.trusthold.lease.model.PhotoLabel;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class CreateLeasePhotoDtoTest {

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
  void presetLabelWithoutCustomLabel_isValid() {
    CreateLeasePhotoDto dto = new CreateLeasePhotoDto(
        UUID.randomUUID(), LeasePhotoPhase.MOVE_IN, PhotoLabel.WALLS,
        null, "https://bucket.s3.amazonaws.com/leases/wall.jpg", UUID.randomUUID());

    Set<ConstraintViolation<CreateLeasePhotoDto>> violations = validator.validate(dto);

    assertThat(violations).isEmpty();
  }

  @Test
  void otherLabelWithCustomLabel_isValid() {
    CreateLeasePhotoDto dto = new CreateLeasePhotoDto(
        UUID.randomUUID(), LeasePhotoPhase.MOVE_OUT, PhotoLabel.OTHER,
        "Balcony", "https://bucket.s3.amazonaws.com/leases/balcony.jpg", UUID.randomUUID());

    Set<ConstraintViolation<CreateLeasePhotoDto>> violations = validator.validate(dto);

    assertThat(violations).isEmpty();
  }

  @Test
  void presetLabelWithCustomLabel_isValid() {
    // the entity clears customLabel for non-OTHER labels, so the DTO does not reject it
    CreateLeasePhotoDto dto = new CreateLeasePhotoDto(
        UUID.randomUUID(), LeasePhotoPhase.MOVE_IN, PhotoLabel.KITCHEN,
        "Ignored text", "https://bucket.s3.amazonaws.com/leases/kitchen.jpg", UUID.randomUUID());

    Set<ConstraintViolation<CreateLeasePhotoDto>> violations = validator.validate(dto);

    assertThat(violations).isEmpty();
  }

  @Test
  void customLabelAtMaximumLength_isValid() {
    CreateLeasePhotoDto dto = new CreateLeasePhotoDto(
        UUID.randomUUID(), LeasePhotoPhase.MOVE_IN, PhotoLabel.OTHER,
        "a".repeat(60), "https://bucket.s3.amazonaws.com/leases/x.jpg", UUID.randomUUID());

    Set<ConstraintViolation<CreateLeasePhotoDto>> violations = validator.validate(dto);

    assertThat(violations).isEmpty();
  }

  @Test
  void imageUrlAtMaximumLength_isValid() {
    CreateLeasePhotoDto dto = new CreateLeasePhotoDto(
        UUID.randomUUID(), LeasePhotoPhase.MOVE_IN, PhotoLabel.WALLS,
        null, "a".repeat(500), UUID.randomUUID());

    Set<ConstraintViolation<CreateLeasePhotoDto>> violations = validator.validate(dto);

    assertThat(violations).isEmpty();
  }

  // ---------- required fields ----------

  @Test
  void nullLeaseId_isRejected() {
    CreateLeasePhotoDto dto = new CreateLeasePhotoDto(
        null, LeasePhotoPhase.MOVE_IN, PhotoLabel.WALLS,
        null, "https://bucket.s3.amazonaws.com/leases/wall.jpg", UUID.randomUUID());

    Set<ConstraintViolation<CreateLeasePhotoDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("leaseId", "Lease is required"));
  }

  @Test
  void nullPhase_isRejected() {
    CreateLeasePhotoDto dto = new CreateLeasePhotoDto(
        UUID.randomUUID(), null, PhotoLabel.WALLS,
        null, "https://bucket.s3.amazonaws.com/leases/wall.jpg", UUID.randomUUID());

    Set<ConstraintViolation<CreateLeasePhotoDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("phase", "Photo phase is required"));
  }

  @Test
  void nullLabel_isRejected() {
    CreateLeasePhotoDto dto = new CreateLeasePhotoDto(
        UUID.randomUUID(), LeasePhotoPhase.MOVE_IN, null,
        null, "https://bucket.s3.amazonaws.com/leases/wall.jpg", UUID.randomUUID());

    Set<ConstraintViolation<CreateLeasePhotoDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("label", "Photo label is required"));
  }

  @Test
  void nullImageUrl_isRejected() {
    CreateLeasePhotoDto dto = new CreateLeasePhotoDto(
        UUID.randomUUID(), LeasePhotoPhase.MOVE_IN, PhotoLabel.WALLS,
        null, null, UUID.randomUUID());

    Set<ConstraintViolation<CreateLeasePhotoDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("imageUrl", "Image URL is required"));
  }

  @Test
  void nullUploadedBy_isRejected() {
    CreateLeasePhotoDto dto = new CreateLeasePhotoDto(
        UUID.randomUUID(), LeasePhotoPhase.MOVE_IN, PhotoLabel.WALLS,
        null, "https://bucket.s3.amazonaws.com/leases/wall.jpg", null);

    Set<ConstraintViolation<CreateLeasePhotoDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("uploadedBy", "Uploader is required"));
  }

  // ---------- image URL ----------

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "   "})
  void blankImageUrl_isRejected(String imageUrl) {
    CreateLeasePhotoDto dto = new CreateLeasePhotoDto(
        UUID.randomUUID(), LeasePhotoPhase.MOVE_IN, PhotoLabel.WALLS,
        null, imageUrl, UUID.randomUUID());

    Set<ConstraintViolation<CreateLeasePhotoDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("imageUrl", "Image URL is required"));
  }

  @Test
  void imageUrlLongerThan500Characters_isRejected() {
    CreateLeasePhotoDto dto = new CreateLeasePhotoDto(
        UUID.randomUUID(), LeasePhotoPhase.MOVE_IN, PhotoLabel.WALLS,
        null, "a".repeat(501), UUID.randomUUID());

    Set<ConstraintViolation<CreateLeasePhotoDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("imageUrl", "Image URL must be at most 500 characters"));
  }

  // ---------- custom label ----------

  @Test
  void otherLabelWithNullCustomLabel_isRejected() {
    CreateLeasePhotoDto dto = new CreateLeasePhotoDto(
        UUID.randomUUID(), LeasePhotoPhase.MOVE_IN, PhotoLabel.OTHER,
        null, "https://bucket.s3.amazonaws.com/leases/x.jpg", UUID.randomUUID());

    Set<ConstraintViolation<CreateLeasePhotoDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("customLabelProvided", "Custom label is required when the label is Other"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "   "})
  void otherLabelWithBlankCustomLabel_isRejected(String customLabel) {
    CreateLeasePhotoDto dto = new CreateLeasePhotoDto(
        UUID.randomUUID(), LeasePhotoPhase.MOVE_IN, PhotoLabel.OTHER,
        customLabel, "https://bucket.s3.amazonaws.com/leases/x.jpg", UUID.randomUUID());

    Set<ConstraintViolation<CreateLeasePhotoDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("customLabelProvided", "Custom label is required when the label is Other"));
  }

  @Test
  void customLabelLongerThan60Characters_isRejected() {
    CreateLeasePhotoDto dto = new CreateLeasePhotoDto(
        UUID.randomUUID(), LeasePhotoPhase.MOVE_IN, PhotoLabel.OTHER,
        "a".repeat(61), "https://bucket.s3.amazonaws.com/leases/x.jpg", UUID.randomUUID());

    Set<ConstraintViolation<CreateLeasePhotoDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage)
        .containsExactly(tuple("customLabel", "Custom label must be at most 60 characters"));
  }

  @Test
  void nullLabel_doesNotTriggerCustomLabelViolation() {
    CreateLeasePhotoDto dto = new CreateLeasePhotoDto(
        UUID.randomUUID(), LeasePhotoPhase.MOVE_IN, null,
        null, "https://bucket.s3.amazonaws.com/leases/x.jpg", UUID.randomUUID());

    Set<ConstraintViolation<CreateLeasePhotoDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsExactly("Photo label is required");
  }

  // ---------- several errors at once ----------

  @Test
  void multipleInvalidFields_reportEachViolation() {
    CreateLeasePhotoDto dto = new CreateLeasePhotoDto(
        null, null, PhotoLabel.OTHER,
        " ", "", null);

    Set<ConstraintViolation<CreateLeasePhotoDto>> violations = validator.validate(dto);

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsExactlyInAnyOrder(
            "Lease is required",
            "Photo phase is required",
            "Custom label is required when the label is Other",
            "Image URL is required",
            "Uploader is required");
  }
}