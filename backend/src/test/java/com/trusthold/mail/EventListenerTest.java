package com.trusthold.mail;

import com.trusthold.TestcontainersConfiguration;
import com.trusthold.mail.dtos.PasswordResetEvent;
import com.trusthold.mail.dtos.UserRegisteredEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class EventListenerTest {

  @Autowired
  private ApplicationEventPublisher publisher;

  @Autowired
  private PlatformTransactionManager txManager;

  @MockitoBean
  private EmailService emailService;

  private TransactionTemplate tx;

  private UserRegisteredEvent event;
  private PasswordResetEvent passwordResetEvent;

  @BeforeEach
  void setUp() {
    tx = new TransactionTemplate(txManager);
    event = new UserRegisteredEvent("a@b.com", "USER", "Bipin");
    passwordResetEvent = new PasswordResetEvent("a@b.com", "Bipin", "raw-token");
  }

  @Test
  void userRegistered_sendsEmailAfterCommit() {
    tx.executeWithoutResult(status ->
        publisher.publishEvent(event));

    verify(emailService).newlyRegisterUserEmail(event.email(), event.role(), event.name());
  }

  @Test
  void userRegistered_doesNotSendEmailOnRollback() {
    tx.executeWithoutResult(status -> {
      publisher.publishEvent(event);
      status.setRollbackOnly();
    });

    verifyNoInteractions(emailService);
  }

  @Test
  void passwordReset_sendsEmailAfterCommit() {
    tx.executeWithoutResult(status ->
        publisher.publishEvent(passwordResetEvent));

    verify(emailService).sendPasswordResetEmail(
        passwordResetEvent.to(), passwordResetEvent.firstName(), passwordResetEvent.rawToken());
  }
}