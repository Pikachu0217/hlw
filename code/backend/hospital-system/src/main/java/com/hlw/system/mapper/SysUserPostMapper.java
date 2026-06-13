package com.hlw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.system.entity.SysUserPostEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户岗位关系数据访问组件。
 */
@Mapper
public interface SysUserPostMapper extends BaseMapper<SysUserPostEntity> {
}
