package com.hlw.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.appointment.entity.AptAppointmentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约单数据访问组件。
 */
@Mapper
public interface AptAppointmentMapper extends BaseMapper<AptAppointmentEntity> {
}
