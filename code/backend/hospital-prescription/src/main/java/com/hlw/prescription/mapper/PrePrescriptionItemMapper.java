package com.hlw.prescription.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.prescription.entity.PrePrescriptionItemEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 处方药品明细数据访问组件。
 */
@Mapper
public interface PrePrescriptionItemMapper extends BaseMapper<PrePrescriptionItemEntity> {
}
