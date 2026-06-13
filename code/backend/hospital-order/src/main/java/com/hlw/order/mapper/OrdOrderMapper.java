package com.hlw.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.order.entity.OrdOrderEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单数据访问组件。
 */
@Mapper
public interface OrdOrderMapper extends BaseMapper<OrdOrderEntity> {
}
