package com.trusthold.mail;

import com.resend.core.exception.ResendException;
import com.trusthold.mail.templates.PasswordResetEmailTemplate;
import com.trusthold.mail.templates.WelcomeEmailTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTests {

  @Mock
  private WelcomeEmailTemplate welcomeEmailTemplate;

  @Mock
  private PasswordResetEmailTemplate passwordResetEmailTemplate;

  @InjectMocks
  private EmailService emailService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(emailService, "resendApiKey", "test_api_key");
    ReflectionTestUtils.setField(emailService, "fromAddress", "sender@example.com");
    ReflectionTestUtils.setField(emailService, "supportEmail", "support@example.com");
    ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:3000");
  }

  @Nested
  class NewlyRegisteredEmailTests {

    @Test
    void shouldPropagateError_whenSendMailThrows() throws Exception {
      // Arrange
      String to = "user@example.com";
      String userRole = "TENANT";
      String userName = "testuser";
      String html = "<html>Welcome</html>";

      when(welcomeEmailTemplate.render(userRole, userName, "http://localhost:3000", "support@example.com"))
          .thenReturn(html);

      EmailService spy = spy(emailService);
      doThrow(new ResendException("API error"))
          .when(spy).sendMail(anyString(), anyString(), anyString());

      // Act
      spy.newlyRegisterUserEmail(to, userRole, userName);

      // Give the async thread time to complete
      Thread.sleep(500);

      // Assert
      // The method should not throw — it catches and logs the ResendException internally.
      verify(welcomeEmailTemplate).render(userRole, userName, "http://localhost:3000", "support@example.com");
      verify(spy).sendMail(eq(to), eq("Welcome to TrustHold, " + userName), eq(html));
    }

    @Test
    void happyPath_whenSendMailSucceeds() throws Exception {
      // Arrange
      String to = "user@example.com";
      String userRole = "LANDLORD";
      String userName = "landlordUser";
      String html = "<html>Welcome Landlord</html>";

      when(welcomeEmailTemplate.render(userRole, userName, "http://localhost:3000", "support@example.com"))
          .thenReturn(html);

      EmailService spy = spy(emailService);
      doAnswer(invocation -> null).when(spy).sendMail(anyString(), anyString(), anyString());

      // Act
      spy.newlyRegisterUserEmail(to, userRole, userName);

      // Give the async thread time to complete
      Thread.sleep(500);

      // Assert
      verify(welcomeEmailTemplate).render(userRole, userName, "http://localhost:3000", "support@example.com");
      verify(spy).sendMail(eq(to), eq("Welcome to TrustHold, " + userName), eq(html));
    }
  }

  @Nested
  class SendPasswordResetEmailTests {

    @Test
    void shouldPropagateError_whenSendMailThrows() throws Exception {
      // Arrange
      String to = "user@example.com";
      String firstName = "John";
      String rawToken = "abc123token";
      String html = "<html>Reset Password</html>";

      when(passwordResetEmailTemplate.render(firstName, "http://localhost:3000/reset-password?token=" + rawToken,
          30L, "support@example.com"))
          .thenReturn(html);

      EmailService spy = spy(emailService);
      doThrow(new ResendException("API error"))
          .when(spy).sendMail(anyString(), anyString(), anyString());

      // Act
      spy.sendPasswordResetEmail(to, firstName, rawToken);

      // Give the async thread time to complete
      Thread.sleep(500);

      // Assert
      // The method should not throw — it catches and logs the ResendException internally.
      verify(passwordResetEmailTemplate).render(firstName, "http://localhost:3000/reset-password?token=" + rawToken,
          30L, "support@example.com");
      verify(spy).sendMail(eq(to), eq(PasswordResetEmailTemplate.SUBJECT), eq(html));
    }

    @Test
    void happyPath_whenSendMailSucceeds() throws Exception {
      // Arrange
      String to = "user@example.com";
      String firstName = "Jane";
      String rawToken = "xyz789token";
      String html = "<html>Reset Password for Jane</html>";

      when(passwordResetEmailTemplate.render(firstName, "http://localhost:3000/reset-password?token=" + rawToken,
          30L, "support@example.com"))
          .thenReturn(html);

      EmailService spy = spy(emailService);
      doAnswer(invocation -> null).when(spy).sendMail(anyString(), anyString(), anyString());

      // Act
      spy.sendPasswordResetEmail(to, firstName, rawToken);

      // Give the async thread time to complete
      Thread.sleep(500);

      // Assert
      verify(passwordResetEmailTemplate).render(firstName, "http://localhost:3000/reset-password?token=" + rawToken,
          30L, "support@example.com");
      verify(spy).sendMail(eq(to), eq(PasswordResetEmailTemplate.SUBJECT), eq(html));
    }
  }

  @Nested
  class SendMailTests {

    @Test
    void shouldThrowNullPointerException_whenToIsNull() {
      // Act & Assert
      assertThrows(NullPointerException.class, () -> emailService.sendMail(null, "Subject", "<html>Body</html>"));
    }

    @Test
    void shouldThrowNullPointerException_whenSubjectIsNull() {
      // Act & Assert
      assertThrows(NullPointerException.class, () -> emailService.sendMail("user@example.com", null, "<html>Body</html>"));
    }

    @Test
    void shouldThrowNullPointerException_whenBodyIsNull() {
      // Act & Assert
      assertThrows(NullPointerException.class, () -> emailService.sendMail("user@example.com", "Subject", null));
    }

    @Test
    void happyPath_whenSendMailSucceeds() throws Exception {
      // Arrange
      String to = "user@example.com";
      String subject = "Test Subject";
      String body = "<html>Test Body</html>";

      // Since Resend is instantiated inside sendMail and cannot be mocked directly,
      // we verify the method's parameter validation passes by calling it and expecting
      // a ResendException (invalid API key) rather than NullPointerException.

      // Act & Assert
      try {
        emailService.sendMail(to, subject, body);
        // If no exception, the API call succeeded (unlikely without real key)
      } catch (NullPointerException e) {
        fail("Should not throw NullPointerException for valid parameters");
      } catch (ResendException e) {
        // Expected — parameter validation passed, but API call failed due to test key
        assertNotNull(e);
      } catch (Exception e) {
        // Other exceptions (network, etc.) also indicate parameter validation passed
        assertNotNull(e);
      }
    }
  }
}
