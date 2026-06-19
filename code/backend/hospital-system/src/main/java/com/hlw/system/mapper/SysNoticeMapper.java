package com.hlw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.system.entity.SysNoticeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知公告数据访问组件。
 */
@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNoticeEntity> {
}
