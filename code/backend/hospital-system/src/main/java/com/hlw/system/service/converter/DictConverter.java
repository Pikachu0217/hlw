package com.hlw.system.service.converter;

import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.domain.resp.DictResp;
import com.hlw.system.entity.SysDictDataEntity;
import com.hlw.system.entity.SysDictTypeEntity;
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
     * @param entity 字典数据实体
     * @param typeEntity 字典类型实体
     * @return 字典展示对象
     */
    public DictResp toDictVO(SysDictDataEntity entity, SysDictTypeEntity typeEntity) {
        DictResp vo = new DictResp();
        vo.setId(entity.getId());
        vo.setDictName(typeEntity == null ? "" : typeEntity.getDictName());
        vo.setDictType(entity.getDictType());
        vo.setDictLabel(entity.getDictLabel());
        vo.setDictValue(entity.getDictValue());
        vo.setDictSort(DefaultValueUtils.defaultIfNull(entity.getDictSort(), 0));
        vo.setRemark(entity.getRemark());
        return vo;
    }
}
