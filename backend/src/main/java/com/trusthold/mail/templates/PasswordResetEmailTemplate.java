package com.trusthold.mail.templates;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PasswordResetEmailTemplate {

  private static final String TEMPLATE = "reset-password";
  public static final String SUBJECT = "Reset your TrustHold password";

  private final EmailTemplateRenderer renderer;

  public String render(String firstName, String resetUrl, long expiresInMinutes, String supportEmail) {
    return renderer.render(TEMPLATE, Map.of(
        "firstName", firstName,
        "resetUrl", resetUrl,
        "expiresInMinutes", String.valueOf(expiresInMinutes),
        "supportEmail", supportEmail,
        "year", String.valueOf(Year.now().getValue())
    ));
  }
}
