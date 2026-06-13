package com.hlw.consult.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.consult.entity.ConConsultEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问诊单数据访问组件。
 */
@Mapper
public interface ConConsultMapper extends BaseMapper<ConConsultEntity> {
}
