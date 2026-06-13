package com.hlw.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.appointment.entity.AptReleaseConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 放号配置数据访问组件。
 */
@Mapper
public interface AptReleaseConfigMapper extends BaseMapper<AptReleaseConfigEntity> {
}
