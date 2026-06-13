package com.hlw.doctor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.doctor.entity.DocDoctorDepartmentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医生科室关系数据访问组件。
 */
@Mapper
public interface DocDoctorDepartmentMapper extends BaseMapper<DocDoctorDepartmentEntity> {
}
