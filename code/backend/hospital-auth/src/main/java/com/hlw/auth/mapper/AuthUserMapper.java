package com.hlw.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.auth.entity.AuthUserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 认证用户数据访问组件。
 */
@Mapper
public interface AuthUserMapper extends BaseMapper<AuthUserEntity> {
}
