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
     * 按租户物理删除套餐复制菜单关联的角色菜单绑定。
     *
     * @param tenantId 租户编号
     * @return 删除行数
     */
    @Delete("""
        DELETE rm
        FROM sys_role_menu rm
        INNER JOIN sys_menu menu ON rm.tenant_id = menu.tenant_id AND rm.menu_id = menu.id
        WHERE rm.tenant_id = #{tenantId}
          AND menu.source_menu_id IS NOT NULL
        """)
    int physicalDeleteByTenantCopiedMenus(@Param("tenantId") String tenantId);

    /**
     * 按主键物理删除角色菜单绑定关系。
     *
     * @param id 关系编号
     * @return 删除行数
     */
    @Delete("DELETE FROM sys_role_menu WHERE tenant_id = #{tenantId} AND id = #{id}")
    int physicalDeleteById(@Param("tenantId") String tenantId, @Param("id") Long id);
}
