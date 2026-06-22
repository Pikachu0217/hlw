package com.hlw.system.service.support;

import com.hlw.common.core.enums.HttpStatusEnum;
import com.hlw.common.core.exception.BizException;
import com.hlw.system.constants.SystemTenantConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 系统默认数据保护公共方法，统一处理默认数据的修改与删除校验。
 */
@Slf4j
public final class SystemDefaultDataGuard {

    /**
     * 禁止实例化工具类。
     */
    private SystemDefaultDataGuard() {
    }

    /**
     * 判断是否为系统默认数据。
     *
     * @param isDefault 默认数据标识
     * @return true 表示系统默认数据
     */
    public static boolean isSystemDefault(Integer isDefault) {
        return Objects.equals(SystemTenantConstants.SYSTEM_DEFAULT_DATA_FLAG, isDefault);
    }

    /**
     * 校验系统默认数据是否允许修改。
     *
     * @param isDefault 默认数据标识
     * @param resourceName 资源名称
     */
    public static void ensureCanUpdate(Integer isDefault, String resourceName) {
        ensureNotSystemDefault(isDefault, "修改", resourceName);
    }

    /**
     * 校验系统默认数据是否允许删除。
     *
     * @param isDefault 默认数据标识
     * @param resourceName 资源名称
     */
    public static void ensureCanDelete(Integer isDefault, String resourceName) {
        ensureNotSystemDefault(isDefault, "删除", resourceName);
    }

    /**
     * 校验系统默认数据是否允许执行指定操作。
     *
     * @param isDefault 默认数据标识
     * @param actionName 操作名称
     * @param resourceName 资源名称
     */
    private static void ensureNotSystemDefault(Integer isDefault, String actionName, String resourceName) {
        if (isSystemDefault(isDefault)) {
            log.warn("系统默认数据操作被拦截，resourceName={}，actionName={}，isDefault={}",
                resourceName, actionName, isDefault);
            throw new BizException(HttpStatusEnum.SYSTEM_DEFAULT_DATA_OPERATION_FORBIDDEN, actionName, resourceName);
        }
    }
}
