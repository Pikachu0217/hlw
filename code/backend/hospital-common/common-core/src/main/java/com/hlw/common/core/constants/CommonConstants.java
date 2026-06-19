package com.hlw.common.core.constants;

/**
 * 常量类
 */
public class CommonConstants {
    /**
     * jwt中的租户id
     */
    public static final String JWT_TENANT_ID = "tenantId";

    /**
     * jwt中的用户id
     */
    public static final String JWT_USER_ID = "userId";

    /**
     * jwt中的用户类型
     */
    public static final String JWT_USER_TYPE = "userType";

    /**
     * jwt的默认时效时间(单位 ms)
     */
    public static final Long JWT_DEFAULT_EXPIRATION_MS = 30L * 24 * 60 * 60 * 1000;



    /**
     * 隔离租户编号，用于无法解析租户或匿名访问的场景。
     */
    public static final long ISOLATED_TENANT_ID = -1L;

    /** 平台租户编号，只有平台账号的租户编号为 0，享有跨租户管理权限。 */
    public static final Long PLATFORM_TENANT_ID = 0L;

    /**
     * 判断租户编号是否为平台租户编号。
     *
     * @param tenantId 租户编号
     * @return 是否平台租户
     */
    public static boolean isPlatformTenant(Long tenantId) {
        return PLATFORM_TENANT_ID.equals(tenantId);
    }
}
