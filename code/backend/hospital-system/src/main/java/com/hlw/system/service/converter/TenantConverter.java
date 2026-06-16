package com.hlw.system.service.converter;

import com.hlw.system.entity.SysTenantEntity;
import com.hlw.system.vo.TenantVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * 租户实体到展示对象的转换器，承接原 god service 中的内联 VO 构造逻辑。
 */
@Component
@RequiredArgsConstructor
public class TenantConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 转换为租户展示对象。
     *
     * @param entity 租户实体
     * @return 租户展示对象
     */
    public TenantVO toTenantVO(SysTenantEntity entity) {
        TenantVO vo = new TenantVO();
        vo.setKey(String.valueOf(entity.getId()));
        vo.setTenantId(entity.getTenantId());
        vo.setTenantName(entity.getTenantName());
        vo.setPackageName(entity.getPackageName());
        vo.setAdminName(entity.getAdminName());
        vo.setExpireAt(entity.getExpireAt() == null ? "" : entity.getExpireAt().format(DATE_FORMATTER));
        vo.setStatus(entity.getStatus());
        return vo;
    }
}
