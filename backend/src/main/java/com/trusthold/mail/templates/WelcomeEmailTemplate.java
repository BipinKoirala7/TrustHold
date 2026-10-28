package com.trusthold.mail.templates;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WelcomeEmailTemplate {
  private static final String TEMPLATE = "welcome";

  private final EmailTemplateRenderer renderer;

  private record RoleContent(
      String roleLabel,
      String roleIntro,
      String step1,
      String step2,
      String step3,
      String ctaPath,
      String ctaLabel,
      String preheader) {}

  private static final RoleContent LANDLORD = new RoleContent(
      "Landlord",
      "Your account is ready. You can now create a lease, invite your tenant, and keep the deposit documented from move-in to move-out.",
      "Create a lease for your property and tenant",
      "Upload move-in photos for each part of the property",
      "Invite your tenant to pay the deposit through eSewa",
      "/dashboard",
      "Go to your dashboard",
      "Your landlord account is ready. Create your first lease.");

  private static final RoleContent TENANT = new RoleContent(
      "Tenant",
      "Your account is ready. Once your landlord adds you to a lease, you can pay your deposit and keep everything documented until move-out.",
      "Share the email you signed up with so your landlord can add you to a lease",
      "Pay your security deposit securely through eSewa",
      "Review and upload move-in photos for each part of the property",
      "/dashboard",
      "Go to your dashboard",
      "Your tenant account is ready. Your deposit stays safe until you both agree.");

  public String subject(String userName) {
    return "Welcome to TrustHold, " + displayName(userName);
  }

  public String render(String userRole, String userName, String frontendUrl, String supportEmail) {
    RoleContent content = "LANDLORD".equalsIgnoreCase(userRole) ? LANDLORD : TENANT;

    return renderer.render(TEMPLATE, Map.ofEntries(
        Map.entry("preheader", content.preheader()),
        Map.entry("roleLabel", content.roleLabel()),
        Map.entry("firstName", displayName(userName)),
        Map.entry("roleIntro", content.roleIntro()),
        Map.entry("step1", content.step1()),
        Map.entry("step2", content.step2()),
        Map.entry("step3", content.step3()),
        Map.entry("ctaUrl", frontendUrl + content.ctaPath()),
        Map.entry("ctaLabel", content.ctaLabel()),
        Map.entry("supportEmail", supportEmail),
        Map.entry("year", String.valueOf(Year.now().getValue()))));
  }

  private String displayName(String userName) {
    return userName == null || userName.isBlank() ? "there" : "@" + userName.trim();
  }
}
