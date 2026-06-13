package com.hlw.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.appointment.entity.AptNumberSourceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约号源数据访问组件。
 */
@Mapper
public interface AptNumberSourceMapper extends BaseMapper<AptNumberSourceEntity> {
}
