package com.hlw.common.core.tenant;

/**
 * 租户上下文工具类，用于在多线程环境下存储和获取当前线程的租户信息
 * 使用ThreadLocal实现线程隔离，确保每个线程的租户信息独立存储
 */
public final class TenantContext {
    // 使用ThreadLocal存储当前线程的租户ID
    private static final ThreadLocal<Long> HOLDER = new ThreadLocal<>();
    // 使用ThreadLocal存储当前线程是否为平台请求的标记
    private static final ThreadLocal<Boolean> PLATFORM_HOLDER = new ThreadLocal<>();

    /**
     * 私有构造方法，防止实例化工具类
     */
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
