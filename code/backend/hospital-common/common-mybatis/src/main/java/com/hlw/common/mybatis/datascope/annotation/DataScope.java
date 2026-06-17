package com.hlw.common.mybatis.datascope.annotation;

import com.hlw.common.mybatis.datascope.enums.DataScopeType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在 Mapper 方法或 Mapper 接口上启用数据权限改写。
 *
 * <p>方法级注解优先于类级注解。未标注的方法默认不过滤（loose 模式）。</p>
 *
 * <p>列默认 {@code dept_id} / {@code create_by}；多表 join 时通过 {@link #deptAlias} / {@link #userAlias}
 * 显式指定别名，避免拦截器在多表场景下挑错表。</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 强制指定数据权限类型。
     * <p>默认 {@link DataScopeType#ANNOTATION_INHERIT} 表示按当前线程上下文 effectiveType 解析。</p>
     */
    DataScopeType type() default DataScopeType.ANNOTATION_INHERIT;

    /** 部门列名。 */
    String deptColumn() default "dept_id";

    /** 部门列别名；为空时拦截器按当前 Table 的 alias / name 推断。 */
    String deptAlias() default "";

    /** 创建人列名。 */
    String userColumn() default "create_by";

    /** 创建人列别名。 */
    String userAlias() default "";

    /** 租户列别名（仅供 CUSTOM SQL 片段占位使用；TENANT 维度由 TenantLineInnerInterceptor 处理）。 */
    String tenantAlias() default "";

    /** 标识为忽略：拦截器直接跳过该方法（不影响 TenantLineInnerInterceptor）。 */
    boolean ignore() default false;
}
