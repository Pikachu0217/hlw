package com.hlw.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.patient.entity.PatPatientEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 患者档案数据访问组件。
 */
@Mapper
public interface PatPatientMapper extends BaseMapper<PatPatientEntity> {
}
