package com.hlw.auth.service;

import java.util.HashMap;
import java.util.Map;

class FakeUserRepository implements UserRepository {
    private final Map<String, LoginUser> users = new HashMap<>();

    void save(LoginUser user) {
        users.put(user.username(), user);
    }

    @Override
    public LoginUser findByUsername(String username) {
        return users.get(username);
    }
}
