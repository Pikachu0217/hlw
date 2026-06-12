package com.hlw.auth.service;

public record LoginUser(Long id, Long tenantId, String username, String password, String userType) {
}
