package com.hlw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.system.entity.SysMenuEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统菜单数据访问组件。
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenuEntity> {
}
