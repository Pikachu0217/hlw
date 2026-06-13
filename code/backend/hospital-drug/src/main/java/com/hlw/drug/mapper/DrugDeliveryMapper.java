package com.hlw.drug.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.drug.entity.DrugDeliveryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 药品配送数据访问组件。
 */
@Mapper
public interface DrugDeliveryMapper extends BaseMapper<DrugDeliveryEntity> {
}
