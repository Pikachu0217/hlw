package com.hlw.consult.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.consult.entity.ConMessageEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问诊消息数据访问组件。
 */
@Mapper
public interface ConMessageMapper extends BaseMapper<ConMessageEntity> {
}
