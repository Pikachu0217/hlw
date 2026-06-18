package com.hlw.auth.service.converter;

import com.hlw.auth.domain.resp.LoginRecordResp;
import com.hlw.auth.entity.AuthLoginRecordEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * 登录记录实体到展示对象的转换器。
 */
@Component
@RequiredArgsConstructor
public class LoginRecordConverter {
    /** 日期时间格式化器。 */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 转换为登录记录展示对象。
     *
     * @param entity 登录记录实体
     * @return 登录记录展示对象
     */
    public LoginRecordResp toResp(AuthLoginRecordEntity entity) {
        LoginRecordResp resp = new LoginRecordResp();
        resp.setKey(String.valueOf(entity.getId()));
        resp.setTenantId(entity.getTenantId());
        resp.setUserId(entity.getUserId());
        resp.setUsername(entity.getUsername());
        resp.setUserType(entity.getUserType());
        resp.setLoginStatus(entity.getLoginStatus());
        resp.setFailureReason(entity.getFailureReason());
        resp.setTokenDigest(entity.getTokenDigest());
        resp.setLoginTime(entity.getLoginTime() == null ? "" : entity.getLoginTime().format(DATE_TIME_FORMATTER));
        resp.setLogoutTime(entity.getLogoutTime() == null ? "" : entity.getLogoutTime().format(DATE_TIME_FORMATTER));
        resp.setClientIp(entity.getClientIp());
        resp.setUserAgent(entity.getUserAgent());
        return resp;
    }
}
