package com.trusthold.mail;

import com.trusthold.mail.dtos.PasswordResetEvent;
import com.trusthold.mail.dtos.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EventListener {

  private final EmailService emailService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onUserRegistered(UserRegisteredEvent event) {
    emailService.newlyRegisterUserEmail(event.email(), event.role(), event.name());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onPasswordReset(PasswordResetEvent event) {
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
