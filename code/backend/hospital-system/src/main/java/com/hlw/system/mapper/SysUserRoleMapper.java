package com.hlw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.system.entity.SysUserRoleEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关系数据访问组件。
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRoleEntity> {
}
