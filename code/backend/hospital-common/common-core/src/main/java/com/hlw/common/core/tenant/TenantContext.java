package com.hlw.common.core.tenant;

public final class TenantContext {
    private static final ThreadLocal<Long> HOLDER = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(Long tenantId) {
        HOLDER.set(tenantId);
    }

    public static Long getTenantId() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
