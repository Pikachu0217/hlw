package com.hlw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.system.entity.SysPermissionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统权限码数据访问组件。
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermissionEntity> {
}
