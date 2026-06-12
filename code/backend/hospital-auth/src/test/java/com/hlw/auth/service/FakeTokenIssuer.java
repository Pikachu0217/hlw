package com.hlw.auth.service;

class FakeTokenIssuer implements TokenIssuer {
    @Override
    public String issue(LoginUser user) {
        return "test-token-" + user.id();
    }
}
