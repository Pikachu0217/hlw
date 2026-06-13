package com.hlw.prescription.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.prescription.entity.PrePrescriptionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 处方数据访问组件。
 */
@Mapper
public interface PrePrescriptionMapper extends BaseMapper<PrePrescriptionEntity> {
}
