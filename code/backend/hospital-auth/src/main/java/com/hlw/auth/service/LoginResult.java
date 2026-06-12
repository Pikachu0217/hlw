package com.hlw.auth.service;

public record LoginResult(String token, Long tenantId, String userType) {
}
