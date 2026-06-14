package com.hlw.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {
    private static final String SECRET = "test-secret-key-for-unit-tests-only-do-not-use-in-production";

    @Test
    void issue_and_parse_jwt_token() {
        String token = JwtUtil.issue(42L, 100L, "ADMIN", SECRET);

        Claims claims = JwtUtil.parse(token, SECRET);
        assertThat(((Number) claims.get("userId")).longValue()).isEqualTo(42L);
        assertThat(((Number) claims.get("tenantId")).longValue()).isEqualTo(100L);
        assertThat(claims.get("userType")).isEqualTo("ADMIN");
    }

    @Test
    void wrong_secret_fails_parsing() {
        String token = JwtUtil.issue(1L, 100L, "ADMIN", SECRET);

        assertThatThrownBy(() -> JwtUtil.parse(token, "wrong-secret"))
                .isInstanceOf(JwtException.class);
    }
}
