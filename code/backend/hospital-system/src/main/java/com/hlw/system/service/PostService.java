package com.hlw.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.CommonStatusEnum;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.dto.CreatePostRequest;
import com.hlw.system.entity.SysPostEntity;
import com.hlw.system.mapper.SysPostMapper;
import com.hlw.system.service.converter.PostConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import com.hlw.system.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 岗位聚合服务，负责岗位的查询与创建编排。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    /** 岗位数据访问组件。 */
    private final SysPostMapper sysPostMapper;
    /** 岗位展示对象转换器。 */
    private final PostConverter postConverter;

    /**
     * 分页查询岗位列表，按排序、主键升序排列。
     *
     * @param query 分页查询条件
     * @return 岗位分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<PostVO> listPosts(PageQuery query) {
        log.info("查询系统岗位列表分页，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());

        Page<SysPostEntity> page = query.toPage();
        LambdaQueryWrapper<SysPostEntity> wrapper = MybatisTenantHelpers.notDeletedWrapper(SysPostEntity::getDeleted);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(SysPostEntity::getPostName, query.getKeyword());
        }
        wrapper.orderByAsc(SysPostEntity::getSort)
            .orderByAsc(SysPostEntity::getId);

        Page<SysPostEntity> result = sysPostMapper.selectPage(page, wrapper);
        List<PostVO> records = result.getRecords().stream()
            .map(postConverter::toPostVO)
            .toList();
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 创建岗位。
     *
     * @param request 创建岗位请求
     * @return 新建岗位展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public PostVO createPost(CreatePostRequest request) {
        log.info("创建岗位，postName={}，postCode={}", request.getPostName(), request.getPostCode());
        SysPostEntity entity = new SysPostEntity();
        entity.setPostName(request.getPostName());
        entity.setPostCode(request.getPostCode());
        entity.setSort(DefaultValueUtils.defaultIfNull(request.getSort(), 0));
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), CommonStatusEnum.ENABLED.getStatus()));
        entity.setRemark(DefaultValueUtils.defaultIfBlank(request.getRemark(), ""));
        entity.setDeleted(0);
        sysPostMapper.insert(entity);
        return postConverter.toPostVO(entity);
    }
}
