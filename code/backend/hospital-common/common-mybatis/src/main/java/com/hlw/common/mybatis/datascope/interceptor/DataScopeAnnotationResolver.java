package com.hlw.common.mybatis.datascope.interceptor;

import com.hlw.common.mybatis.datascope.annotation.DataScope;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.extern.slf4j.Slf4j;

/**
 * 按 {@code mappedStatementId} 反射查找 {@link DataScope} 注解。
 *
 * <p>查找顺序：方法上的 {@code @DataScope} → 方法所在 Mapper 接口（含父接口）上的 {@code @DataScope}。
 * 找不到时缓存 {@link DataScopeMeta#EMPTY}，避免对未注解的 200+ Mapper 方法反复反射。</p>
 */
@Slf4j
public class DataScopeAnnotationResolver {

    private final ConcurrentMap<String, DataScopeMeta> cache = new ConcurrentHashMap<>();

    /** 测试用：清空缓存。 */
    public void clearCache() {
        cache.clear();
    }

    /**
     * 按 mappedStatementId 解析；从不返回 null。
     *
     * @param mappedStatementId 形如 {@code com.hlw.system.mapper.SysUserMapper.selectPage}
     */
    public DataScopeMeta resolve(String mappedStatementId) {
        if (mappedStatementId == null) {
            return DataScopeMeta.EMPTY;
        }
        return cache.computeIfAbsent(mappedStatementId, this::doResolve);
    }

    private DataScopeMeta doResolve(String mappedStatementId) {
        int lastDot = mappedStatementId.lastIndexOf('.');
        if (lastDot <= 0 || lastDot == mappedStatementId.length() - 1) {
            return DataScopeMeta.EMPTY;
        }
        String className = mappedStatementId.substring(0, lastDot);
        String methodName = mappedStatementId.substring(lastDot + 1);

        Class<?> mapperClass;
        try {
            mapperClass = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException ex) {
            if (log.isDebugEnabled()) {
                log.debug("DataScope resolver: cannot load mapper class {}, skip", className);
            }
            return DataScopeMeta.EMPTY;
        }

        // 方法级注解优先；MP 自带的 selectPage / selectList 等同名方法通常不会有多签名，按方法名匹配第一个。
        DataScope methodAnnotation = findMethodAnnotation(mapperClass, methodName);
        if (methodAnnotation != null) {
            return DataScopeMeta.from(methodAnnotation);
        }
        DataScope typeAnnotation = mapperClass.getAnnotation(DataScope.class);
        if (typeAnnotation == null) {
            for (Class<?> parent : mapperClass.getInterfaces()) {
                typeAnnotation = parent.getAnnotation(DataScope.class);
                if (typeAnnotation != null) {
                    break;
                }
            }
        }
        return typeAnnotation == null ? DataScopeMeta.EMPTY : DataScopeMeta.from(typeAnnotation);
    }

    private DataScope findMethodAnnotation(Class<?> mapperClass, String methodName) {
        for (Method method : mapperClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                DataScope annotation = method.getAnnotation(DataScope.class);
                if (annotation != null) {
                    return annotation;
                }
            }
        }
        return null;
    }
}
