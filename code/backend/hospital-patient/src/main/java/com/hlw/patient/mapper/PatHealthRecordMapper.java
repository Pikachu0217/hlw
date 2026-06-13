package com.hlw.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.patient.entity.PatHealthRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 健康档案数据访问组件。
 */
@Mapper
public interface PatHealthRecordMapper extends BaseMapper<PatHealthRecordEntity> {
}
