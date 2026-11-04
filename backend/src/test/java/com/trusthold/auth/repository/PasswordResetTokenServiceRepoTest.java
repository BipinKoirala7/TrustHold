package com.trusthold.auth.repository;

import com.trusthold.TestcontainersConfiguration;
import com.trusthold.auth.model.PasswordResetToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the JPQL in PasswordResetTokenRepository.consume against a real Postgres.
 * Business flow (hashing, user lookup, password update) is covered in
 * PasswordResetTokenServiceTests; here we only prove the query's own semantics.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
public class PasswordResetTokenServiceRepoTest {

  // Fixed clock value so boundary cases are deterministic.
  private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

  @Autowired
  private PasswordResetTokenRepository repository;

  private String hash;

  @BeforeEach
  void setUp() {
    hash = "hash-" + UUID.randomUUID();
  }

  private PasswordResetToken saveToken(String tokenHash, Instant expiresAt) {
    PasswordResetToken token = new PasswordResetToken();
    token.setUserId(UUID.randomUUID());
    token.setTokenHash(tokenHash);
    token.setExpiresAt(expiresAt);
    return repository.saveAndFlush(token);
  }

  @Test
  void shouldDeleteTokenAndReturnOne_whenHashMatchesAndTokenIsNotExpired() {
    PasswordResetToken token = saveToken(hash, NOW.plus(Duration.ofMinutes(15)));

    int deleted = repository.consume(hash, NOW);

    assertThat(deleted).isEqualTo(1);
    assertThat(repository.findById(token.getId())).isEmpty();
  }

  @Test
  void shouldReturnZeroAndKeepToken_whenTokenIsExpired() {
    PasswordResetToken token = saveToken(hash, NOW.minus(Duration.ofSeconds(1)));

    int deleted = repository.consume(hash, NOW);

    assertThat(deleted).isZero();
    assertThat(repository.findById(token.getId())).isPresent();
  }

  @Test
  void shouldReturnZeroAndKeepToken_whenExpiresAtEqualsNow() {
    // The query uses a strict "expiresAt > now", so a token expiring exactly now is already dead.
    PasswordResetToken token = saveToken(hash, NOW);

    int deleted = repository.consume(hash, NOW);

    assertThat(deleted).isZero();
    assertThat(repository.findById(token.getId())).isPresent();
  }

  @Test
  void shouldReturnZero_whenNoTokenMatchesHash() {
    PasswordResetToken other = saveToken("some-other-hash", NOW.plus(Duration.ofMinutes(15)));

    int deleted = repository.consume(hash, NOW);

    assertThat(deleted).isZero();
    assertThat(repository.findById(other.getId())).isPresent();
  }

  @Test
  void shouldOnlyDeleteTheMatchingToken_whenOtherValidTokensExist() {
    PasswordResetToken target = saveToken(hash, NOW.plus(Duration.ofMinutes(15)));
    PasswordResetToken other = saveToken("other-" + UUID.randomUUID(), NOW.plus(Duration.ofMinutes(15)));

    int deleted = repository.consume(hash, NOW);

    assertThat(deleted).isEqualTo(1);
    assertThat(repository.findById(target.getId())).isEmpty();
    assertThat(repository.findById(other.getId())).isPresent();
  }

  @Test
  void shouldBeSingleUse_whenConsumedTwice() {
    saveToken(hash, NOW.plus(Duration.ofMinutes(15)));

    int first = repository.consume(hash, NOW);
    int second = repository.consume(hash, NOW);

    assertThat(first).isEqualTo(1);
    assertThat(second).isZero();
  }

  @Test
  void shouldNotServeDeletedTokenFromPersistenceContext_afterConsume() {
    // saveAndFlush leaves the entity in the first-level cache. findById would return that stale
    // instance unless clearAutomatically = true evicted it after the bulk delete.
    PasswordResetToken token = saveToken(hash, NOW.plus(Duration.ofMinutes(15)));
    assertThat(repository.findById(token.getId())).isPresent();

    repository.consume(hash, NOW);

    assertThat(repository.findById(token.getId())).isEmpty();
  }
}
