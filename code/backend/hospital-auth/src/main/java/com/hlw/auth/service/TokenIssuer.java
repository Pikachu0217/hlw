package com.hlw.auth.service;

public interface TokenIssuer {
    String issue(LoginUser user);
}
