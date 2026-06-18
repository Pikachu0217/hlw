package com.hlw.system.service.converter;

import com.hlw.system.entity.SysConfigEntity;
import com.hlw.system.domain.resp.ConfigResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 参数配置实体到展示对象的转换器。
 */
@Component
@RequiredArgsConstructor
public class ConfigConverter {

    /**
     * 转换为参数配置展示对象。
     *
     * @param entity 参数配置实体
     * @return 参数配置展示对象
     */
    public ConfigResp toConfigVO(SysConfigEntity entity) {
        ConfigResp vo = new ConfigResp();
        vo.setKey(String.valueOf(entity.getId()));
        vo.setConfigKey(entity.getConfigKey());
        vo.setConfigValue(entity.getConfigValue());
        vo.setConfigType(entity.getConfigType());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }
}
