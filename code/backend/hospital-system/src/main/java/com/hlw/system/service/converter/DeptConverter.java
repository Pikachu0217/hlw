package com.hlw.system.service.converter;

import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.entity.SysDeptEntity;
import com.hlw.system.domain.resp.DeptResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 部门实体到展示对象的转换器。
 */
@Component
@RequiredArgsConstructor
public class DeptConverter {

    /**
     * 转换为部门展示对象。
     *
     * @param entity 部门实体
     * @return 部门展示对象
     */
    public DeptResp toDeptVO(SysDeptEntity entity) {
        DeptResp vo = new DeptResp();
        vo.setId(entity.getId());
        vo.setParentId(DefaultValueUtils.defaultIfNull(entity.getParentId(), 0L));
        vo.setDeptName(entity.getDeptName());
        vo.setAncestors(DefaultValueUtils.defaultIfBlank(entity.getAncestors(), "0"));
        vo.setOrderNum(DefaultValueUtils.defaultIfNull(entity.getOrderNum(), 0));
        vo.setLeader(entity.getLeader());
        vo.setPhone(entity.getPhone());
        vo.setEmail(entity.getEmail());
        vo.setStatus(entity.getStatus());
        vo.setIsDefault(entity.getIsDefault());
        return vo;
    }
}
