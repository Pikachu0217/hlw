package com.hlw.system.service.converter;

import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.entity.SysMenuEntity;
import com.hlw.system.domain.resp.MenuResp;
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
    public MenuResp toMenuVO(SysMenuEntity entity) {
        MenuResp vo = new MenuResp();
        vo.setId(entity.getId());
        vo.setParentId(DefaultValueUtils.defaultIfNull(entity.getParentId(), 0L));
        vo.setMenuName(entity.getMenuName());
        vo.setMenuType(entity.getMenuType());
        vo.setPerms(entity.getPerms());
        vo.setPath(entity.getPath());
        vo.setComponent(entity.getComponent());
        vo.setIsFrame(entity.getIsFrame());
        vo.setVisible(entity.getVisible());
        vo.setOrderNum(DefaultValueUtils.defaultIfNull(entity.getOrderNum(), 0));
        vo.setIcon(entity.getIcon());
        vo.setRemark(entity.getRemark());
        vo.setStatus(entity.getStatus());
        vo.setIsDefault(entity.getIsDefault());
        return vo;
    }
}
