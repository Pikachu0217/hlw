package com.hlw.gateway.service.converter;

import com.hlw.gateway.domain.resp.RouteConfigResp;
import com.hlw.gateway.entity.GwRouteConfigEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 网关路由配置实体到展示对象的转换器。
 */
@Component
@RequiredArgsConstructor
public class RouteConfigConverter {
    /**
     * 转换为网关路由配置展示对象。
     *
     * @param entity 网关路由配置实体
     * @return 网关路由配置展示对象
     */
    public RouteConfigResp toResp(GwRouteConfigEntity entity) {
        RouteConfigResp resp = new RouteConfigResp();
        resp.setKey(String.valueOf(entity.getId()));
        resp.setRouteCode(entity.getRouteCode());
        resp.setUri(entity.getUri());
        resp.setPathPredicate(entity.getPathPredicate());
        resp.setSort(entity.getSort());
        resp.setStatus(entity.getStatus());
        resp.setRemark(entity.getRemark());
        return resp;
    }
}
