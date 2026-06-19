package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.system.domain.resp.OperatorLogResp;
import com.hlw.system.entity.SysOperatorLogEntity;
import com.hlw.system.mapper.SysOperatorLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 操作日志聚合服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OperatorLogService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 操作日志数据访问组件。 */
    private final SysOperatorLogMapper sysOperatorLogMapper;

    /**
     * 分页查询操作日志。
     *
     * @param query 分页查询条件
     * @return 操作日志分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<OperatorLogResp> listOperatorLogs(PageQuery query) {
        log.info("查询操作日志列表，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());
        LambdaQueryWrapper<SysOperatorLogEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(item -> item.like(SysOperatorLogEntity::getTitle, query.getKeyword())
                .or()
                .like(SysOperatorLogEntity::getOperatorName, query.getKeyword()));
        }
        wrapper.orderByDesc(SysOperatorLogEntity::getOperatorTime).orderByDesc(SysOperatorLogEntity::getId);
        Page<SysOperatorLogEntity> page = sysOperatorLogMapper.selectPage(query.toPage(), wrapper);
        List<OperatorLogResp> records = page.getRecords().stream().map(this::toResp).toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 转换操作日志展示对象。
     *
     * @param entity 操作日志实体
     * @return 操作日志展示对象
     */
    private OperatorLogResp toResp(SysOperatorLogEntity entity) {
        OperatorLogResp resp = new OperatorLogResp();
        resp.setKey(String.valueOf(entity.getId()));
        resp.setTenantId(entity.getTenantId());
        resp.setTitle(entity.getTitle());
        resp.setBusinessType(entity.getBusinessType());
        resp.setMethod(entity.getMethod());
        resp.setRequestMethod(entity.getRequestMethod());
        resp.setOperatorType(entity.getOperatorType());
        resp.setOperatorName(entity.getOperatorName());
        resp.setDeptName(entity.getDeptName());
        resp.setOperatorUrl(entity.getOperatorUrl());
        resp.setOperatorIp(entity.getOperatorIp());
        resp.setStatus(entity.getStatus());
        resp.setErrorMsg(entity.getErrorMsg());
        resp.setOperatorTime(entity.getOperatorTime() == null ? "" : entity.getOperatorTime().format(DATE_TIME_FORMATTER));
        resp.setCostTime(entity.getCostTime());
        return resp;
    }
}
