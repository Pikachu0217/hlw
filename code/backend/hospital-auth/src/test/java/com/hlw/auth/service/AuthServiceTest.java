package com.hlw.auth.service;

import com.hlw.common.security.PasswordEncoder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceTest {
    private static final String TEST_JWT_SECRET = "test-secret-key-not-for-production-use-only-in-tests";

    @Test
    void login_returns_token_and_tenant_id() {
        FakeUserRepository users = new FakeUserRepository();
        users.save(new LoginUser(1L, 100L, "admin", PasswordEncoder.encode("admin123"), "ADMIN"));
        AuthService service = new AuthService(users, new FakeTokenIssuer(), TEST_JWT_SECRET);

        LoginResult result = service.login(new LoginCommand(100L, "admin", "admin123"));

        assertThat(result.token()).isEqualTo("test-token-1");
        assertThat(result.tenantId()).isEqualTo(100L);
        assertThat(result.userType()).isEqualTo("ADMIN");
    }

    @Test
    void wrong_password_throws_exception() {
        FakeUserRepository users = new FakeUserRepository();
        users.save(new LoginUser(1L, 100L, "admin", PasswordEncoder.encode("correct"), "ADMIN"));
        AuthService service = new AuthService(users, new FakeTokenIssuer(), TEST_JWT_SECRET);

        try {
            service.login(new LoginCommand(100L, "admin", "wrong"));
            assertThat(true).as("Expected BizException was not thrown").isFalse();
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("用户名或密码错误");
        }
    }
}
