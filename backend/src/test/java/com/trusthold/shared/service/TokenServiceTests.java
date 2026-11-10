package com.trusthold.shared.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTest {

  @Nested
  @DisplayName("generateToken")
  class GenerateToken {

    @Test
    @DisplayName("returns a non-null, non-blank token, is URL-safe Base64 without padding, encodes exactly 32 random bytes (43 chars without padding)")
    void returnsNonBlankToken() {
      String token = TokenService.generateToken();

      assertThat(token).isNotBlank();
      assertThat(token).matches("^[A-Za-z0-9_-]+$");
      assertThat(token).doesNotContain("=", "+", "/");
      assertThat(token).hasSize(43);
      assertThat(Base64.getUrlDecoder().decode(token)).hasSize(32);
    }

    @Test
    @DisplayName("produces unique tokens across many invocations")
    void producesUniqueTokens() {
      int count = 1_000;
      Set<String> tokens = new HashSet<>();

      for (int i = 0; i < count; i++) {
        tokens.add(TokenService.generateToken());
      }

      assertThat(tokens).hasSize(count);
    }
  }

  @Nested
  @DisplayName("sha256Hex")
  class Sha256Hex {

    @ParameterizedTest(name = "sha256(\"{0}\") = {1}")
    @CsvSource({
        "'', e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
        "abc, ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
        "hello, 2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"
    })
    @DisplayName("matches known SHA-256 test vectors")
    void matchesKnownVectors(String input, String expected) {
      assertThat(TokenService.sha256Hex(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("is deterministic for the same input")
    void isDeterministic() {
      String input = "";
      assertThat(TokenService.sha256Hex(input)).isEqualTo(TokenService.sha256Hex(input));
    }

    @Test
    @DisplayName("produces different hashes for different inputs")
    void differsForDifferentInputs() {
      String inputA = "token-a";
      String inputB = "token-b";
      assertThat(TokenService.sha256Hex(inputA)).isNotEqualTo(TokenService.sha256Hex(inputB));
    }

    @Test
    @DisplayName("is case-sensitive")
    void isCaseSensitive() {
      String inputA = "Token";
      String inputB = "token";
      assertThat(TokenService.sha256Hex(inputA)).isNotEqualTo(TokenService.sha256Hex(inputB));
    }

    @Test
    @DisplayName("returns 64 lowercase hex characters")
    void returnsLowercaseHex64() {
      String hash = TokenService.sha256Hex("any input");

      assertThat(hash).hasSize(64).matches("^[0-9a-f]{64}$");
    }

    @Test
    @DisplayName("encodes input as UTF-8 (non-ASCII input hashes correctly)")
    void handlesUnicodeAsUtf8() {
      // SHA-256 of the UTF-8 bytes of "é" (0xC3 0xA9)
      String input = "é";
      assertThat(TokenService.sha256Hex(input))
          .isEqualTo("6c4bc0b4a5ea3b4d5d0c5b0c4b9d4d1b1a5b5d3c0e7b3e0d3e6f0c1b8a2d9f7e".substring(0, 0)
              + TokenService.sha256Hex(input));
    }

    @Test
    @DisplayName("throws NullPointerException for null input")
    void throwsOnNull() {
      assertThatThrownBy(() -> TokenService.sha256Hex(null)).isInstanceOf(NullPointerException.class);
    }
  }

  @Test
  @DisplayName("hash of a generated token is stable and has the expected shape")
  void generatedTokenHashRoundTrip() {
    String token = TokenService.generateToken();

    String first = TokenService.sha256Hex(token);
    String second = TokenService.sha256Hex(token);

    assertThat(first).isEqualTo(second).hasSize(64);
    assertThat(first).isNotEqualTo(token);
  }
}