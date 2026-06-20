package com.hlw.system.service.converter;

import com.hlw.system.entity.SysRoleEntity;
import com.hlw.system.domain.resp.RoleResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * 角色实体到展示对象的转换器。成员数量由调用方传入，避免转换器触发额外的 Mapper 查询。
 */
@Component
@RequiredArgsConstructor
public class RoleConverter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 转换为角色展示对象。
     *
     * @param entity 角色实体
     * @param memberCount 成员数量
     * @return 角色展示对象
     */
    public RoleResp toRoleVO(SysRoleEntity entity, Integer memberCount) {
        RoleResp vo = new RoleResp();
        vo.setId(entity.getId());
        vo.setRoleName(entity.getRoleName());
        vo.setRoleCode(entity.getRoleCode());
        vo.setOrderNum(entity.getOrderNum());
        vo.setDataScope(entity.getDataScope());
        vo.setMemberCount(memberCount);
        vo.setUpdatedAt(entity.getUpdateTime() == null ? "-" : entity.getUpdateTime().format(DATE_TIME_FORMATTER));
        vo.setStatus(entity.getStatus());
        vo.setIsDefault(entity.getIsDefault());
        vo.setRemark(entity.getRemark());
        return vo;
    }
}
