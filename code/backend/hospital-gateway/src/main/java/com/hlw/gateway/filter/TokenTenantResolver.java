package com.hlw.gateway.filter;

@FunctionalInterface
public interface TokenTenantResolver {
    Long resolveTenantId(String token);
}
