package com.hlw.drug.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.drug.entity.DrugInfoEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 药品信息数据访问组件。
 */
@Mapper
public interface DrugInfoMapper extends BaseMapper<DrugInfoEntity> {
}
