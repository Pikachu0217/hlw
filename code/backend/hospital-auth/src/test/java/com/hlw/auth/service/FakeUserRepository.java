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

    @Override
    public Map<String, Object> findProfileById(Long id, Long tenantId) {
        return users.values().stream()
            .filter(user -> user.id().equals(id) && user.tenantId().equals(tenantId))
            .findFirst()
            .map(user -> Map.<String, Object>of(
                "userId", user.id(),
                "tenantId", user.tenantId(),
                "username", user.username(),
                "userType", user.userType()
            ))
            .orElse(Map.of());
    }
}
