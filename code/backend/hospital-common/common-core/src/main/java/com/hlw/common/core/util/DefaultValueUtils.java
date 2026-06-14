package com.hlw.common.core.util;

import java.math.BigDecimal;

/**
 * 默认值工具类，消除各模块中重复的 defaultXxx 方法。
 */
public final class DefaultValueUtils {

    private DefaultValueUtils() {
    }

    /**
     * 返回非空字符串，空值时返回默认值。
     *
     * @param value 待处理字符串
     * @param defaultValue 默认字符串
     * @return 非空字符串
     */
    public static String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    /**
     * 返回非空 Long，空值时返回默认值。
     *
     * @param value 待处理长整型
     * @param defaultValue 默认长整型
     * @return 非空长整型
     */
    public static Long defaultIfNull(Long value, Long defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * 返回非空 Integer，空值时返回默认值。
     *
     * @param value 待处理整型
     * @param defaultValue 默认整型
     * @return 非空整型
     */
    public static Integer defaultIfNull(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * 返回非空 BigDecimal，空值时返回默认值。
     *
     * @param value 待处理金额
     * @param defaultValue 默认金额
     * @return 非空金额
     */
    public static BigDecimal defaultIfNull(BigDecimal value, BigDecimal defaultValue) {
        return value == null ? defaultValue : value;
    }
}
