package com.hlw.system.service.converter;

import com.hlw.system.entity.SysUserEntity;
import com.hlw.system.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用户实体到展示对象的转换器。岗位名称由调用方传入，避免转换器触发额外的 Mapper 查询。
 */
@Component
@RequiredArgsConstructor
public class UserConverter {

    /**
     * 转换为用户展示对象，按 listUsers 输出形状统一字段。
     *
     * @param entity 用户实体
     * @param postName 岗位名称（无岗位时调用方传入 "-"）
     * @return 用户展示对象
     */
    public UserVO toUserVO(SysUserEntity entity, String postName) {
        UserVO vo = new UserVO();
        vo.setKey(String.valueOf(entity.getId()));
        vo.setUsername(entity.getUsername());
        vo.setDeptName(entity.getDeptName());
        vo.setRoleName(entity.getRoleName());
        vo.setPhone(entity.getPhone());
        vo.setLastLogin(entity.getLastLogin());
        vo.setStatus(entity.getStatus());
        vo.setPostName(postName);
        return vo;
    }
}
