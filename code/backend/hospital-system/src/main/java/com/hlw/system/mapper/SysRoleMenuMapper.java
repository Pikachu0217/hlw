package com.hlw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.system.entity.SysRoleMenuEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色菜单关系数据访问组件。
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenuEntity> {
    /**
     * 按角色物理删除菜单绑定关系。
     *
     * @param tenantId 租户编号
     * @param roleId 角色编号
     * @return 删除行数
     */
    @Delete("DELETE FROM sys_role_menu WHERE tenant_id = #{tenantId} AND role_id = #{roleId}")
    int physicalDeleteByRoleId(@Param("tenantId") String tenantId, @Param("roleId") Long roleId);

    /**
     * 按主键物理删除角色菜单绑定关系。
     *
     * @param id 关系编号
     * @return 删除行数
     */
    @Delete("DELETE FROM sys_role_menu WHERE tenant_id = #{tenantId} AND id = #{id}")
    int physicalDeleteById(@Param("tenantId") String tenantId, @Param("id") Long id);
}
