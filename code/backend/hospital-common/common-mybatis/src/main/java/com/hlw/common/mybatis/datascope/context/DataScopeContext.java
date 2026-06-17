package com.hlw.common.mybatis.datascope.context;

import com.hlw.common.mybatis.datascope.enums.DataScopeType;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 数据权限运行时上下文，由业务侧加载点（如 HandlerInterceptor）填充并写入 {@link DataScopeContextHolder}。
 *
 * <p>不持有"中文 dataScope"原始字符串——业务加载点用 {@link DataScopeType#fromCode(String)} 翻译后写入
 * {@link #effectiveType}。</p>
 */
@Data
@Builder
public class DataScopeContext {

    /** 当前登录用户 id。SELF 维度依赖该字段。 */
    private Long userId;

    /** 当前用户所属部门 id；可为 null。DEPT 维度依赖该字段。 */
    private Long deptId;

    /** "本部门及子部门"展开后的部门 id 集合；由加载方负责展开，框架不查 sys_dept。 */
    private Set<Long> deptIds;

    /** 当前用户的角色 id 列表，仅用于调试与扩展，框架不直接使用。 */
    private List<Long> roleIds;

    /** 多角色聚合后真正生效的类型；为 null 时拦截器视同 ALL（不过滤）。 */
    private DataScopeType effectiveType;

    /** CUSTOM 类型对应的 SQL 条件片段（不含 WHERE 关键字，参数已拼好）。 */
    private String customSql;

    /** 整体跳过数据权限，例如平台超级管理员请求。 */
    private boolean ignoreAll;
}
