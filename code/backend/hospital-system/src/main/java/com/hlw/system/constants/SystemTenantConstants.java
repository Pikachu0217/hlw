package com.hlw.system.constants;

import com.hlw.common.core.constants.CommonConstants;

import java.util.Set;

/**
 * 系统模块租户初始化与权限隔离常量。
 */
public final class SystemTenantConstants {
    /** 平台租户编号字符串。 */
    public static final String PLATFORM_TENANT_ID = String.valueOf(CommonConstants.PLATFORM_TENANT_ID);
    /** 顶级菜单父编号。 */
    public static final Long ROOT_MENU_PARENT_ID = 0L;
    /** 正常状态字符串。 */
    public static final String STATUS_NORMAL = "0";
    /** 正常状态数值。 */
    public static final Integer STATUS_NORMAL_VALUE = 0;
    /** 默认角色排序。 */
    public static final Integer DEFAULT_ROLE_ORDER = 0;
    /** 默认数据权限范围。 */
    public static final Integer DEFAULT_DATA_SCOPE = 1;
    /** 默认菜单排序。 */
    public static final Integer DEFAULT_MENU_ORDER = 0;
    /** 默认菜单类型。 */
    public static final String DEFAULT_MENU_TYPE = "C";
    /** 按钮菜单类型。 */
    public static final String MENU_TYPE_BUTTON = "F";
    /** 默认菜单图标。 */
    public static final String DEFAULT_MENU_ICON = "#";
    /** 默认组件外链标识。 */
    public static final Integer DEFAULT_MENU_FRAME = 1;
    /** 租户管理员角色编码。 */
    public static final String TENANT_ADMIN_ROLE_CODE = "tenant_admin";
    /** 租户管理员角色名称。 */
    public static final String TENANT_ADMIN_ROLE_NAME = "租户管理员";
    /** 租户管理员角色排序。 */
    public static final Integer TENANT_ADMIN_ROLE_ORDER = 1;
    /** 租户普通用户角色编码。 */
    public static final String TENANT_USER_ROLE_CODE = "tenant_user";
    /** 租户普通用户角色名称。 */
    public static final String TENANT_USER_ROLE_NAME = "普通用户";
    /** 租户普通用户角色排序。 */
    public static final Integer TENANT_USER_ROLE_ORDER = 2;
    /** 默认租户管理员登录账号。 */
    public static final String DEFAULT_TENANT_ADMIN_USER_NAME = "admin";
    /** 默认租户管理员登录密码。 */
    public static final String DEFAULT_TENANT_ADMIN_PASSWORD = "123456";
    /** 默认系统用户类型。 */
    public static final String DEFAULT_USER_TYPE = "sys_user";
    /** 默认未知性别。 */
    public static final String DEFAULT_UNKNOWN_SEX = "2";
    /** 普通用户基础菜单权限标识集合。 */
    public static final Set<String> TENANT_USER_BASIC_PERMS = Set.of("dashboard:view");

    private SystemTenantConstants() {
    }
}
