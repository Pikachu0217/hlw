package com.hlw.gateway.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.gateway.entity.GwRouteConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 网关路由配置数据访问组件。
 */
@Mapper
public interface GwRouteConfigMapper extends BaseMapper<GwRouteConfigEntity> {
}
