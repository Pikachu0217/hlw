package com.hlw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.system.entity.SysTenantPackageMenuEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 租户套餐菜单关系数据访问组件。
 */
@Mapper
public interface SysTenantPackageMenuMapper extends BaseMapper<SysTenantPackageMenuEntity> {
    /**
     * 按租户套餐物理删除菜单绑定关系。
     *
     * @param tenantId 租户编号
     * @param packageId 套餐编号
     * @return 删除行数
     */
    @Delete("DELETE FROM sys_tenant_package_menu WHERE tenant_id = #{tenantId} AND package_id = #{packageId}")
    int physicalDeleteByPackageId(@Param("tenantId") String tenantId, @Param("packageId") Long packageId);
}
