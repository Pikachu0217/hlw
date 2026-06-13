package com.hlw.drug.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.drug.entity.DrugStockEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 药品库存数据访问组件。
 */
@Mapper
public interface DrugStockMapper extends BaseMapper<DrugStockEntity> {
}
