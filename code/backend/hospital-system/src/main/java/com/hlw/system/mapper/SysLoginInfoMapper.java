package com.hlw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.system.entity.SysLoginInfoEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统访问记录数据访问组件。
 */
@Mapper
public interface SysLoginInfoMapper extends BaseMapper<SysLoginInfoEntity> {
}
