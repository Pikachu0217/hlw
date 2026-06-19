package com.hlw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.system.entity.SysTenantPackageEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户套餐数据访问组件。
 */
@Mapper
public interface SysTenantPackageMapper extends BaseMapper<SysTenantPackageEntity> {
}
