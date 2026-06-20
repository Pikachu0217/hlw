package com.hlw.system.service.converter;

import com.hlw.system.entity.SysTenantPackageEntity;
import com.hlw.system.entity.SysTenantEntity;
import com.hlw.system.domain.resp.TenantResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * 租户实体到展示对象的转换器，承接原 god service 中的内联 VO 构造逻辑。
 */
@Component
@RequiredArgsConstructor
public class TenantConverter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 转换为租户展示对象。
     *
     * @param entity 租户实体
     * @param packageEntity 租户套餐实体
     * @return 租户展示对象
     */
    public TenantResp toTenantVO(SysTenantEntity entity, SysTenantPackageEntity packageEntity) {
        TenantResp vo = new TenantResp();
        vo.setId(entity.getId());
        vo.setTenantId(entity.getTenantId());
        vo.setContactUserName(entity.getContactUserName());
        vo.setContactPhone(entity.getContactPhone());
        vo.setCompanyName(entity.getCompanyName());
        vo.setLicenseNumber(entity.getLicenseNumber());
        vo.setAddress(entity.getAddress());
        vo.setIntro(entity.getIntro());
        vo.setDomain(entity.getDomain());
        vo.setRemark(entity.getRemark());
        vo.setPackageId(entity.getPackageId());
        vo.setPackageName(packageEntity == null ? "" : packageEntity.getPackageName());
        vo.setExpireTime(entity.getExpireTime() == null ? "" : entity.getExpireTime().format(DATE_TIME_FORMATTER));
        vo.setAccountCount(entity.getAccountCount());
        vo.setStatus(entity.getStatus());
        vo.setIsDefault(entity.getIsDefault());
        return vo;
    }
}
