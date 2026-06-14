package com.hlw.common.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码编码工具，使用 BCrypt 进行密码哈希。
 */
public final class PasswordEncoder {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordEncoder() {
    }

    /**
     * 对明文密码进行 BCrypt 编码。
     *
     * @param rawPassword 明文密码
     * @return 编码后的密码哈希
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验明文密码与哈希值是否匹配。
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 哈希密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
