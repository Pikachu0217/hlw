package com.hlw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.system.entity.SysUserRoleEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户角色关系数据访问组件。
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRoleEntity> {
    /**
     * 按用户物理删除角色绑定关系。
     *
     * @param tenantId 租户编号
     * @param userId 用户业务编号
     * @return 删除行数
     */
    @Delete("DELETE FROM sys_user_role WHERE tenant_id = #{tenantId} AND user_id = #{userId}")
    int physicalDeleteByUserId(@Param("tenantId") String tenantId, @Param("userId") String userId);

    /**
     * 按主键物理删除用户角色绑定关系。
     *
     * @param id 关系编号
     * @return 删除行数
     */
    @Delete("DELETE FROM sys_user_role WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);
}
