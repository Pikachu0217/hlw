package com.hlw.auth.service;

public interface UserRepository {
    LoginUser findByUsername(String username);
}
