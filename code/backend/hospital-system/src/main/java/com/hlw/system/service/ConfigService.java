package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.DeletedStatusEnum;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.domain.req.CreateConfigReq;
import com.hlw.system.domain.req.UpdateConfigReq;
import com.hlw.system.domain.resp.ConfigResp;
import com.hlw.system.entity.SysConfigEntity;
import com.hlw.system.mapper.SysConfigMapper;
import com.hlw.system.service.converter.ConfigConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 参数配置聚合服务。
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
     * 分页查询参数配置列表。
     *
     * @param query 分页查询条件
     * @return 参数配置分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<ConfigResp> listConfigs(PageQuery query) {
        log.info("查询参数配置列表，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());
        LambdaQueryWrapper<SysConfigEntity> wrapper = MybatisTenantHelpers.notDeletedWrapper(SysConfigEntity::getDeleted);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(item -> item.like(SysConfigEntity::getConfigName, query.getKeyword())
                .or()
                .like(SysConfigEntity::getConfigKey, query.getKeyword()));
        }
        wrapper.orderByAsc(SysConfigEntity::getId);
        Page<SysConfigEntity> page = sysConfigMapper.selectPage(query.toPage(), wrapper);
        List<ConfigResp> records = page.getRecords().stream().map(configConverter::toConfigVO).toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 创建参数配置。
     *
     * @param request 参数配置创建请求
     * @return 参数配置展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public ConfigResp createConfig(CreateConfigReq request) {
        log.info("创建参数配置，configKey={}", request.getConfigKey());
        SysConfigEntity entity = new SysConfigEntity();
        entity.setConfigName(request.getConfigName());
        entity.setConfigKey(request.getConfigKey());
        entity.setConfigValue(request.getConfigValue());
        entity.setRemark(request.getRemark());
        entity.setDeleted(DeletedStatusEnum.NOT_DELETED.getType());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        sysConfigMapper.insert(entity);
        return configConverter.toConfigVO(entity);
    }

    /**
     * 查询参数配置详情。
     *
     * @param id 参数配置编号
     * @return 参数配置展示对象
     */
    @Transactional(readOnly = true)
    public ConfigResp getConfig(Long id) {
        log.info("查询参数配置详情，id={}", id);
        return configConverter.toConfigVO(requireConfig(id));
    }

    /**
     * 更新参数配置。
     *
     * @param id 参数配置编号
     * @param request 参数配置更新请求
     * @return 参数配置展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public ConfigResp updateConfig(Long id, UpdateConfigReq request) {
        log.info("更新参数配置，id={}", id);
        SysConfigEntity entity = requireConfig(id);
        entity.setConfigValue(request.getConfigValue());
        entity.setRemark(DefaultValueUtils.defaultIfBlank(request.getRemark(), entity.getRemark()));
        entity.setUpdateTime(LocalDateTime.now());
        sysConfigMapper.updateById(entity);
        return configConverter.toConfigVO(entity);
    }

    /**
     * 删除参数配置。
     *
     * @param id 参数配置编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long id) {
        log.info("删除参数配置，id={}", id);
        SysConfigEntity entity = requireConfig(id);
        entity.setDeleted(DeletedStatusEnum.DELETED.getType());
        entity.setUpdateTime(LocalDateTime.now());
        sysConfigMapper.updateById(entity);
    }

    /**
     * 校验参数配置存在。
     *
     * @param id 参数配置编号
     * @return 参数配置实体
     */
    private SysConfigEntity requireConfig(Long id) {
        return MybatisTenantHelpers.requireEntity(sysConfigMapper.selectOne(
            MybatisTenantHelpers.notDeletedWrapper(SysConfigEntity::getDeleted)
                .eq(SysConfigEntity::getId, id)
                .last("limit 1")), "参数配置不存在");
    }
}
