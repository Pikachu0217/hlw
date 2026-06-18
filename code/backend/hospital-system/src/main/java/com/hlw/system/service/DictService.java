package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.CommonStatusEnum;
import com.hlw.common.core.enums.DeletedStatusEnum;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.domain.req.CreateDictReq;
import com.hlw.system.entity.SysDictEntity;
import com.hlw.system.mapper.SysDictMapper;
import com.hlw.system.service.converter.DictConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import com.hlw.system.domain.resp.DictResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 字典聚合服务，负责字典项的查询与创建编排。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DictService {
    /** 字典数据访问组件。 */
    private final SysDictMapper sysDictMapper;
    /** 字典展示对象转换器。 */
    private final DictConverter dictConverter;

    /**
     * 分页查询字典列表，按类型、排序、主键升序排列。
     *
     * @param query 分页查询条件
     * @return 字典分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<DictResp> listDicts(PageQuery query) {
        log.info("查询系统字典列表分页，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());

        Page<SysDictEntity> page = query.toPage();
        LambdaQueryWrapper<SysDictEntity> wrapper = MybatisTenantHelpers.notDeletedWrapper(SysDictEntity::getDeleted);
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword();
            wrapper.and(w -> w.like(SysDictEntity::getDictLabel, keyword).or().like(SysDictEntity::getDictType, keyword));
        }
        wrapper.orderByAsc(SysDictEntity::getDictType)
            .orderByAsc(SysDictEntity::getSort)
            .orderByAsc(SysDictEntity::getId);

        Page<SysDictEntity> result = sysDictMapper.selectPage(page, wrapper);
        List<DictResp> records = result.getRecords().stream()
            .map(dictConverter::toDictVO)
            .toList();
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 创建字典项。
     *
     * @param request 创建字典请求
     * @return 新建字典展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public DictResp createDict(CreateDictReq request) {
        log.info("创建字典项，dictType={}，dictLabel={}", request.getDictType(), request.getDictLabel());
        SysDictEntity entity = new SysDictEntity();
        entity.setDictType(request.getDictType());
        entity.setDictLabel(request.getDictLabel());
        entity.setDictValue(request.getDictValue());
        entity.setSort(DefaultValueUtils.defaultIfNull(request.getSort(), 0));
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), CommonStatusEnum.ENABLED.getStatus()));
        entity.setRemark(DefaultValueUtils.defaultIfBlank(request.getRemark(), ""));
        entity.setDeleted(0);
        sysDictMapper.insert(entity);
        return dictConverter.toDictVO(entity);
    }

    /**
     * 查询字典详情。
     *
     * @param dictId 字典编号
     * @return 字典展示对象
     */
    @Transactional(readOnly = true)
    public DictResp getDict(Long dictId) {
        log.info("查询系统字典详情，dictId={}", dictId);
        return dictConverter.toDictVO(requireActiveDict(dictId));
    }

    /**
     * 更新字典项。
     *
     * @param dictId 字典编号
     * @param request 字典更新请求
     * @return 更新后的字典展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public DictResp updateDict(Long dictId, CreateDictReq request) {
        log.info("更新系统字典项，dictId={}，dictType={}，dictLabel={}", dictId, request.getDictType(), request.getDictLabel());
        SysDictEntity entity = requireActiveDict(dictId);
        entity.setDictType(request.getDictType());
        entity.setDictLabel(request.getDictLabel());
        entity.setDictValue(request.getDictValue());
        entity.setSort(DefaultValueUtils.defaultIfNull(request.getSort(), 0));
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), CommonStatusEnum.ENABLED.getStatus()));
        entity.setRemark(DefaultValueUtils.defaultIfBlank(request.getRemark(), ""));
        sysDictMapper.updateById(entity);
        return dictConverter.toDictVO(entity);
    }

    /**
     * 删除字典项。
     *
     * @param dictId 字典编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDict(Long dictId) {
        log.info("删除系统字典项，dictId={}", dictId);
        SysDictEntity entity = requireActiveDict(dictId);
        entity.setDeleted(DeletedStatusEnum.DELETED.getType());
        sysDictMapper.updateById(entity);
    }

    /**
     * 校验字典项处于可用状态。
     *
     * @param dictId 字典编号
     * @return 字典实体
     */
    private SysDictEntity requireActiveDict(Long dictId) {
        return MybatisTenantHelpers.requireEntity(sysDictMapper.selectOne(
            MybatisTenantHelpers.notDeletedWrapper(SysDictEntity::getDeleted)
                .eq(SysDictEntity::getId, dictId)
                .last("limit 1")), "字典项不存在");
    }
}
