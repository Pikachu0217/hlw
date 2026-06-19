package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.system.domain.resp.LoginInfoResp;
import com.hlw.system.entity.SysLoginInfoEntity;
import com.hlw.system.mapper.SysLoginInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 登录日志聚合服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginInfoService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 登录日志数据访问组件。 */
    private final SysLoginInfoMapper sysLoginInfoMapper;

    /**
     * 分页查询登录日志。
     *
     * @param query 分页查询条件
     * @return 登录日志分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<LoginInfoResp> listLoginInfos(PageQuery query) {
        log.info("查询登录日志列表，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());
        LambdaQueryWrapper<SysLoginInfoEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(SysLoginInfoEntity::getUserName, query.getKeyword());
        }
        wrapper.orderByDesc(SysLoginInfoEntity::getLoginTime).orderByDesc(SysLoginInfoEntity::getId);
        Page<SysLoginInfoEntity> page = sysLoginInfoMapper.selectPage(query.toPage(), wrapper);
        List<LoginInfoResp> records = page.getRecords().stream().map(this::toResp).toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 转换登录日志展示对象。
     *
     * @param entity 登录日志实体
     * @return 登录日志展示对象
     */
    private LoginInfoResp toResp(SysLoginInfoEntity entity) {
        LoginInfoResp resp = new LoginInfoResp();
        resp.setKey(String.valueOf(entity.getId()));
        resp.setTenantId(entity.getTenantId());
        resp.setUserName(entity.getUserName());
        resp.setClientKey(entity.getClientKey());
        resp.setDeviceType(entity.getDeviceType());
        resp.setIpaddr(entity.getIpaddr());
        resp.setLoginLocation(entity.getLoginLocation());
        resp.setBrowser(entity.getBrowser());
        resp.setOs(entity.getOs());
        resp.setStatus(entity.getStatus());
        resp.setMsg(entity.getMsg());
        resp.setLoginTime(entity.getLoginTime() == null ? "" : entity.getLoginTime().format(DATE_TIME_FORMATTER));
        return resp;
    }
}
