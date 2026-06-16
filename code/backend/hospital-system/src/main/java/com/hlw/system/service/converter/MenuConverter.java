package com.hlw.system.service.converter;

import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.entity.SysMenuEntity;
import com.hlw.system.vo.MenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 菜单实体到展示对象的转换器。
 */
@Component
@RequiredArgsConstructor
public class MenuConverter {

    /**
     * 转换为菜单展示对象。
     *
     * @param entity 菜单实体
     * @return 菜单展示对象
     */
    public MenuVO toMenuVO(SysMenuEntity entity) {
        MenuVO vo = new MenuVO();
        vo.setKey(String.valueOf(entity.getId()));
        vo.setParentId(String.valueOf(DefaultValueUtils.defaultIfNull(entity.getParentId(), 0L)));
        vo.setMenuName(entity.getMenuName());
        vo.setMenuType(entity.getMenuType());
        vo.setPermission(entity.getPermission());
        vo.setRoutePath(entity.getRoutePath());
        vo.setSort(DefaultValueUtils.defaultIfNull(entity.getSort(), 0));
        vo.setStatus(entity.getStatus());
        return vo;
    }
}
