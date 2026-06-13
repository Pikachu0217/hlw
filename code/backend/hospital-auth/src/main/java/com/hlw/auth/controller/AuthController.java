package com.hlw.auth.controller;

import com.hlw.auth.service.AuthService;
import com.hlw.auth.service.LoginCommand;
import com.hlw.auth.service.LoginResult;
import com.hlw.auth.vo.UserProfileVO;
import com.hlw.common.core.domain.R;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器，提供登录、资料和退出登录接口。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * 构造认证控制器。
     *
     * @param authService 认证服务
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录。
     *
     * @param command 登录命令
     * @return 登录结果
     */
    @PostMapping("/login")
    public R<LoginResult> login(@RequestBody LoginCommand command) {
        return R.ok(authService.login(command));
    }

    /**
     * 查询登录用户资料。
     *
     * @param token 登录令牌
     * @return 登录用户资料
     */
    @GetMapping("/profile")
    public R<UserProfileVO> profile(@RequestHeader(value = "satoken", required = false) String token) {
        return R.ok(authService.profile(token));
    }

    /**
     * 用户退出登录。
     *
     * @return 空响应
     */
    @PostMapping("/logout")
    public R<Void> logout() {
        return R.ok(null);
    }
}
