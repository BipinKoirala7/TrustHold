package com.trusthold.lease.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class LeaseHelper {

  @PreAuthorize("hasRole('LANDLORD')")
  public void inviteTenantToLease(String tenantEmail, String leaseId) {
    // Logic to send an invitation email to the tenant
    // This could involve calling an email service or using a messaging queue
    System.out.println("Inviting tenant " + tenantEmail + " to lease " + leaseId);
  }
}
