package com.trusthold.lease.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Lease activity types
@Getter
@AllArgsConstructor
public enum Activity {
  LEASE_CREATED("Lease Created"),
  TENANT_JOINED("Tenant Joined"),
  LEASE_PHOTOS_UPLOADED("Lease Photos Uploaded"),
  LEASE_UPDATED("Lease Updated"),
  LEASE_DELETED("Lease Deleted");

  private final String label;

  public String toString(){
      return label;
  }
}
