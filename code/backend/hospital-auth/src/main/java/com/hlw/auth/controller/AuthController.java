package com.hlw.auth.controller;

import com.hlw.auth.domain.req.LoginReq;
import com.hlw.auth.domain.resp.LoginResultResp;
import com.hlw.auth.domain.resp.UserDetailResp;
import com.hlw.auth.service.AuthService;
import com.hlw.common.core.config.AuthTokenProperties;
import com.hlw.common.core.domain.R;
import com.hlw.common.core.security.AuthTokenResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * 认证控制器，提供登录、资料和退出登录接口。
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final AuthTokenProperties authTokenProperties;

    /**
     * 构造认证控制器。
     *
     * @param authService 认证服务
     * @param authTokenProperties 公共认证令牌配置属性
     */
    public AuthController(AuthService authService, AuthTokenProperties authTokenProperties) {
        this.authService = authService;
        this.authTokenProperties = authTokenProperties;
    }

    /**
     * 用户登录。
     *
     * @param request HTTP 请求对象
     * @param loginReq 登录请求体
     * @return 登录结果
     */
    @PostMapping("/login")
    public R<LoginResultResp> login(
            HttpServletRequest request,
            @Valid @RequestBody LoginReq loginReq
    ) {
        String tenantHeader = request.getHeader(authTokenProperties.getTenantHeaderName());
        Long tenantId = AuthTokenResolver.resolveLoginTenantId(tenantHeader, loginReq.tenantId());
        log.info("用户登录请求进入认证控制器，tenantId={}，username={}", tenantId, loginReq.username());
        return R.ok(authService.login(loginReq.withTenantId(tenantId), resolveClientIp(request), request.getHeader("User-Agent")));
    }

    /**
     * 查询登录用户资料。
     *
     * @return 登录用户资料
     */
    @GetMapping("/detail")
    public R<UserDetailResp> detail() {
        log.info("查询登录用户资料请求进入认证控制器");
        return R.ok(authService.detail());
    }

    /**
     * 用户退出登录，将当前令牌加入黑名单直至原令牌过期。
     *
     * @param request HTTP 请求对象
     * @return 空响应
     */
    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest request) {
        String rawToken = AuthTokenResolver.resolve(
                request.getHeader(authTokenProperties.getTokenName()),
                authTokenProperties.getTokenPrefix()
        );
        log.info("用户退出登录请求进入认证控制器");
        authService.logout(rawToken);
        return R.ok(null);
    }

    /**
     * 解析客户端 IP。
     *
     * @param request HTTP 请求对象
     * @return 客户端 IP
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
