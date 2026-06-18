package com.hlw.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.CommonStatusEnum;
import com.hlw.common.core.enums.DeletedStatusEnum;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.domain.req.CreatePostReq;
import com.hlw.system.entity.SysPostEntity;
import com.hlw.system.mapper.SysPostMapper;
import com.hlw.system.service.converter.PostConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import com.hlw.system.domain.resp.PostResp;
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
    public PageResult<PostResp> listPosts(PageQuery query) {
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
        List<PostResp> records = result.getRecords().stream()
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
    public PostResp createPost(CreatePostReq request) {
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

    /**
     * 查询岗位详情。
     *
     * @param postId 岗位编号
     * @return 岗位展示对象
     */
    @Transactional(readOnly = true)
    public PostResp getPost(Long postId) {
        log.info("查询系统岗位详情，postId={}", postId);
        return postConverter.toPostVO(requireActivePost(postId));
    }

    /**
     * 更新岗位。
     *
     * @param postId 岗位编号
     * @param request 岗位更新请求
     * @return 更新后的岗位展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public PostResp updatePost(Long postId, CreatePostReq request) {
        log.info("更新系统岗位，postId={}，postName={}，postCode={}", postId, request.getPostName(), request.getPostCode());
        SysPostEntity entity = requireActivePost(postId);
        entity.setPostName(request.getPostName());
        entity.setPostCode(request.getPostCode());
        entity.setSort(DefaultValueUtils.defaultIfNull(request.getSort(), 0));
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), CommonStatusEnum.ENABLED.getStatus()));
        entity.setRemark(DefaultValueUtils.defaultIfBlank(request.getRemark(), ""));
        sysPostMapper.updateById(entity);
        return postConverter.toPostVO(entity);
    }

    /**
     * 删除岗位。
     *
     * @param postId 岗位编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId) {
        log.info("删除系统岗位，postId={}", postId);
        SysPostEntity entity = requireActivePost(postId);
        entity.setDeleted(DeletedStatusEnum.DELETED.getType());
        sysPostMapper.updateById(entity);
    }

    /**
     * 校验岗位处于可用状态。
     *
     * @param postId 岗位编号
     * @return 岗位实体
     */
    private SysPostEntity requireActivePost(Long postId) {
        return MybatisTenantHelpers.requireEntity(sysPostMapper.selectOne(
            MybatisTenantHelpers.notDeletedWrapper(SysPostEntity::getDeleted)
                .eq(SysPostEntity::getId, postId)
                .last("limit 1")), "岗位不存在");
    }
}
