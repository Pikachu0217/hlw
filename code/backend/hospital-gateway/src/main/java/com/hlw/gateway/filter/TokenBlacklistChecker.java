package com.hlw.gateway.filter;

import com.hlw.common.core.enums.RedisKeyEnum;
import com.hlw.common.core.security.AuthTokenResolver;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

/**
 * 网关侧的 Token 黑名单校验器，基于 Redisson 反应式客户端访问 Redis。
 */
@Slf4j
public class TokenBlacklistChecker {

    private final RedissonReactiveClient redissonReactiveClient;
    private final String tokenPrefix;

    /**
     * 构造 Token 黑名单校验器。
     *
     * @param redissonReactiveClient Redisson 反应式客户端
     * @param tokenPrefix 登录令牌前缀
     */
    public TokenBlacklistChecker(RedissonReactiveClient redissonReactiveClient, String tokenPrefix) {
        this.redissonReactiveClient = redissonReactiveClient;
        this.tokenPrefix = tokenPrefix == null ? "" : tokenPrefix.trim();
    }

    /**
     * 判断当前令牌是否已加入黑名单。
     * 在 Spring Cloud Gateway 的响应式链路里，当前用 RedissonReactiveClient 是非阻塞的。
     *
     * @param tokenHeader 登录令牌请求头值
     * @return 是否命中黑名单
     */
    public Mono<Boolean> isBlacklisted(String tokenHeader) {
        String rawToken = AuthTokenResolver.resolve(tokenHeader, tokenPrefix);
        if (!StringUtils.hasText(rawToken)) {
            return Mono.just(false);
        }
        String key = RedisKeyEnum.getKey(RedisKeyEnum.AUTH_LOGOUT_BLACKLIST, rawToken);
        return redissonReactiveClient.getBucket(key).isExists()
                .onErrorResume(exception -> {
                    log.error("查询 Token 黑名单异常，key={}", key, exception);
                    return Mono.just(false);
                });
    }
}
