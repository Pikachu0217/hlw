package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.domain.req.CreateNoticeReq;
import com.hlw.system.domain.resp.NoticeResp;
import com.hlw.system.entity.SysNoticeEntity;
import com.hlw.system.mapper.SysNoticeMapper;
import com.hlw.system.service.support.MybatisTenantHelpers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知公告聚合服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeService {
    /** 通知公告数据访问组件。 */
    private final SysNoticeMapper sysNoticeMapper;

    /**
     * 分页查询通知公告列表。
     *
     * @param query 分页查询条件
     * @return 通知公告分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<NoticeResp> listNotices(PageQuery query) {
        log.info("查询通知公告列表，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());
        LambdaQueryWrapper<SysNoticeEntity> wrapper = new LambdaQueryWrapper<SysNoticeEntity>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(SysNoticeEntity::getNoticeTitle, query.getKeyword());
        }
        wrapper.orderByDesc(SysNoticeEntity::getCreateTime).orderByDesc(SysNoticeEntity::getId);
        Page<SysNoticeEntity> page = sysNoticeMapper.selectPage(query.toPage(), wrapper);
        List<NoticeResp> records = page.getRecords().stream().map(this::toResp).toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 创建通知公告。
     *
     * @param request 通知公告创建请求
     * @return 通知公告展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public NoticeResp createNotice(CreateNoticeReq request) {
        log.info("创建通知公告，noticeTitle={}", request.getNoticeTitle());
        SysNoticeEntity entity = new SysNoticeEntity();
        fillNotice(entity, request);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        sysNoticeMapper.insert(entity);
        return toResp(entity);
    }

    /**
     * 查询通知公告详情。
     *
     * @param id 通知公告编号
     * @return 通知公告展示对象
     */
    @Transactional(readOnly = true)
    public NoticeResp getNotice(Long id) {
        log.info("查询通知公告详情，id={}", id);
        return toResp(requireNotice(id));
    }

    /**
     * 更新通知公告。
     *
     * @param id 通知公告编号
     * @param request 通知公告更新请求
     * @return 通知公告展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public NoticeResp updateNotice(Long id, CreateNoticeReq request) {
        log.info("更新通知公告，id={}，noticeTitle={}", id, request.getNoticeTitle());
        SysNoticeEntity entity = requireNotice(id);
        fillNotice(entity, request);
        entity.setUpdateTime(LocalDateTime.now());
        sysNoticeMapper.updateById(entity);
        return toResp(entity);
    }

    /**
     * 删除通知公告。
     *
     * @param id 通知公告编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(Long id) {
        log.info("删除通知公告，id={}", id);
        SysNoticeEntity entity = requireNotice(id);
        entity.setDeleted(1);
        sysNoticeMapper.updateById(entity);
    }

    /**
     * 填充通知公告实体。
     *
     * @param entity 通知公告实体
     * @param request 通知公告请求
     */
    private void fillNotice(SysNoticeEntity entity, CreateNoticeReq request) {
        entity.setNoticeTitle(request.getNoticeTitle());
        entity.setNoticeType(request.getNoticeType());
        entity.setNoticeContent(request.getNoticeContent());
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), "0"));
        entity.setRemark(request.getRemark());
    }

    /**
     * 转换通知公告展示对象。
     *
     * @param entity 通知公告实体
     * @return 通知公告展示对象
     */
    private NoticeResp toResp(SysNoticeEntity entity) {
        NoticeResp resp = new NoticeResp();
        resp.setId(entity.getId());
        resp.setNoticeTitle(entity.getNoticeTitle());
        resp.setNoticeType(entity.getNoticeType());
        resp.setNoticeContent(entity.getNoticeContent());
        resp.setStatus(entity.getStatus());
        resp.setRemark(entity.getRemark());
        return resp;
    }

    /**
     * 校验通知公告存在。
     *
     * @param id 通知公告编号
     * @return 通知公告实体
     */
    private SysNoticeEntity requireNotice(Long id) {
        return MybatisTenantHelpers.requireEntity(sysNoticeMapper.selectOne(
            new LambdaQueryWrapper<SysNoticeEntity>()
                .eq(SysNoticeEntity::getId, id)
                .last("limit 1")), "通知公告不存在");
    }
}
