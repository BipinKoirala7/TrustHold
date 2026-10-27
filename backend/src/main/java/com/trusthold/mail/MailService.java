package com.trusthold.mail;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.Year;
import java.util.Locale;
import java.util.Map;

/**
 * Service for sending emails.
 */
@Service
@Slf4j
@EnableAsync
public class MailService {

  @Value("${resend.api.key}")
  private String resendApiKey;

  @Value("${mail.from:sender@example.com}")
  private String fromAddress;

  @Value("${mail.support:support@example.com}")
  private String supportEmail;

  @Value("${app.frontend-url:http://localhost:3000}")
  private String frontendUrl;

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

  private static final String WELCOME_TEMPLATE = """
      <!DOCTYPE html>
      <html lang="en">
      <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <meta name="color-scheme" content="light only">
      <title>Welcome to TrustHold</title>
      </head>
      <body style="margin:0;padding:0;background-color:#F7F7F2;">
      <div style="display:none;max-height:0;overflow:hidden;opacity:0;">{{preheader}}</div>
      <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0" style="background-color:#F7F7F2;">
      <tr><td align="center" style="padding:32px 16px;">
      <table role="presentation" width="560" cellpadding="0" cellspacing="0" border="0" style="width:100%;max-width:560px;">

      <tr><td style="padding:0 4px 16px 4px;font-family:Arial,Helvetica,sans-serif;font-size:20px;font-weight:bold;color:#166534;">TrustHold<span style="color:#F59E0B;">.</span></td></tr>

      <tr><td style="background-color:#FFFFFF;border:1px solid #E7E5DF;border-radius:12px;">
      <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0">

      <tr><td style="background-color:#166534;border-radius:12px 12px 0 0;padding:28px 32px;font-family:Arial,Helvetica,sans-serif;">
      <span style="display:inline-block;background-color:#F59E0B;color:#1C1917;font-size:12px;font-weight:bold;padding:4px 10px;border-radius:999px;">{{roleLabel}} account</span>
      <h1 style="margin:14px 0 0 0;font-size:24px;line-height:32px;color:#FFFFFF;font-weight:bold;">Welcome to TrustHold, {{firstName}}</h1>
      </td></tr>

      <tr><td style="padding:28px 32px 8px 32px;font-family:Arial,Helvetica,sans-serif;font-size:15px;line-height:24px;color:#1C1917;">
      <p style="margin:0 0 20px 0;">{{roleIntro}}</p>
      <p style="margin:0 0 12px 0;font-weight:bold;">Get started in three steps</p>
      <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0">
      <tr><td width="28" valign="top" style="padding:0 12px 14px 0;"><div style="width:28px;height:28px;line-height:28px;text-align:center;background-color:#DCFCE7;color:#166534;border-radius:14px;font-size:13px;font-weight:bold;">1</div></td><td valign="top" style="padding:3px 0 14px 0;font-size:15px;line-height:22px;color:#1C1917;">{{step1}}</td></tr>
      <tr><td width="28" valign="top" style="padding:0 12px 14px 0;"><div style="width:28px;height:28px;line-height:28px;text-align:center;background-color:#DCFCE7;color:#166534;border-radius:14px;font-size:13px;font-weight:bold;">2</div></td><td valign="top" style="padding:3px 0 14px 0;font-size:15px;line-height:22px;color:#1C1917;">{{step2}}</td></tr>
      <tr><td width="28" valign="top" style="padding:0 12px 14px 0;"><div style="width:28px;height:28px;line-height:28px;text-align:center;background-color:#DCFCE7;color:#166534;border-radius:14px;font-size:13px;font-weight:bold;">3</div></td><td valign="top" style="padding:3px 0 14px 0;font-size:15px;line-height:22px;color:#1C1917;">{{step3}}</td></tr>
      </table>
      </td></tr>

      <tr><td align="left" style="padding:4px 32px 28px 32px;font-family:Arial,Helvetica,sans-serif;">
      <a href="{{ctaUrl}}" style="display:inline-block;background-color:#166534;color:#FFFFFF;font-size:15px;font-weight:bold;text-decoration:none;padding:12px 24px;border-radius:8px;">{{ctaLabel}}</a>
      </td></tr>

      <tr><td style="padding:0 32px 28px 32px;">
      <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0"><tr><td style="background-color:#FEF3C7;border-radius:8px;padding:14px 16px;font-family:Arial,Helvetica,sans-serif;font-size:14px;line-height:21px;color:#1C1917;">The deposit stays safe until you both agree.</td></tr></table>
      </td></tr>

      </table>
      </td></tr>

      <tr><td align="center" style="padding:20px 16px 0 16px;font-family:Arial,Helvetica,sans-serif;font-size:12px;line-height:18px;color:#78716C;">
      Questions? Reply to this email or write to <a href="mailto:{{supportEmail}}" style="color:#166534;">{{supportEmail}}</a>.<br>
      You received this because you created a TrustHold account.<br>
      &copy; {{year}} TrustHold
      </td></tr>

      </table>
      </td></tr>
      </table>
      </body>
      </html>
      """;

  /**
   * Sends welcome email to newly registered user.
   * @param to domain where the email will be sent
   * @param userRole role of the newly registered user
   * @param userName username of the newly registered user
   */
  @Async
  public void newlyRegisterUserMail(String to, String userRole, String userName) {
    log.info("Sending welcome email to newly registered {}", userRole);

    RoleContent content = "LANDLORD".equals(userRole.toUpperCase(Locale.ROOT)) ? LANDLORD : TENANT;
    String firstName = userName == null || userName.isBlank() ? "there" : userName.trim().split("\\s+")[0];

    String htmlBody = render(WELCOME_TEMPLATE, Map.ofEntries(
        Map.entry("preheader", content.preheader()),
        Map.entry("roleLabel", content.roleLabel()),
        Map.entry("firstName", firstName),
        Map.entry("roleIntro", content.roleIntro()),
        Map.entry("step1", content.step1()),
        Map.entry("step2", content.step2()),
        Map.entry("step3", content.step3()),
        Map.entry("ctaUrl", frontendUrl + content.ctaPath()),
        Map.entry("ctaLabel", content.ctaLabel()),
        Map.entry("supportEmail", supportEmail),
        Map.entry("year", String.valueOf(Year.now().getValue()))));

    try {
      sendMail(to, "Welcome to TrustHold, " + firstName, htmlBody);
    } catch (ResendException e) {
      log.error("Failed to send welcome email to {}", to, e);
    }
  }

  private String render(String template, Map<String, String> values) {
    String result = template;
    for (Map.Entry<String, String> entry : values.entrySet()) {
      result = result.replace("{{" + entry.getKey() + "}}", HtmlUtils.htmlEscape(entry.getValue()));
    }
    return result;
  }

  /**
   * Sends email to the given domain. It's the main method that sets up
   * the email sending process.
   *
   * @param to domain where the email will be sent
   * @param subject subject of the email
   * @param body body of the email
   * @throws ResendException if the email fails to send
   */
  public void sendMail(String to, String subject, String body) throws ResendException {
    log.info("Sending email to {}", to);

    Resend resend = new Resend(resendApiKey);

    CreateEmailOptions options = CreateEmailOptions
        .builder()
        .from(fromAddress)
        .to(to)
        .subject(subject)
        .html(body)
        .build();

    CreateEmailResponse response = resend.emails().send(options);
    log.info("Email sent, id {}", response.getId());
  }
}