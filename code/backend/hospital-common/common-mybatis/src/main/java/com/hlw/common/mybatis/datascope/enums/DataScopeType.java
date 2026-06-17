package com.hlw.common.mybatis.datascope.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据权限类型。
 *
 * <p>{@link #ANNOTATION_INHERIT} 是注解上的"哨兵值"：当 {@code @DataScope.type()} 取该值时，
 * 拦截器会读取当前线程上下文中的 {@code effectiveType} 来决定真正生效的类型；
 * 它不应当出现在 {@code DataScopeContext#effectiveType} 中，也不应当出现在数据库中。</p>
 */
public enum DataScopeType {

    /** 全部数据，不追加任何条件。 */
    ALL("ALL", "全部数据"),

    /** 本租户数据，由 {@code TenantLineInnerInterceptor} 处理，本拦截器不重复加条件。 */
    TENANT("TENANT", "本租户数据"),

    /** 本部门数据。 */
    DEPT("DEPT", "本部门数据"),

    /** 本部门及子部门数据；子部门集合由上下文加载方提前展开。 */
    DEPT_AND_CHILD("DEPT_AND_CHILD", "本部门及以下数据"),

    /** 本人数据，按创建人列过滤。 */
    SELF("SELF", "本人数据"),

    /** 自定义 SQL 片段。 */
    CUSTOM("CUSTOM", "自定义"),

    /** 哨兵值：表示注解未指定类型，按上下文 effectiveType 解析。仅用于注解默认值。 */
    ANNOTATION_INHERIT("ANNOTATION_INHERIT", "继承上下文");

    private final String code;
    private final String label;

    DataScopeType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    private static final Map<String, DataScopeType> BY_KEY = Arrays.stream(values())
            .flatMap(t -> java.util.stream.Stream.of(
                    Map.entry(t.code.toUpperCase(), t),
                    Map.entry(t.label, t)))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));

    /**
     * 从字符串解析类型；同时兼容英文 code（大小写不敏感）和中文 label（"本租户数据"等）。
     *
     * @param value 字符串值
     * @return 未识别时返回 {@code null}
     */
    public static DataScopeType fromCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return BY_KEY.get(value.toUpperCase());
    }
}
