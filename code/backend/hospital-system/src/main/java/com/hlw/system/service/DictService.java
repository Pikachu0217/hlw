package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.domain.req.CreateDictReq;
import com.hlw.system.domain.resp.DictResp;
import com.hlw.system.entity.SysDictDataEntity;
import com.hlw.system.entity.SysDictTypeEntity;
import com.hlw.system.mapper.SysDictDataMapper;
import com.hlw.system.mapper.SysDictTypeMapper;
import com.hlw.system.service.converter.DictConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典聚合服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DictService {
    /** 字典类型数据访问组件。 */
    private final SysDictTypeMapper sysDictTypeMapper;
    /** 字典数据访问组件。 */
    private final SysDictDataMapper sysDictDataMapper;
    /** 字典展示对象转换器。 */
    private final DictConverter dictConverter;

    /**
     * 分页查询字典数据列表。
     *
     * @param query 分页查询条件
     * @return 字典分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<DictResp> listDicts(PageQuery query) {
        log.info("查询字典数据列表，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());
        LambdaQueryWrapper<SysDictDataEntity> wrapper = new LambdaQueryWrapper<SysDictDataEntity>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(item -> item.like(SysDictDataEntity::getDictType, query.getKeyword())
                .or()
                .like(SysDictDataEntity::getDictLabel, query.getKeyword()));
        }
        wrapper.orderByAsc(SysDictDataEntity::getDictType).orderByAsc(SysDictDataEntity::getDictSort).orderByAsc(SysDictDataEntity::getId);
        Page<SysDictDataEntity> page = sysDictDataMapper.selectPage(query.toPage(), wrapper);
        Map<String, SysDictTypeEntity> typeMap = loadTypeMap(page.getRecords().stream().map(SysDictDataEntity::getDictType).toList());
        List<DictResp> records = page.getRecords().stream()
            .map(data -> dictConverter.toDictVO(data, typeMap.get(data.getDictType())))
            .toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 创建字典数据。
     *
     * @param request 字典创建请求
     * @return 字典展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public DictResp createDict(CreateDictReq request) {
        log.info("创建字典数据，dictType={}，dictValue={}", request.getDictType(), request.getDictValue());
        SysDictTypeEntity typeEntity = ensureDictType(request);
        SysDictDataEntity entity = new SysDictDataEntity();
        entity.setDictType(request.getDictType());
        entity.setDictLabel(request.getDictLabel());
        entity.setDictValue(request.getDictValue());
        entity.setDictSort(DefaultValueUtils.defaultIfNull(request.getDictSort(), 0));
        entity.setRemark(request.getRemark());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        sysDictDataMapper.insert(entity);
        return dictConverter.toDictVO(entity, typeEntity);
    }

    /**
     * 查询字典数据详情。
     *
     * @param id 字典数据编号
     * @return 字典展示对象
     */
    @Transactional(readOnly = true)
    public DictResp getDict(Long id) {
        log.info("查询字典数据详情，id={}", id);
        SysDictDataEntity entity = requireDictData(id);
        SysDictTypeEntity typeEntity = findDictType(entity.getDictType());
        return dictConverter.toDictVO(entity, typeEntity);
    }

    /**
     * 更新字典数据。
     *
     * @param id 字典数据编号
     * @param request 字典更新请求
     * @return 字典展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public DictResp updateDict(Long id, CreateDictReq request) {
        log.info("更新字典数据，id={}，dictType={}，dictValue={}", id, request.getDictType(), request.getDictValue());
        SysDictTypeEntity typeEntity = ensureDictType(request);
        SysDictDataEntity entity = requireDictData(id);
        entity.setDictType(request.getDictType());
        entity.setDictLabel(request.getDictLabel());
        entity.setDictValue(request.getDictValue());
        entity.setDictSort(DefaultValueUtils.defaultIfNull(request.getDictSort(), 0));
        entity.setRemark(request.getRemark());
        entity.setUpdateTime(LocalDateTime.now());
        sysDictDataMapper.updateById(entity);
        return dictConverter.toDictVO(entity, typeEntity);
    }

    /**
     * 删除字典数据。
     *
     * @param id 字典数据编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDict(Long id) {
        log.info("删除字典数据，id={}", id);
        requireDictData(id);
        sysDictDataMapper.deleteById(id);
    }

    /**
     * 确保字典类型存在。
     *
     * @param request 字典请求
     * @return 字典类型实体
     */
    private SysDictTypeEntity ensureDictType(CreateDictReq request) {
        SysDictTypeEntity existed = findDictType(request.getDictType());
        if (existed != null) {
            if (StringUtils.hasText(request.getDictName()) && !request.getDictName().equals(existed.getDictName())) {
                existed.setDictName(request.getDictName());
                existed.setUpdateTime(LocalDateTime.now());
                sysDictTypeMapper.updateById(existed);
            }
            return existed;
        }
        SysDictTypeEntity entity = new SysDictTypeEntity();
        entity.setDictType(request.getDictType());
        entity.setDictName(DefaultValueUtils.defaultIfBlank(request.getDictName(), request.getDictType()));
        entity.setRemark(request.getRemark());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        sysDictTypeMapper.insert(entity);
        return entity;
    }

    /**
     * 查询字典类型。
     *
     * @param dictType 字典类型
     * @return 字典类型实体
     */
    private SysDictTypeEntity findDictType(String dictType) {
        return sysDictTypeMapper.selectOne(new LambdaQueryWrapper<SysDictTypeEntity>()
            .eq(SysDictTypeEntity::getDictType, dictType)
            .last("limit 1"));
    }

    /**
     * 批量加载字典类型映射。
     *
     * @param dictTypes 字典类型列表
     * @return 字典类型映射
     */
    private Map<String, SysDictTypeEntity> loadTypeMap(List<String> dictTypes) {
        if (dictTypes.isEmpty()) {
            return Map.of();
        }
        return sysDictTypeMapper.selectList(new LambdaQueryWrapper<SysDictTypeEntity>()
                .in(SysDictTypeEntity::getDictType, dictTypes)).stream()
            .collect(Collectors.toMap(SysDictTypeEntity::getDictType, item -> item, (left, right) -> left));
    }

    /**
     * 校验字典数据存在。
     *
     * @param id 字典数据编号
     * @return 字典数据实体
     */
    private SysDictDataEntity requireDictData(Long id) {
        return MybatisTenantHelpers.requireEntity(sysDictDataMapper.selectOne(
            new LambdaQueryWrapper<SysDictDataEntity>()
                .eq(SysDictDataEntity::getId, id)
                .last("limit 1")), "字典数据不存在");
    }
}
