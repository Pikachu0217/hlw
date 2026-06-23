package com.hlw.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Redis Key 枚举类，集中管理项目中所有写入 Redis 的 Key 命名规则。
 * <p>命名规则：{@code hlw:{模块}:{业务}:{业务标识}}，便于在 Redis 中按业务前缀检索与清理。</p>
 */
@Getter
@AllArgsConstructor
public enum RedisKeyEnum {

    /**
     * 退出登录后的 Token 黑名单 Key，完整 key 形如 {@code hlw:auth:logout:{token}}，过期时间由令牌剩余有效期动态决定。
     */
    AUTH_LOGOUT_BLACKLIST("hlw:auth:logout:%s", 0L),

    /**
     * 患者端手机验证码 Key，完整 key 形如 {@code hlw:auth:phone-code:{phone}}，过期时间 300 秒。
     */
    AUTH_PHONE_CODE("hlw:auth:phone-code:%s", 300_000L);

    /**
     * Redis Key 前缀。
     */
    private final String key;

    /**
     * Redis Key 默认过期时间，单位毫秒；0 表示由业务动态指定。
     */
    private final long timeoutMillis;

    /**
     * 拼接 Redis 完整 Key。
     *
     * @param redisKey Redis Key 枚举
     * @param parts 业务标识片段
     * @return Redis 完整 Key
     */
    public static String getKey(RedisKeyEnum keyEnum, Object... parts) {
        return String.format(keyEnum.getKey(), parts);
    }
}
