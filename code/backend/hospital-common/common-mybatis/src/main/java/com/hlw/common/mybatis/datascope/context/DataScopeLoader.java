package com.hlw.common.mybatis.datascope.context;

/**
 * 数据权限上下文加载器接口；由业务侧实现，负责根据当前登录信息聚合多角色 dataScope、查询部门集合等。
 *
 * <p>框架本身不提供实现，也不强制业务必须实现该接口——它只是为后续业务侧统一加载点提供一个建议契约。</p>
 */
@FunctionalInterface
public interface DataScopeLoader {

    /**
     * 加载当前线程的数据权限上下文。
     *
     * @param userId   当前登录用户 id，可为 null
     * @param tenantId 当前租户 id，可为 null
     * @return 已聚合好的数据权限上下文；返回 null 表示当前请求不需要数据权限过滤
     */
    DataScopeContext load(Long userId, Long tenantId);
}
