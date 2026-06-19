package com.hlw.system.service.support;

import java.util.UUID;

/**
 * 系统用户业务编号生成器。
 */
public final class UserIdGenerator {
    private static final String USER_ID_PREFIX = "U_";

    private UserIdGenerator() {
    }

    /**
     * 生成用户业务编号。
     *
     * @return 用户业务编号，格式为 U_ 加 32 位 UUID
     */
    public static String nextUserId() {
        return USER_ID_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }
}
