package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.CommonStatusEnum;
import com.hlw.common.core.enums.DeletedStatusEnum;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.domain.req.CreateConfigReq;
import com.hlw.system.domain.req.UpdateConfigReq;
import com.hlw.system.entity.SysConfigEntity;
import com.hlw.system.mapper.SysConfigMapper;
import com.hlw.system.service.converter.ConfigConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import com.hlw.system.domain.resp.ConfigResp;
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
    public PageResult<ConfigResp> listConfigs(PageQuery query) {
        log.info("查询系统参数配置列表分页，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());

        Page<SysConfigEntity> page = query.toPage();
        LambdaQueryWrapper<SysConfigEntity> wrapper = MybatisTenantHelpers.notDeletedWrapper(SysConfigEntity::getDeleted);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(SysConfigEntity::getConfigKey, query.getKeyword());
        }
        wrapper.orderByAsc(SysConfigEntity::getId);

        Page<SysConfigEntity> result = sysConfigMapper.selectPage(page, wrapper);
        List<ConfigResp> records = result.getRecords().stream()
            .map(configConverter::toConfigVO)
            .toList();
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 创建参数配置。
     *
     * @param request 创建参数请求
     * @return 新建参数展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public ConfigResp createConfig(CreateConfigReq request) {
        log.info("创建系统配置，configKey={}，configType={}", request.getConfigKey(), request.getConfigType());
        SysConfigEntity entity = new SysConfigEntity();
        entity.setConfigKey(request.getConfigKey());
        entity.setConfigValue(request.getConfigValue());
        entity.setConfigType(DefaultValueUtils.defaultIfBlank(request.getConfigType(), "业务参数"));
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), CommonStatusEnum.ENABLED.getStatus()));
        entity.setRemark(DefaultValueUtils.defaultIfBlank(request.getRemark(), ""));
        entity.setDeleted(0);
        sysConfigMapper.insert(entity);
        return configConverter.toConfigVO(entity);
    }

    /**
     * 查询参数配置详情。
     *
     * @param configId 配置编号
     * @return 参数配置展示对象
     */
    @Transactional(readOnly = true)
    public ConfigResp getConfig(Long configId) {
        log.info("查询系统配置详情，configId={}", configId);
        return configConverter.toConfigVO(requireActiveConfig(configId));
    }

    /**
     * 更新参数配置。
     *
     * @param id 配置编号
     * @param request 更新参数请求
     * @return 更新后的参数展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public ConfigResp updateConfig(Long id, UpdateConfigReq request) {
        log.info("更新系统配置，configId={}", id);
        SysConfigEntity entity = requireActiveConfig(id);
        entity.setConfigValue(request.getConfigValue());
        entity.setRemark(DefaultValueUtils.defaultIfBlank(request.getRemark(), ""));
        sysConfigMapper.updateById(entity);
        return configConverter.toConfigVO(entity);
    }

    /**
     * 删除参数配置。
     *
     * @param configId 配置编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long configId) {
        log.info("删除系统配置，configId={}", configId);
        SysConfigEntity entity = requireActiveConfig(configId);
        entity.setDeleted(DeletedStatusEnum.DELETED.getType());
        sysConfigMapper.updateById(entity);
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
