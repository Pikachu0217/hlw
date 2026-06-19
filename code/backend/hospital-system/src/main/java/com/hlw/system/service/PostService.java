package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.DeletedStatusEnum;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.domain.req.CreatePostReq;
import com.hlw.system.domain.resp.PostResp;
import com.hlw.system.entity.SysPostEntity;
import com.hlw.system.mapper.SysPostMapper;
import com.hlw.system.service.converter.PostConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 岗位聚合服务。
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
     * 分页查询岗位列表。
     *
     * @param query 分页查询条件
     * @return 岗位分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<PostResp> listPosts(PageQuery query) {
        log.info("查询岗位列表，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());
        LambdaQueryWrapper<SysPostEntity> wrapper = MybatisTenantHelpers.notDeletedWrapper(SysPostEntity::getDeleted);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(item -> item.like(SysPostEntity::getPostName, query.getKeyword())
                .or()
                .like(SysPostEntity::getPostCode, query.getKeyword()));
        }
        wrapper.orderByAsc(SysPostEntity::getOrderNum).orderByAsc(SysPostEntity::getId);
        Page<SysPostEntity> page = sysPostMapper.selectPage(query.toPage(), wrapper);
        List<PostResp> records = page.getRecords().stream().map(postConverter::toPostVO).toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 创建岗位。
     *
     * @param request 岗位创建请求
     * @return 岗位展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public PostResp createPost(CreatePostReq request) {
        log.info("创建岗位，postCode={}，postName={}", request.getPostCode(), request.getPostName());
        SysPostEntity entity = new SysPostEntity();
        fillPost(entity, request);
        entity.setDeleted(DeletedStatusEnum.NOT_DELETED.getType());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        sysPostMapper.insert(entity);
        return postConverter.toPostVO(entity);
    }

    /**
     * 查询岗位详情。
     *
     * @param id 岗位编号
     * @return 岗位展示对象
     */
    @Transactional(readOnly = true)
    public PostResp getPost(Long id) {
        log.info("查询岗位详情，id={}", id);
        return postConverter.toPostVO(requirePost(id));
    }

    /**
     * 更新岗位。
     *
     * @param id 岗位编号
     * @param request 岗位更新请求
     * @return 岗位展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public PostResp updatePost(Long id, CreatePostReq request) {
        log.info("更新岗位，id={}，postName={}", id, request.getPostName());
        SysPostEntity entity = requirePost(id);
        fillPost(entity, request);
        entity.setUpdateTime(LocalDateTime.now());
        sysPostMapper.updateById(entity);
        return postConverter.toPostVO(entity);
    }

    /**
     * 删除岗位。
     *
     * @param id 岗位编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long id) {
        log.info("删除岗位，id={}", id);
        SysPostEntity entity = requirePost(id);
        entity.setDeleted(DeletedStatusEnum.DELETED.getType());
        entity.setUpdateTime(LocalDateTime.now());
        sysPostMapper.updateById(entity);
    }

    /**
     * 按编号查询岗位实体。
     *
     * @param id 岗位编号
     * @return 岗位实体
     */
    @Transactional(readOnly = true)
    public SysPostEntity requirePost(Long id) {
        return MybatisTenantHelpers.requireEntity(sysPostMapper.selectOne(
            MybatisTenantHelpers.notDeletedWrapper(SysPostEntity::getDeleted)
                .eq(SysPostEntity::getId, id)
                .last("limit 1")), "岗位不存在");
    }

    /**
     * 填充岗位实体字段。
     *
     * @param entity 岗位实体
     * @param request 岗位请求
     */
    private void fillPost(SysPostEntity entity, CreatePostReq request) {
        entity.setPostCode(request.getPostCode());
        entity.setPostName(request.getPostName());
        entity.setOrderNum(DefaultValueUtils.defaultIfNull(request.getOrderNum(), 0));
        entity.setRemark(request.getRemark());
        entity.setStatus(DefaultValueUtils.defaultIfNull(request.getStatus(), 0));
    }
}
