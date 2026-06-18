package com.hlw.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.auth.domain.req.CreateLoginRecordReq;
import com.hlw.auth.domain.req.UpdateLoginRecordReq;
import com.hlw.auth.domain.resp.LoginRecordResp;
import com.hlw.auth.domain.resp.LoginUserResp;
import com.hlw.auth.entity.AuthLoginRecordEntity;
import com.hlw.auth.mapper.AuthLoginRecordMapper;
import com.hlw.auth.service.converter.LoginRecordConverter;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.DeletedStatusEnum;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.util.DefaultValueUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 登录记录聚合服务，负责认证登录记录的查询、维护和认证流程写入。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginRecordService {
    /** 登录成功状态。 */
    public static final String STATUS_SUCCESS = "SUCCESS";
    /** 登录失败状态。 */
    public static final String STATUS_FAILED = "FAILED";
    /** 已退出状态。 */
    public static final String STATUS_LOGOUT = "LOGOUT";
    /** 日期时间格式化器。 */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 登录记录数据访问组件。 */
    private final AuthLoginRecordMapper authLoginRecordMapper;
    /** 登录记录展示对象转换器。 */
    private final LoginRecordConverter loginRecordConverter;

    /**
     * 分页查询登录记录。
     *
     * @param query 分页查询条件
     * @return 登录记录分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<LoginRecordResp> listLoginRecords(PageQuery query) {
        log.info("查询登录记录列表分页，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());
        Page<AuthLoginRecordEntity> page = query.toPage();
        LambdaQueryWrapper<AuthLoginRecordEntity> wrapper = activeWrapper();
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword();
            wrapper.and(w -> w.like(AuthLoginRecordEntity::getUsername, keyword)
                .or()
                .like(AuthLoginRecordEntity::getClientIp, keyword));
        }
        wrapper.orderByDesc(AuthLoginRecordEntity::getLoginTime).orderByDesc(AuthLoginRecordEntity::getId);
        Page<AuthLoginRecordEntity> result = ignoreTenantLine(() -> authLoginRecordMapper.selectPage(page, wrapper));
        List<LoginRecordResp> records = result.getRecords().stream()
            .map(loginRecordConverter::toResp)
            .toList();
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 创建登录记录。
     *
     * @param request 创建登录记录请求
     * @return 登录记录展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginRecordResp createLoginRecord(CreateLoginRecordReq request) {
        log.info("创建登录记录，tenantId={}，username={}，loginStatus={}",
            request.getTenantId(), request.getUsername(), request.getLoginStatus());
        AuthLoginRecordEntity entity = new AuthLoginRecordEntity();
        entity.setTenantId(request.getTenantId());
        entity.setUserId(request.getUserId());
        entity.setUsername(request.getUsername());
        entity.setUserType(DefaultValueUtils.defaultIfBlank(request.getUserType(), ""));
        entity.setLoginStatus(request.getLoginStatus());
        entity.setFailureReason(DefaultValueUtils.defaultIfBlank(request.getFailureReason(), ""));
        entity.setTokenDigest(DefaultValueUtils.defaultIfBlank(request.getTokenDigest(), ""));
        entity.setLoginTime(LocalDateTime.now());
        entity.setClientIp(DefaultValueUtils.defaultIfBlank(request.getClientIp(), ""));
        entity.setUserAgent(DefaultValueUtils.defaultIfBlank(request.getUserAgent(), ""));
        entity.setDeleted(DeletedStatusEnum.NOT_DELETED.getType());
        ignoreTenantLine(() -> authLoginRecordMapper.insert(entity));
        return loginRecordConverter.toResp(entity);
    }

    /**
     * 查询登录记录详情。
     *
     * @param recordId 登录记录编号
     * @return 登录记录展示对象
     */
    @Transactional(readOnly = true)
    public LoginRecordResp getLoginRecord(Long recordId) {
        log.info("查询登录记录详情，recordId={}", recordId);
        return loginRecordConverter.toResp(requireActiveRecord(recordId));
    }

    /**
     * 更新登录记录。
     *
     * @param recordId 登录记录编号
     * @param request 更新登录记录请求
     * @return 更新后的登录记录展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginRecordResp updateLoginRecord(Long recordId, UpdateLoginRecordReq request) {
        log.info("更新登录记录，recordId={}，loginStatus={}", recordId, request.getLoginStatus());
        AuthLoginRecordEntity entity = requireActiveRecord(recordId);
        entity.setLoginStatus(request.getLoginStatus());
        entity.setFailureReason(DefaultValueUtils.defaultIfBlank(request.getFailureReason(), ""));
        entity.setLogoutTime(parseDateTime(request.getLogoutTime()));
        entity.setClientIp(DefaultValueUtils.defaultIfBlank(request.getClientIp(), entity.getClientIp()));
        entity.setUserAgent(DefaultValueUtils.defaultIfBlank(request.getUserAgent(), entity.getUserAgent()));
        ignoreTenantLine(() -> authLoginRecordMapper.updateById(entity));
        return loginRecordConverter.toResp(entity);
    }

    /**
     * 删除登录记录。
     *
     * @param recordId 登录记录编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteLoginRecord(Long recordId) {
        log.info("删除登录记录，recordId={}", recordId);
        AuthLoginRecordEntity entity = requireActiveRecord(recordId);
        entity.setDeleted(DeletedStatusEnum.DELETED.getType());
        ignoreTenantLine(() -> authLoginRecordMapper.updateById(entity));
    }

    /**
     * 记录登录成功。
     *
     * @param user 登录用户信息
     * @param rawToken 原始令牌
     * @param clientIp 客户端 IP
     * @param userAgent 客户端标识
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordLoginSuccess(LoginUserResp user, String rawToken, String clientIp, String userAgent) {
        log.info("记录登录成功，tenantId={}，userId={}，username={}", user.tenantId(), user.id(), user.username());
        AuthLoginRecordEntity entity = new AuthLoginRecordEntity();
        entity.setTenantId(user.tenantId());
        entity.setUserId(user.id());
        entity.setUsername(user.username());
        entity.setUserType(user.userType());
        entity.setLoginStatus(STATUS_SUCCESS);
        entity.setFailureReason("");
        entity.setTokenDigest(tokenDigest(rawToken));
        entity.setLoginTime(LocalDateTime.now());
        entity.setClientIp(DefaultValueUtils.defaultIfBlank(clientIp, ""));
        entity.setUserAgent(DefaultValueUtils.defaultIfBlank(userAgent, ""));
        entity.setDeleted(DeletedStatusEnum.NOT_DELETED.getType());
        ignoreTenantLine(() -> authLoginRecordMapper.insert(entity));
    }

    /**
     * 记录登录失败。
     *
     * @param tenantId 租户编号
     * @param username 登录账号
     * @param failureReason 失败原因
     * @param clientIp 客户端 IP
     * @param userAgent 客户端标识
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordLoginFailure(Long tenantId, String username, String failureReason, String clientIp, String userAgent) {
        log.warn("记录登录失败，tenantId={}，username={}，failureReason={}", tenantId, username, failureReason);
        AuthLoginRecordEntity entity = new AuthLoginRecordEntity();
        entity.setTenantId(tenantId);
        entity.setUsername(DefaultValueUtils.defaultIfBlank(username, ""));
        entity.setUserType("");
        entity.setLoginStatus(STATUS_FAILED);
        entity.setFailureReason(DefaultValueUtils.defaultIfBlank(failureReason, ""));
        entity.setTokenDigest("");
        entity.setLoginTime(LocalDateTime.now());
        entity.setClientIp(DefaultValueUtils.defaultIfBlank(clientIp, ""));
        entity.setUserAgent(DefaultValueUtils.defaultIfBlank(userAgent, ""));
        entity.setDeleted(DeletedStatusEnum.NOT_DELETED.getType());
        ignoreTenantLine(() -> authLoginRecordMapper.insert(entity));
    }

    /**
     * 记录退出登录。
     *
     * @param rawToken 原始令牌
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordLogout(String rawToken) {
        String digest = tokenDigest(rawToken);
        if (!StringUtils.hasText(digest)) {
            log.warn("退出登录记录失败，令牌摘要为空");
            return;
        }
        AuthLoginRecordEntity entity = ignoreTenantLine(() -> authLoginRecordMapper.selectOne(activeWrapper()
            .eq(AuthLoginRecordEntity::getTokenDigest, digest)
            .isNull(AuthLoginRecordEntity::getLogoutTime)
            .orderByDesc(AuthLoginRecordEntity::getLoginTime)
            .last("limit 1")));
        if (entity == null) {
            log.warn("退出登录未找到登录记录，tokenDigest={}", digest);
            return;
        }
        entity.setLoginStatus(STATUS_LOGOUT);
        entity.setLogoutTime(LocalDateTime.now());
        ignoreTenantLine(() -> authLoginRecordMapper.updateById(entity));
        log.info("退出登录记录成功，recordId={}", entity.getId());
    }

    /**
     * 计算令牌摘要。
     *
     * @param rawToken 原始令牌
     * @return 摘要字符串
     */
    public String tokenDigest(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            return "";
        }
        return DigestUtils.md5DigestAsHex(rawToken.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 构造未删除登录记录查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<AuthLoginRecordEntity> activeWrapper() {
        return new LambdaQueryWrapper<AuthLoginRecordEntity>()
            .eq(AuthLoginRecordEntity::getDeleted, DeletedStatusEnum.NOT_DELETED.getType());
    }

    /**
     * 校验登录记录处于可用状态。
     *
     * @param recordId 登录记录编号
     * @return 登录记录实体
     */
    private AuthLoginRecordEntity requireActiveRecord(Long recordId) {
        AuthLoginRecordEntity entity = ignoreTenantLine(() -> authLoginRecordMapper.selectOne(activeWrapper()
            .eq(AuthLoginRecordEntity::getId, recordId)
            .last("limit 1")));
        if (entity == null) {
            throw new BizException(404, "登录记录不存在");
        }
        return entity;
    }

    /**
     * 解析日期时间。
     *
     * @param dateTimeText 日期时间文本
     * @return 日期时间，空值时返回 null
     */
    private LocalDateTime parseDateTime(String dateTimeText) {
        if (!StringUtils.hasText(dateTimeText)) {
            return null;
        }
        return LocalDateTime.parse(dateTimeText.trim(), DATE_TIME_FORMATTER);
    }

    /**
     * 在认证登录记录读写时忽略租户行过滤。
     *
     * @param supplier 数据库操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    private <T> T ignoreTenantLine(java.util.function.Supplier<T> supplier) {
        return InterceptorIgnoreHelper.execute(IgnoreStrategy.builder().tenantLine(true).build(), supplier);
    }
}
