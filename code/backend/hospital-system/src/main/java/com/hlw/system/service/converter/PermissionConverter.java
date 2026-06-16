package com.hlw.system.service.converter;

import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.entity.SysPermissionEntity;
import com.hlw.system.vo.PermissionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 权限码实体到展示对象的转换器。菜单名称由调用方传入，避免转换器触发额外的 Mapper 查询。
 */
@Component
@RequiredArgsConstructor
public class PermissionConverter {

    /**
     * 转换为权限码展示对象。
     *
     * @param entity 权限码实体
     * @param menuName 菜单名称
     * @return 权限码展示对象
     */
    public PermissionVO toPermissionVO(SysPermissionEntity entity, String menuName) {
        PermissionVO vo = new PermissionVO();
        vo.setKey(String.valueOf(entity.getId()));
        vo.setPermissionName(entity.getPermissionName());
        vo.setPermissionCode(entity.getPermissionCode());
        vo.setResourceType(entity.getResourceType());
        vo.setMenuName(DefaultValueUtils.defaultIfBlank(menuName, "-"));
        vo.setStatus(entity.getStatus());
        return vo;
    }
}
