package com.trusthold.auth.dtos;

import com.trusthold.auth.model.RegistrationRole;
import com.trusthold.shared.dtos.DtoValidationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleRegistrationRequestTest extends DtoValidationTestSupport {

  @ParameterizedTest
  @EnumSource(RegistrationRole.class)
  @DisplayName("every Role value is accepted")
  void anyRoleIsValid(RegistrationRole role) {
    assertThat(validate(new GoogleRegistrationRequest(role))).isEmpty();
  }

  @Test
  @DisplayName("null role fails with 'Role is required'")
  void nullRole() {
    assertThat(messagesFor(new GoogleRegistrationRequest(null), "role"))
        .containsExactly("Role is required");
  }
}
