package com.hlw.system.service.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.hlw.common.core.enums.DeletedStatusEnum;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.tenant.TokenPrincipalContext;

/**
 * MyBatis Plus 与租户上下文的公共工具方法集合，承载跨聚合复用的查询条件、忽略策略与守卫逻辑。
 */
public final class MybatisTenantHelpers {

    private MybatisTenantHelpers() {
    }

    /**
     * 构造忽略租户行拦截策略，用于平台上下文跨租户读写场景。
     *
     * @return 忽略策略
     */
    public static IgnoreStrategy ignoreTenantLine() {
        return IgnoreStrategy.builder().tenantLine(true).build();
    }

    /**
     * 构造未删除数据查询条件，统一替代各实体专属的 active 包装方法。
     *
     * @param deletedGetter 实体删除标记字段引用
     * @param <T> 实体类型
     * @return 查询条件
     */
    public static <T> LambdaQueryWrapper<T> notDeletedWrapper(SFunction<T, ?> deletedGetter) {
        return new LambdaQueryWrapper<T>().eq(deletedGetter, DeletedStatusEnum.NOT_DELETED.getType());
    }

    /**
     * 校验实体是否存在，否则抛出 404 业务异常。
     *
     * @param entity 查询结果
     * @param message 错误消息
     * @param <T> 实体类型
     * @return 非空实体
     */
    public static <T> T requireEntity(T entity, String message) {
        if (entity == null) {
            throw new BizException(404, message);
        }
        return entity;
    }

    /**
     * 校验当前请求是否处于平台上下文，否则抛出 403 业务异常。
     *
     * @param message 不满足条件时的错误消息
     */
    public static void ensurePlatformContext(String message) {
        if (!TokenPrincipalContext.get().getPlatformRequest()) {
            throw new BizException(403, message);
        }
    }
}
