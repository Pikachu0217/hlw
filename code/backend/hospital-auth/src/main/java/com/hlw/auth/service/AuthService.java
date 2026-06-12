package com.hlw.auth.service;

import com.hlw.common.core.exception.BizException;

public class AuthService {
    private final UserRepository userRepository;
    private final TokenIssuer tokenIssuer;

    public AuthService(UserRepository userRepository, TokenIssuer tokenIssuer) {
        this.userRepository = userRepository;
        this.tokenIssuer = tokenIssuer;
    }

    public LoginResult login(LoginCommand command) {
        LoginUser user = userRepository.findByUsername(command.username());
        if (user == null || !matches(command.password(), user.password())) {
            throw new BizException(401, "用户名或密码错误");
        }
        String token = tokenIssuer.issue(user);
        return new LoginResult(token, user.tenantId(), user.userType());
    }

    private boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword != null && encodedPassword.startsWith("{noop}")) {
            return encodedPassword.substring("{noop}".length()).equals(rawPassword);
        }
        return encodedPassword != null && encodedPassword.equals(rawPassword);
    }
}
