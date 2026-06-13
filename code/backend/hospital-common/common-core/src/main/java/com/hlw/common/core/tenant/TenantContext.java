package com.hlw.common.core.tenant;

public final class TenantContext {
    private static final ThreadLocal<Long> HOLDER = new ThreadLocal<>();

    private TenantContext() {
    }

    /**
     * 写入当前线程租户编号。
     *
     * @param tenantId 租户编号
     */
    public static void setTenantId(Long tenantId) {
        HOLDER.set(tenantId);
    }

    /**
     * 获取当前线程租户编号。
     *
     * @return 租户编号
     */
    public static Long getTenantId() {
        return HOLDER.get();
    }

    /**
     * 清理当前线程租户编号。
     */
    public static void clear() {
        HOLDER.remove();
    }
}
