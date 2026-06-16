package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.CommonStatusEnum;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.dto.CreateDictRequest;
import com.hlw.system.entity.SysDictEntity;
import com.hlw.system.mapper.SysDictMapper;
import com.hlw.system.service.converter.DictConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import com.hlw.system.vo.DictVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 字典聚合服务，负责字典项的查询与创建编排。
 */
@Service
@RequiredArgsConstructor
public class DictService {
    private static final Logger log = LoggerFactory.getLogger(DictService.class);

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
    public PageResult<DictVO> listDicts(PageQuery query) {
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
        List<DictVO> records = result.getRecords().stream()
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
    public DictVO createDict(CreateDictRequest request) {
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
}
