package com.trusthold.mail.templates;

import com.trusthold.mail.dtos.LeaseInvitationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class LeaseInvitationEmailTemplate {

  private static final String TEMPLATE = "lease-invitation";
  public static final String SUBJECT = "You've been invited to a lease on TrustHold";

  private final EmailTemplateRenderer renderer;

  public String render(LeaseInvitationDto dto, String inviteLink) {
    return renderer.render(TEMPLATE, Map.of(
        "landlordName", dto.landlordName(),
        "propertyName", dto.propertyName(),
        "propertyAddress", dto.propertyAddress(),
        "depositAmount", dto.depositAmount(),
        "inviteLink", inviteLink,
        "expiryHours", String.valueOf(dto.expiryHours())
    ));
  }
}