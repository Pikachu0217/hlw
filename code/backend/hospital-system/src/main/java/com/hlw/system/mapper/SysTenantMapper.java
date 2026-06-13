package com.hlw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.system.entity.SysTenantEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户数据访问组件。
 */
@Mapper
public interface SysTenantMapper extends BaseMapper<SysTenantEntity> {
}
