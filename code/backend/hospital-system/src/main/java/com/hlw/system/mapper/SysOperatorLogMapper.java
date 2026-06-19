package com.hlw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.system.entity.SysOperatorLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志数据访问组件。
 */
@Mapper
public interface SysOperatorLogMapper extends BaseMapper<SysOperatorLogEntity> {
}
