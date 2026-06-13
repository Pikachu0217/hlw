package com.hlw.doctor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.doctor.entity.DocScheduleEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医生排班数据访问组件。
 */
@Mapper
public interface DocScheduleMapper extends BaseMapper<DocScheduleEntity> {
}
