package com.hlw.auth.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceTest {
    @Test
    void login_returns_token_and_tenant_id() {
        FakeUserRepository users = new FakeUserRepository();
        users.save(new LoginUser(1L, 100L, "admin", "{noop}admin123", "ADMIN"));
        AuthService service = new AuthService(users, new FakeTokenIssuer());

        LoginResult result = service.login(new LoginCommand("admin", "admin123"));

        assertThat(result.token()).isEqualTo("test-token-1");
        assertThat(result.tenantId()).isEqualTo(100L);
        assertThat(result.userType()).isEqualTo("ADMIN");
    }
}
