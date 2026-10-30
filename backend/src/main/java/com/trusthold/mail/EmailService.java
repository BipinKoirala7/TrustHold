package com.trusthold.mail;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.trusthold.auth.service.PasswordResetTokenService;
import com.trusthold.mail.templates.PasswordResetEmailTemplate;
import com.trusthold.mail.templates.WelcomeEmailTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Service for sending emails.
 */
@Service
@Slf4j
@EnableAsync
@RequiredArgsConstructor
public class EmailService {

  private final PasswordResetEmailTemplate passwordResetEmailTemplate;
  private final WelcomeEmailTemplate welcomeEmailTemplate;

  @Value("${resend.api.key}")
  private String resendApiKey;

  @Value("${mail.from:sender@example.com}")
  private String fromAddress;

  @Value("${mail.support:support@example.com}")
  private String supportEmail;

  @Value("${app.frontend-url:http://localhost:3000}")
  private String frontendUrl;

  /**
   * Sends welcome email to newly registered user.
   * @param to domain where the email will be sent
   * @param userRole role of the newly registered user
   * @param userName username of the newly registered user
   */
  @Async
  public void newlyRegisterUserEmail(String to, String userRole, String userName) {
    log.info("Sending welcome email to newly registered {}", userRole);

    String html = welcomeEmailTemplate.render(userRole, userName, frontendUrl, supportEmail);

    try {
      sendMail(to, "Welcome to TrustHold, " + userName, html);
    } catch (ResendException e) {
      log.error("Failed to send welcome email to {}", to, e);
    }
  }

  /**
   * Sends password reset email to the user.
   * @param to domain where the email will be sent
   * @param firstName first name of the user
   * @param rawToken raw token for password reset
   */
  @Async
  public void sendPasswordResetEmail(String to, String firstName, String rawToken) {
    String resetUrl = frontendUrl + "/reset-password?token=" + rawToken;
    String html = passwordResetEmailTemplate.render(firstName, resetUrl, PasswordResetTokenService.TOKEN_EXPIRY_MINUTES, supportEmail);
    try{
      sendMail(to, PasswordResetEmailTemplate.SUBJECT, html);
    } catch (ResendException e) {
      log.error("Failed to send password reset email to {}", to, e);
    }
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

    Objects.requireNonNull(to, "to cannot be null");
    Objects.requireNonNull(subject, "subject cannot be null");
    Objects.requireNonNull(body, "body cannot be null");

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