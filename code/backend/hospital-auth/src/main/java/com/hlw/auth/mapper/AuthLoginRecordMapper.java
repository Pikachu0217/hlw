package com.hlw.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.auth.entity.AuthLoginRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 认证登录记录数据访问组件。
 */
@Mapper
public interface AuthLoginRecordMapper extends BaseMapper<AuthLoginRecordEntity> {
}
