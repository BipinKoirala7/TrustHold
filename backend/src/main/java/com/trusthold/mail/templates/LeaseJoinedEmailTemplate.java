package com.trusthold.mail.templates;

import com.trusthold.mail.events.LeaseJoinedEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Validated
public class LeaseJoinedEmailTemplate {

  private static final String TEMPLATE = "lease-joined";
  public static final String SUBJECT = "Your tenant has joined the lease on TrustHold";

  private final EmailTemplateRenderer renderer;

  /***
   * Renders the lease joined email template.
   * @param dto the lease joined event
   * @param dashboardLink the link to the user's dashboard
   * @return the rendered email
   */
  public String render(@Valid LeaseJoinedEvent dto, String dashboardLink) {
    return renderer.render(TEMPLATE, Map.of(
        "landlordName", dto.landlordName(),
        "tenantName", dto.tenantName(),
        "tenantEmail", dto.tenantEmail(),
        "propertyName", dto.propertyName(),
        "propertyAddress", dto.propertyAddress(),
        "depositAmount", dto.depositAmount(),
        "dashboardLink", dashboardLink
    ));
  }
}