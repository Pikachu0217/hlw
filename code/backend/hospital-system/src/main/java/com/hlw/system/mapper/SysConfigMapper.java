package com.hlw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.system.entity.SysConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统参数配置数据访问组件。
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfigEntity> {
}
