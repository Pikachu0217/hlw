package com.hlw.common.core.tenant;

public final class TenantContext {
    private static final ThreadLocal<Long> HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> PLATFORM_HOLDER = new ThreadLocal<>();

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
     * 写入当前线程平台上下文标记。
     *
     * @param platformRequest 是否平台请求
     */
    public static void setPlatformRequest(boolean platformRequest) {
        PLATFORM_HOLDER.set(platformRequest);
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
     * 判断当前线程是否平台上下文。
     *
     * @return 是否平台上下文
     */
    public static boolean isPlatformRequest() {
        return Boolean.TRUE.equals(PLATFORM_HOLDER.get());
    }

    /**
     * 清理当前线程租户编号。
     */
    public static void clear() {
        HOLDER.remove();
        PLATFORM_HOLDER.remove();
    }
}
