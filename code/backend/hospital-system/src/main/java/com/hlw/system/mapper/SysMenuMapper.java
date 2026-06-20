package com.hlw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hlw.system.entity.SysMenuEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统菜单数据访问组件。
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenuEntity> {
    /**
     * 按租户物理删除全部菜单。
     *
     * @param tenantId 租户编号
     * @return 删除行数
     */
    @Delete("DELETE FROM sys_menu WHERE tenant_id = #{tenantId}")
    int physicalDeleteByTenantId(@Param("tenantId") String tenantId);
}
