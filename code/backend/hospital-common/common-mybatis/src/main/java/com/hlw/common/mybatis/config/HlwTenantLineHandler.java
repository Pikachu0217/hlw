package com.hlw.common.mybatis.config;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.hlw.common.core.constants.CommonConstants;
import com.hlw.common.core.tenant.TokenPrincipalContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

public class HlwTenantLineHandler implements TenantLineHandler {
        /**
         * 获取当前租户表达式。
         *
         * @return 租户表达式
         */
        @Override
        public Expression getTenantId() {
            Long tenantId = TokenPrincipalContext.get().getTenantId();
            return new LongValue(tenantId == null ? CommonConstants.ISOLATED_TENANT_ID : tenantId);
        }

        /**
         * 判断表是否忽略租户过滤。
         *
         * @param tableName 表名
         * @return 是否忽略
         */
        @Override
        public boolean ignoreTable(String tableName) {
            return "local_message".equalsIgnoreCase(tableName)
                    || "sys_tenant".equalsIgnoreCase(tableName)
                    || "sys_menu".equalsIgnoreCase(tableName)
                    || "sys_tenant_package".equalsIgnoreCase(tableName)
                    || "sys_tenant_package_menu".equalsIgnoreCase(tableName);
        }
    }
