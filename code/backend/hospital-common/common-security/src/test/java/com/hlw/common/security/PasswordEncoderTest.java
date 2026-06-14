package com.hlw.common.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordEncoderTest {
    @Test
    void encode_generates_bcrypt_hash() {
        String encoded = PasswordEncoder.encode("my-password");

        assertThat(encoded).isNotNull();
        assertThat(encoded).startsWith("$2a$");
    }

    @Test
    void matches_returns_true_for_correct_password() {
        String encoded = PasswordEncoder.encode("correct-password");

        assertThat(PasswordEncoder.matches("correct-password", encoded)).isTrue();
    }

    @Test
    void matches_returns_false_for_wrong_password() {
        String encoded = PasswordEncoder.encode("correct-password");

        assertThat(PasswordEncoder.matches("wrong-password", encoded)).isFalse();
    }

    @Test
    void each_encode_produces_different_hash() {
        String hash1 = PasswordEncoder.encode("same-password");
        String hash2 = PasswordEncoder.encode("same-password");

        assertThat(hash1).isNotEqualTo(hash2);
    }
}
