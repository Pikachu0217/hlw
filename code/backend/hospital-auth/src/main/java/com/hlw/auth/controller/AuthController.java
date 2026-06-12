package com.hlw.auth.controller;

import com.hlw.auth.service.AuthService;
import com.hlw.auth.service.LoginCommand;
import com.hlw.auth.service.LoginResult;
import com.hlw.common.core.domain.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public R<LoginResult> login(@RequestBody LoginCommand command) {
        return R.ok(authService.login(command));
    }

    @GetMapping("/profile")
    public R<Map<String, Object>> profile() {
        return R.ok(Map.of());
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        return R.ok(null);
    }
}
