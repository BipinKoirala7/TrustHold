package com.trusthold.user;

/**
 * Enum representing the role of a user in the system.
 * The roles TENANT & LANDLORD are self registered by user.
 * ADMIN users are created by the system and never created through the registration process.
 */
public enum Role {
  TENANT,
  LANDLORD,
  ADMIN;

  /**
   * Returns true if the role is TENANT or LANDLORD, false otherwise.
   *
   * @param role the role to validate
   * @return true if the role is TENANT or LANDLORD, false otherwise
   */
  public boolean validateRegistrationRole(Role role) {
    if (role != Role.TENANT && role != Role.LANDLORD) {
      throw new IllegalArgumentException(
          "Invalid registration role"
      );
    }

    return true;
  }
}
