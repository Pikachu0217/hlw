package com.hlw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.system.entity.SysDictEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统字典数据访问组件。
 */
@Mapper
public interface SysDictMapper extends BaseMapper<SysDictEntity> {
}
