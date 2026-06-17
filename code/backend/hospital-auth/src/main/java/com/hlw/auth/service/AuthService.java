package com.hlw.auth.service;

import com.hlw.auth.domain.dto.TokenIssuer;
import com.hlw.auth.domain.req.LoginReq;
import com.hlw.auth.domain.resp.LoginResultResp;
import com.hlw.auth.domain.resp.LoginUserResp;
import com.hlw.auth.domain.resp.UserDetailResp;
import com.hlw.common.core.config.AuthTokenProperties;
import com.hlw.common.core.enums.HttpStatusEnum;
import com.hlw.common.core.enums.RedisKeyEnum;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.security.TokenPrincipal;
import com.hlw.common.core.tenant.TokenPrincipalContext;
import com.hlw.common.core.util.JwtUtil;
import com.hlw.common.redis.service.RedisService;
import com.hlw.common.security.PasswordEncoder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

/**
 * 认证业务服务，负责账号登录和登录用户资料读取。
 */
@Service
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final TokenIssuer tokenIssuer;
    private final String jwtSecret;
    private final AuthTokenProperties authTokenProperties;
    private final RedisService redisService;

    /**
     * 构造认证服务。
     *
     * @param userRepository 用户仓储
     * @param tokenIssuer 令牌签发器
     * @param jwtSecret JWT 签名密钥
     * @param authTokenProperties 公共认证令牌配置属性
     * @param redisService Redis 操作服务，用于维护退出登录令牌黑名单
     */
    public AuthService(UserRepository userRepository, TokenIssuer tokenIssuer,
                       @Value("${hlw.jwt.secret}") String jwtSecret,
                       AuthTokenProperties authTokenProperties,
                       RedisService redisService) {
        this.userRepository = userRepository;
        this.tokenIssuer = tokenIssuer;
        this.jwtSecret = jwtSecret;
        this.authTokenProperties = authTokenProperties;
        this.redisService = redisService;
    }

    /**
     * 执行账号密码登录。
     *
     * @param loginReq 登录命令
     * @return 登录结果
     */
    public LoginResultResp login(LoginReq loginReq) {
        if (loginReq == null) {
            log.warn("用户登录认证失败，登录参数为空");
            throw new BizException(HttpStatusEnum.LOGIN_PARAMETERS_CANNOT_BE_NULL);
        }
        Long tenantId = requireLoginTenantId(loginReq.tenantId());
        log.info("用户登录认证开始，tenantId={}，username={}", tenantId, loginReq.username());
        LoginUserResp user = userRepository.findByTenantIdAndUsername(tenantId, loginReq.username());
        if (user == null || !matches(loginReq.password(), user.password())) {
            log.warn("用户登录认证失败，tenantId={}，username={}", tenantId, loginReq.username());
            throw new BizException(HttpStatusEnum.USERNAME_OR_PASSWORD_ERROR);
        }
        String token = tokenIssuer.issue(user);
        log.info("用户登录认证成功，userId={}，tenantId={}", user.id(), user.tenantId());
        return new LoginResultResp(token, user.tenantId(), user.userType());
    }

    /**
     * 按令牌读取登录用户资料。
     *
     * @return 登录用户资料
     */
    public UserDetailResp detail() {
        TokenPrincipal principal = TokenPrincipalContext.get();
        log.info("查询登录用户资料，userId={}，tenantId={}", principal.getUserId(), principal.getTenantId());
        UserDetailResp detail = userRepository.findProfileById(principal.getUserId(), principal.getTenantId());
        if (detail == null) {
            throw new BizException(HttpStatusEnum.LOGIN_USER_DOES_NOT_EXIST);
        }
        return detail;
    }

    /**
     * 注销登录令牌，将令牌写入 Redis 黑名单直至原令牌过期。
     * <p>令牌已过期或缺失时直接返回，不再写入 Redis；令牌格式无效时记录告警但不抛错，
     * 避免攻击者通过 logout 接口探测令牌格式。</p>
     *
     * @param rawToken 已剥离前缀的原始登录令牌
     */
    public void logout(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            log.warn("退出登录失败，令牌为空");
            throw new BizException(HttpStatusEnum.LOGOUT_TOKEN_REQUIRED);
        }
        Duration remaining = remainingValidity(rawToken);
        if (remaining == null) {
            log.info("退出登录令牌已失效，无需写入黑名单");
            return;
        }
        String key = RedisKeyEnum.getKey(RedisKeyEnum.AUTH_LOGOUT_BLACKLIST, rawToken);
        boolean success = redisService.set(key, "1", remaining);
        if (!success) {
            log.warn("退出登录写入黑名单失败，key={}", key);
        } else {
            log.info("退出登录写入黑名单成功，ttlSeconds={}", remaining.toSeconds());
        }
    }

    /**
     * 计算令牌相对当前时间的剩余有效时长。
     *
     * @param rawToken 原始登录令牌
     * @return 剩余时长；令牌已过期、无过期时间或解析失败时返回 null
     */
    private Duration remainingValidity(String rawToken) {
        try {
            Claims claims = JwtUtil.parseClaims(rawToken, jwtSecret);
            Date expiration = claims.getExpiration();
            if (expiration == null) {
                return null;
            }
            long remainingMs = expiration.getTime() - System.currentTimeMillis();
            return remainingMs > 0L ? Duration.ofMillis(remainingMs) : null;
        } catch (JwtException exception) {
            log.warn("退出登录解析令牌失败，{}", exception.getMessage());
            return null;
        }
    }

    /**
     * 校验明文密码和存储密码是否匹配。
     *
     * @param rawPassword 明文密码
     * @param encodedPassword 存储密码
     * @return 是否匹配
     */
    private boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        return PasswordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 校验登录租户编号。
     *
     * @param tenantId 租户编号
     * @return 有效租户编号
     */
    private Long requireLoginTenantId(Long tenantId) {
        if (tenantId == null || tenantId <= 0L) {
            log.warn("用户登录认证失败，租户编号无效，tenantId={}", tenantId);
            throw new BizException(400, "租户不能为空");
        }
        return tenantId;
    }
}
