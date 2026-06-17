package com.hlw.common.mybatis.datascope.interceptor;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.hlw.common.mybatis.datascope.context.DataScopeContext;
import com.hlw.common.mybatis.datascope.context.DataScopeContextHolder;
import com.hlw.common.mybatis.datascope.enums.DataScopeType;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据权限 SQL 改写核心。
 *
 * <p>逐表回调：MP 的 {@code DataPermissionInterceptor} 在 select/update/delete 的多表 from 中
 * 依次调用本方法。返回 null 表示"该表不追加任何条件"。</p>
 *
 * <p>{@link DataScopeType#TENANT} 显式跳过——租户 where 由 {@code TenantLineInnerInterceptor} 注入，
 * 避免重复加 {@code tenant_id = ?}。</p>
 *
 * <p>字段缺失（deptId/deptIds/userId 为空）时返回 {@code 1=0} 兜底而非抛异常，保证登录上下文异常时
 * 退化为"无数据可见"而不是 500。</p>
 */
@Slf4j
public class HlwDataPermissionHandler implements MultiDataPermissionHandler {

    /** 永远 false 的兜底表达式（{@code 1 = 0}）。 */
    private static final Expression DENY_ALL = new EqualsTo(new LongValue(1), new LongValue(0));

    private final DataScopeAnnotationResolver resolver;

    public HlwDataPermissionHandler(DataScopeAnnotationResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        try {
            return doGetSqlSegment(table, where, mappedStatementId);
        } catch (RuntimeException ex) {
            log.warn("DataScope handler failed on msId={}, deny by default", mappedStatementId, ex);
            return DENY_ALL;
        }
    }

    private Expression doGetSqlSegment(Table table, Expression where, String mappedStatementId) {
        DataScopeMeta meta = resolver.resolve(mappedStatementId);
        if (!meta.isPresent() || meta.isIgnore()) {
            return null;
        }

        DataScopeContext ctx = DataScopeContextHolder.get();
        if (ctx == null) {
            log.debug("DataScope annotated but no context, deny: msId={}", mappedStatementId);
            return combine(where, DENY_ALL);
        }
        if (ctx.isIgnoreAll()) {
            return null;
        }

        DataScopeType type = meta.getType();
        if (type == null || type == DataScopeType.ANNOTATION_INHERIT) {
            type = ctx.getEffectiveType();
        }
        if (type == null || type == DataScopeType.ALL || type == DataScopeType.TENANT) {
            return null;
        }

        Expression segment = switch (type) {
            case DEPT -> buildDeptEquals(table, meta, ctx);
            case DEPT_AND_CHILD -> buildDeptIn(table, meta, ctx);
            case SELF -> buildSelfEquals(table, meta, ctx);
            case CUSTOM -> buildCustom(meta, ctx);
            default -> null;
        };
        if (segment == null) {
            return DENY_ALL;
        }
        return combine(where, segment);
    }

    private Expression buildDeptEquals(Table table, DataScopeMeta meta, DataScopeContext ctx) {
        if (ctx.getDeptId() == null) {
            return DENY_ALL;
        }
        return new EqualsTo(column(table, meta.getDeptAlias(), meta.getDeptColumn()), new LongValue(ctx.getDeptId()));
    }

    private Expression buildDeptIn(Table table, DataScopeMeta meta, DataScopeContext ctx) {
        Set<Long> deptIds = ctx.getDeptIds();
        if (deptIds == null || deptIds.isEmpty()) {
            return DENY_ALL;
        }
        List<LongValue> items = new ArrayList<>(deptIds.size());
        for (Long id : deptIds) {
            items.add(new LongValue(id));
        }
        ExpressionList<LongValue> values = new ExpressionList<>(items);
        InExpression in = new InExpression();
        in.setLeftExpression(column(table, meta.getDeptAlias(), meta.getDeptColumn()));
        in.setRightExpression(values);
        return in;
    }

    private Expression buildSelfEquals(Table table, DataScopeMeta meta, DataScopeContext ctx) {
        if (ctx.getUserId() == null) {
            return DENY_ALL;
        }
        return new EqualsTo(column(table, meta.getUserAlias(), meta.getUserColumn()), new LongValue(ctx.getUserId()));
    }

    private Expression buildCustom(DataScopeMeta meta, DataScopeContext ctx) {
        String customSql = ctx.getCustomSql();
        if (customSql == null || customSql.isBlank()) {
            return DENY_ALL;
        }
        try {
            String resolvedSql = customSql
                    .replace("{deptAlias}", nullToEmpty(meta.getDeptAlias()))
                    .replace("{userAlias}", nullToEmpty(meta.getUserAlias()))
                    .replace("{tenantAlias}", nullToEmpty(meta.getTenantAlias()));
            Expression expression = CCJSqlParserUtil.parseCondExpression(resolvedSql);
            return expression == null ? DENY_ALL : expression;
        } catch (JSQLParserException ex) {
            log.warn("DataScope custom SQL parse failed, deny by 1=0: {}", customSql, ex);
            return DENY_ALL;
        }
    }

    private static Column column(Table table, String alias, String columnName) {
        String resolvedAlias = (alias == null || alias.isBlank()) ? tableAlias(table) : alias;
        return resolvedAlias == null ? new Column(columnName) : new Column(new Table(resolvedAlias), columnName);
    }

    private static String tableAlias(Table table) {
        if (table == null) {
            return null;
        }
        if (table.getAlias() != null && table.getAlias().getName() != null && !table.getAlias().getName().isBlank()) {
            return table.getAlias().getName();
        }
        return null;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static Expression combine(Expression where, Expression segment) {
        if (segment == null) {
            return where;
        }
        if (where == null) {
            return segment;
        }
        return new AndExpression(where, segment);
    }
}
