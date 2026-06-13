package com.hlw.auth.service;

import com.hlw.common.core.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 认证业务服务，负责账号登录和登录用户资料读取。
 */
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final TokenIssuer tokenIssuer;

    /**
     * 构造认证服务。
     *
     * @param userRepository 用户仓储
     * @param tokenIssuer 令牌签发器
     */
    public AuthService(UserRepository userRepository, TokenIssuer tokenIssuer) {
        this.userRepository = userRepository;
        this.tokenIssuer = tokenIssuer;
    }

    /**
     * 执行账号密码登录。
     *
     * @param command 登录命令
     * @return 登录结果
     */
    public LoginResult login(LoginCommand command) {
        log.info("用户登录认证开始，username={}", command.username());
        LoginUser user = userRepository.findByUsername(command.username());
        if (user == null || !matches(command.password(), user.password())) {
            log.warn("用户登录认证失败，username={}", command.username());
            throw new BizException(401, "用户名或密码错误");
        }
        String token = tokenIssuer.issue(user);
        log.info("用户登录认证成功，userId={}，tenantId={}", user.id(), user.tenantId());
        return new LoginResult(token, user.tenantId(), user.userType());
    }

    /**
     * 按令牌读取登录用户资料。
     *
     * @param token 登录令牌
     * @return 登录用户资料
     */
    public Map<String, Object> profile(String token) {
        TokenPrincipal principal = parseToken(token);
        log.info("查询登录用户资料，userId={}，tenantId={}", principal.userId(), principal.tenantId());
        Map<String, Object> profile = userRepository.findProfileById(principal.userId(), principal.tenantId());
        if (profile.isEmpty()) {
            throw new BizException(404, "登录用户不存在");
        }
        return profile;
    }

    /**
     * 校验明文密码和存储密码是否匹配。
     *
     * @param rawPassword 明文密码
     * @param encodedPassword 存储密码
     * @return 是否匹配
     */
    private boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword != null && encodedPassword.startsWith("{noop}")) {
            return encodedPassword.substring("{noop}".length()).equals(rawPassword);
        }
        return encodedPassword != null && encodedPassword.equals(rawPassword);
    }

    /**
     * 从演示令牌中解析用户编号和租户编号。
     *
     * @param token 登录令牌
     * @return 令牌主体
     */
    private TokenPrincipal parseToken(String token) {
        if (token == null || token.isBlank()) {
            throw new BizException(401, "登录令牌不能为空");
        }
        String[] parts = token.split("-");
        if (parts.length < 4 || !"satoken".equals(parts[0]) || !"demo".equals(parts[1])) {
            throw new BizException(401, "登录令牌无效");
        }
        try {
            return new TokenPrincipal(Long.parseLong(parts[2]), Long.parseLong(parts[3]));
        } catch (NumberFormatException exception) {
            throw new BizException(401, "登录令牌无效");
        }
    }

    /**
     * 登录令牌主体。
     *
     * @param userId 用户编号
     * @param tenantId 租户编号
     */
    private record TokenPrincipal(Long userId, Long tenantId) {
    }
}
