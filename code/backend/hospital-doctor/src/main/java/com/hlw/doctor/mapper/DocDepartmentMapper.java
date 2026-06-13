package com.hlw.doctor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.doctor.entity.DocDepartmentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 科室数据访问组件。
 */
@Mapper
public interface DocDepartmentMapper extends BaseMapper<DocDepartmentEntity> {
}
