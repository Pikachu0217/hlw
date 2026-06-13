package com.hlw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.system.entity.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户数据访问组件。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUserEntity> {
}
