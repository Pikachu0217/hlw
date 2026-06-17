package com.hlw.common.mybatis.datascope.context;

/**
 * 数据权限上下文的 ThreadLocal 持有器，与 {@code TokenPrincipalContext} 对称。
 *
 * <p>典型用法：在 Web 层 {@code HandlerInterceptor#preHandle} 中读取 token、查询角色聚合，
 * 调用 {@link #set(DataScopeContext)} 写入；在 {@code afterCompletion} 调用 {@link #clear()}。</p>
 */
public final class DataScopeContextHolder {

    private static final ThreadLocal<DataScopeContext> HOLDER = new ThreadLocal<>();

    private DataScopeContextHolder() {
    }

    /** 写入当前线程上下文。 */
    public static void set(DataScopeContext context) {
        HOLDER.set(context);
    }

    /** 读取当前线程上下文；可能为 null。 */
    public static DataScopeContext get() {
        return HOLDER.get();
    }

    /** 当前线程是否已设置上下文。 */
    public static boolean hasContext() {
        return HOLDER.get() != null;
    }

    /** 清理当前线程上下文。 */
    public static void clear() {
        HOLDER.remove();
    }

    /**
     * 在 {@code action} 执行期间将 {@code ignoreAll} 临时置为 true（栈式恢复）。
     * 用于平台超级管理员、定时任务等需要绕过数据权限的场景。
     */
    public static void runWithIgnore(Runnable action) {
        DataScopeContext previous = HOLDER.get();
        DataScopeContext ignore = previous == null
                ? DataScopeContext.builder().ignoreAll(true).build()
                : DataScopeContext.builder()
                        .userId(previous.getUserId())
                        .deptId(previous.getDeptId())
                        .deptIds(previous.getDeptIds())
                        .roleIds(previous.getRoleIds())
                        .effectiveType(previous.getEffectiveType())
                        .customSql(previous.getCustomSql())
                        .ignoreAll(true)
                        .build();
        HOLDER.set(ignore);
        try {
            action.run();
        } finally {
            if (previous == null) {
                HOLDER.remove();
            } else {
                HOLDER.set(previous);
            }
        }
    }
}
