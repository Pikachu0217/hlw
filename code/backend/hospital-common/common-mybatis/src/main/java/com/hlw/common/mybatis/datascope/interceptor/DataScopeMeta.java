package com.hlw.common.mybatis.datascope.interceptor;

import com.hlw.common.mybatis.datascope.annotation.DataScope;
import com.hlw.common.mybatis.datascope.enums.DataScopeType;

/**
 * {@link DataScope} 注解摊平后的不可变结构，供拦截器直接读取。
 */
public final class DataScopeMeta {

    /** 表示"目标方法/类未标注 {@code @DataScope}"的占位实例。 */
    public static final DataScopeMeta EMPTY = new DataScopeMeta(false, null, null, null, null, null, null, false);

    private final boolean present;
    private final DataScopeType type;
    private final String deptColumn;
    private final String deptAlias;
    private final String userColumn;
    private final String userAlias;
    private final String tenantAlias;
    private final boolean ignore;

    private DataScopeMeta(boolean present,
                          DataScopeType type,
                          String deptColumn,
                          String deptAlias,
                          String userColumn,
                          String userAlias,
                          String tenantAlias,
                          boolean ignore) {
        this.present = present;
        this.type = type;
        this.deptColumn = deptColumn;
        this.deptAlias = deptAlias;
        this.userColumn = userColumn;
        this.userAlias = userAlias;
        this.tenantAlias = tenantAlias;
        this.ignore = ignore;
    }

    public static DataScopeMeta from(DataScope annotation) {
        return new DataScopeMeta(
                true,
                annotation.type(),
                annotation.deptColumn(),
                annotation.deptAlias(),
                annotation.userColumn(),
                annotation.userAlias(),
                annotation.tenantAlias(),
                annotation.ignore());
    }

    public boolean isPresent() {
        return present;
    }

    public DataScopeType getType() {
        return type;
    }

    public String getDeptColumn() {
        return deptColumn;
    }

    public String getDeptAlias() {
        return deptAlias;
    }

    public String getUserColumn() {
        return userColumn;
    }

    public String getUserAlias() {
        return userAlias;
    }

    public String getTenantAlias() {
        return tenantAlias;
    }

    public boolean isIgnore() {
        return ignore;
    }
}
