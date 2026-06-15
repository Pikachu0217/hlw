package com.hlw.auth.controller;

import com.hlw.auth.service.AuthService;
import com.hlw.auth.service.LoginCommand;
import com.hlw.auth.service.LoginResult;
import com.hlw.auth.vo.UserProfileVO;
import com.hlw.common.core.domain.R;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.security.BearerTokenResolver;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

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
     * @param tenantHeader 租户请求头
     * @param command 登录命令
     * @return 登录结果
     */
    @PostMapping("/login")
    public R<LoginResult> login(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @Valid @RequestBody LoginCommand command
    ) {
        Long tenantId = resolveLoginTenantId(tenantHeader, command.tenantId());
        log.info("用户登录请求进入认证控制器，tenantId={}，username={}", tenantId, command.username());
        return R.ok(authService.login(command.withTenantId(tenantId)));
    }

    /**
     * 查询登录用户资料。
     *
     * @param token 登录令牌
     * @return 登录用户资料
     */
    @GetMapping("/profile")
    public R<UserProfileVO> profile(
            @RequestHeader(value = BearerTokenResolver.AUTHORIZATION_HEADER, required = false) String token
    ) {
        log.info("查询登录用户资料请求进入认证控制器");
        return R.ok(authService.profile(token));
    }

    /**
     * 用户退出登录。
     *
     * @return 空响应
     */
    @PostMapping("/logout")
    public R<Void> logout() {
        log.info("用户退出登录请求进入认证控制器");
        return R.ok(null);
    }

    /**
     * 解析登录请求租户编号，优先使用网关透传请求头。
     *
     * @param tenantHeader 租户请求头
     * @param bodyTenantId 请求体租户编号
     * @return 登录租户编号
     */
    private Long resolveLoginTenantId(String tenantHeader, Long bodyTenantId) {
        if (tenantHeader == null || tenantHeader.isBlank()) {
            return bodyTenantId;
        }
        try {
            long parsedTenantId = Long.parseLong(tenantHeader.trim());
            if (parsedTenantId <= 0L) {
                throw new BizException(400, "租户编号必须大于0");
            }
            return parsedTenantId;
        } catch (NumberFormatException exception) {
            log.warn("登录请求租户请求头格式错误，tenantHeader={}", tenantHeader);
            throw new BizException(400, "租户编号格式错误");
        }
    }
}
