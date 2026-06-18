package com.hlw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.system.entity.SysDeptEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统部门数据访问组件。
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDeptEntity> {
}
