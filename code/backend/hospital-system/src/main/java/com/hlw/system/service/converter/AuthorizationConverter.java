package com.hlw.system.service.converter;

import com.hlw.system.entity.SysRoleMenuEntity;
import com.hlw.system.entity.SysUserRoleEntity;
import com.hlw.system.domain.resp.RelationBindingResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用户角色与角色菜单授权关系到展示对象的转换器。采用方法重载替代原 instanceof 分支判断。
 */
@Component
@RequiredArgsConstructor
public class AuthorizationConverter {

    /**
     * 转换用户角色绑定关系为展示对象。
     *
     * @param entity 用户角色实体
     * @param userId 用户业务编号
     * @param roleId 角色编号
     * @return 关系绑定展示对象
     */
    public RelationBindingResp toRelationBindingVO(SysUserRoleEntity entity, String userId, Long roleId) {
        RelationBindingResp vo = new RelationBindingResp();
        vo.setId(entity.getId());
        vo.setUserId(userId);
        vo.setRoleId(roleId);
        vo.setMenuId(null);
        return vo;
    }

    /**
     * 转换角色菜单绑定关系为展示对象。
     *
     * @param entity 角色菜单实体
     * @param roleId 角色编号
     * @param menuId 菜单编号
     * @return 关系绑定展示对象
     */
    public RelationBindingResp toRelationBindingVO(SysRoleMenuEntity entity, Long roleId, Long menuId) {
        RelationBindingResp vo = new RelationBindingResp();
        vo.setId(entity.getId());
        vo.setUserId(null);
        vo.setRoleId(roleId);
        vo.setMenuId(menuId);
        return vo;
    }
}
