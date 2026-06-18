package com.hlw.system.service.converter;

import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.entity.SysPostEntity;
import com.hlw.system.domain.resp.PostResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 岗位实体到展示对象的转换器。
 */
@Component
@RequiredArgsConstructor
public class PostConverter {

    /**
     * 转换为岗位展示对象。
     *
     * @param entity 岗位实体
     * @return 岗位展示对象
     */
    public PostResp toPostVO(SysPostEntity entity) {
        PostResp vo = new PostResp();
        vo.setKey(String.valueOf(entity.getId()));
        vo.setPostName(entity.getPostName());
        vo.setPostCode(entity.getPostCode());
        vo.setSort(DefaultValueUtils.defaultIfNull(entity.getSort(), 0));
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }
}
