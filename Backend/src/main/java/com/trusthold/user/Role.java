package com.trusthold.user;

/**
 * Enum representing the role of a user in the system.
 * The roles TENANT & LANDLORD are self registered by user.
 * ADMIN users are created by the system and never created through the registration process.
 */
public enum Role {
  TENANT,
  LANDLORD,
  ADMIN
}
