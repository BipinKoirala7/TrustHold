package com.trusthold.mail;

import com.trusthold.mail.dtos.PasswordResetEvent;
import com.trusthold.mail.dtos.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventListener {

  private final EmailService emailService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onUserRegistered(UserRegisteredEvent event) {
    log.info("User registered event received for {}", event.name());
    emailService.newlyRegisterUserEmail(event.email(), event.role(), event.name());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onPasswordReset(PasswordResetEvent event) {
    log.info("Password reset event received for {}", event.to());
    emailService.sendPasswordResetEmail(event.to(), event.firstName(), event.rawToken());
  }

//  @Async
//  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//  public void onDeletionRequested(AccountDeletionEvent event) {
//    String link = frontendUrl + "/confirm-delete?token=" + event.rawToken();
//    emailService.sendAccountDeletionEmail(
//        event.email(), event.fullName(), link, event.expiresAt());
//  }
}
