package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.dto.UpdateConfigRequest;
import com.hlw.system.entity.SysConfigEntity;
import com.hlw.system.mapper.SysConfigMapper;
import com.hlw.system.service.converter.ConfigConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import com.hlw.system.vo.ConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 参数配置聚合服务，负责系统配置项的查询与更新编排。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigService {
    /** 参数配置数据访问组件。 */
    private final SysConfigMapper sysConfigMapper;
    /** 参数配置展示对象转换器。 */
    private final ConfigConverter configConverter;

    /**
     * 分页查询参数配置列表，按主键升序排列。
     *
     * @param query 分页查询条件
     * @return 参数配置分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<ConfigVO> listConfigs(PageQuery query) {
        log.info("查询系统参数配置列表分页，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());

        Page<SysConfigEntity> page = query.toPage();
        LambdaQueryWrapper<SysConfigEntity> wrapper = MybatisTenantHelpers.notDeletedWrapper(SysConfigEntity::getDeleted);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(SysConfigEntity::getConfigKey, query.getKeyword());
        }
        wrapper.orderByAsc(SysConfigEntity::getId);

        Page<SysConfigEntity> result = sysConfigMapper.selectPage(page, wrapper);
        List<ConfigVO> records = result.getRecords().stream()
            .map(configConverter::toConfigVO)
            .toList();
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 更新参数配置。
     *
     * @param id 配置编号
     * @param request 更新参数请求
     * @return 更新后的参数展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public ConfigVO updateConfig(Long id, UpdateConfigRequest request) {
        log.info("更新系统配置，configId={}", id);
        SysConfigEntity entity = requireActiveConfig(id);
        entity.setConfigValue(request.getConfigValue());
        entity.setRemark(DefaultValueUtils.defaultIfBlank(request.getRemark(), ""));
        sysConfigMapper.updateById(entity);
        return configConverter.toConfigVO(entity);
    }

    /**
     * 校验参数配置处于可用状态。
     *
     * @param configId 配置编号
     * @return 配置实体
     */
    private SysConfigEntity requireActiveConfig(Long configId) {
        return MybatisTenantHelpers.requireEntity(sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfigEntity>()
            .eq(SysConfigEntity::getDeleted, 0)
            .eq(SysConfigEntity::getId, configId)
            .last("limit 1")), "系统配置不存在");
    }
}
