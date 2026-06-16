package com.hlw.system.service.converter;

import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.entity.SysDictEntity;
import com.hlw.system.vo.DictVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 字典实体到展示对象的转换器。
 */
@Component
@RequiredArgsConstructor
public class DictConverter {

    /**
     * 转换为字典展示对象。
     *
     * @param entity 字典实体
     * @return 字典展示对象
     */
    public DictVO toDictVO(SysDictEntity entity) {
        DictVO vo = new DictVO();
        vo.setKey(String.valueOf(entity.getId()));
        vo.setDictType(entity.getDictType());
        vo.setDictLabel(entity.getDictLabel());
        vo.setDictValue(entity.getDictValue());
        vo.setSort(DefaultValueUtils.defaultIfNull(entity.getSort(), 0));
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }
}
