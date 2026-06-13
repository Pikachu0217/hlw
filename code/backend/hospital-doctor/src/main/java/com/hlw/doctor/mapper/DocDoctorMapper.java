package com.hlw.doctor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.doctor.entity.DocDoctorEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医生数据访问组件。
 */
@Mapper
public interface DocDoctorMapper extends BaseMapper<DocDoctorEntity> {
}
