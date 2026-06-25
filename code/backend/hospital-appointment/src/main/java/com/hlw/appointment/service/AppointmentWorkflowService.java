package com.hlw.appointment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hlw.appointment.client.ConsultFeignClient;
import com.hlw.appointment.client.req.InternalCreateConsultFromAppointmentRequest;
import com.hlw.appointment.client.req.InternalSyncConsultStatusRequest;
import com.hlw.appointment.domain.resp.*;
import com.hlw.appointment.dto.CreateAppointmentRequest;
import com.hlw.appointment.dto.CreateReleaseConfigRequest;
import com.hlw.appointment.dto.InternalCreateReleaseConfigRequest;
import com.hlw.appointment.entity.AptAppointmentEntity;
import com.hlw.appointment.entity.AptNumberSourceEntity;
import com.hlw.appointment.entity.AptReleaseConfigEntity;
import com.hlw.appointment.mapper.AptAppointmentMapper;
import com.hlw.appointment.mapper.AptNumberSourceMapper;
import com.hlw.appointment.mapper.AptReleaseConfigMapper;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.tenant.TokenPrincipalContext;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.common.redis.lock.RedisLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 * 预约工作流服务，负责预约单、号源锁定和放号配置业务编排。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentWorkflowService {
    private static final String DEFAULT_SOURCE = "PATIENT_H5";
    private static final String DEFAULT_APPOINTMENT_TYPE = "普通门诊";
    private static final String STATUS_PENDING_PAY = AppointmentStatus.PENDING_PAY.dbValue();
    private static final String STATUS_PAID = AppointmentStatus.PAID.dbValue();
    private static final String STATUS_CHECKED_IN = AppointmentStatus.CHECKED_IN.dbValue();
    private static final String STATUS_COMPLETED = AppointmentStatus.COMPLETED.dbValue();
    private static final String STATUS_CANCELLED = AppointmentStatus.CANCELLED.dbValue();
    private static final String STATUS_REJECTED = AppointmentStatus.REJECTED.dbValue();
    private static final String STATUS_GRABBED = AppointmentStatus.GRABBED.dbValue();
    private static final String LEGACY_STATUS_PENDING_PAY = "PENDING_PAY";
    private static final String LEGACY_STATUS_PAID = "PAID";
    private static final String LEGACY_STATUS_CHECKED_IN = "CHECKED_IN";
    private static final String LEGACY_STATUS_COMPLETED = "COMPLETED";
    private static final String LEGACY_STATUS_CANCELLED = "CANCELLED";
    private static final String LEGACY_STATUS_REJECTED = "REJECTED";
    private static final String LEGACY_STATUS_GRABBED = "GRABBED";
    private static final List<String> ACTIVE_DUPLICATE_CHECK_STATUSES = List.of(
        STATUS_PENDING_PAY,
        STATUS_PAID,
        STATUS_CHECKED_IN,
        STATUS_GRABBED,
        LEGACY_STATUS_PENDING_PAY,
        LEGACY_STATUS_PAID,
        LEGACY_STATUS_CHECKED_IN,
        LEGACY_STATUS_GRABBED
    );
    private static final String NUMBER_STATUS_AVAILABLE = "AVAILABLE";
    private static final String NUMBER_STATUS_LOCKED = "LOCKED";
    private static final String NUMBER_STATUS_USED = "USED";
    private static final String DEFAULT_RELEASE_STATUS = "启用";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 预约单数据访问组件。 */
    private final AptAppointmentMapper aptAppointmentMapper;
    /** 预约号源数据访问组件。 */
    private final AptNumberSourceMapper aptNumberSourceMapper;
    /** 放号配置数据访问组件。 */
    private final AptReleaseConfigMapper aptReleaseConfigMapper;
    /** Redis 分布式锁服务。 */
    private final RedisLockService redisLockService;
    /** 问诊服务内部客户端。 */
    private final ConsultFeignClient consultFeignClient;

    /**
     * 查询预约单列表。
     *
     * @return 预约单展示列表
     */
    public List<AppointmentVO> listAppointments() {
        log.info("查询预约单列表");
        return aptAppointmentMapper.selectList(new LambdaQueryWrapper<AptAppointmentEntity>())
            .stream()
            .sorted(Comparator.comparing(AptAppointmentEntity::getId))
            .map(this::toAppointmentVO)
            .toList();
    }

    /**
     * 查询号源列表。
     *
     * @return 号源展示列表
     */
    public List<NumberSourceVO> listNumberSources() {
        log.info("查询号源列表");
        return aptNumberSourceMapper.selectList(new LambdaQueryWrapper<AptNumberSourceEntity>())
            .stream()
            .sorted(Comparator.comparing(AptNumberSourceEntity::getScheduleId)
                .thenComparing(AptNumberSourceEntity::getNumberSeq)
                .thenComparing(AptNumberSourceEntity::getId))
            .map(this::toNumberSourceVO)
            .toList();
    }

    /**
     * 查询号源统计信息（按排班编号汇总容量与状态分布）。
     *
     * @param scheduleId 排班编号
     * @return 号源统计信息
     */
    public NumberSourceStatsVO getNumberSourceStats(Long scheduleId) {
        log.info("查询号源统计信息，scheduleId={}", scheduleId);
        // 总容量：SUM(release_count)
        long totalCapacity = aptReleaseConfigMapper.selectList(new LambdaQueryWrapper<AptReleaseConfigEntity>()
                .eq(AptReleaseConfigEntity::getScheduleId, scheduleId))
            .stream()
            .mapToLong(AptReleaseConfigEntity::getReleaseCount)
            .sum();
        // 已锁定数
        long lockedCount = aptNumberSourceMapper.selectCount(new LambdaQueryWrapper<AptNumberSourceEntity>()
            .eq(AptNumberSourceEntity::getScheduleId, scheduleId)
            .eq(AptNumberSourceEntity::getStatus, NUMBER_STATUS_LOCKED));
        // 已使用数
        long usedCount = aptNumberSourceMapper.selectCount(new LambdaQueryWrapper<AptNumberSourceEntity>()
            .eq(AptNumberSourceEntity::getScheduleId, scheduleId)
            .eq(AptNumberSourceEntity::getStatus, NUMBER_STATUS_USED));
        // 可锁号源数
        long availableCount = Math.max(0, totalCapacity - lockedCount - usedCount);
        NumberSourceStatsVO vo = new NumberSourceStatsVO();
        vo.setScheduleId(scheduleId);
        vo.setTotalCapacity(totalCapacity);
        vo.setLockedCount(lockedCount);
        vo.setUsedCount(usedCount);
        vo.setAvailableCount(availableCount);
        return vo;
    }

    /**
     * 创建预约单。
     *
     * @param request 预约创建请求
     * @return 创建后的预约单
     */
    @Transactional
    public AppointmentVO createAppointment(CreateAppointmentRequest request) {
        TokenPrincipalContext.ensureBusinessTenantContext("预约模块操作缺少有效租户上下文");
        Long scheduleId = request.getScheduleId();
        if (scheduleId == null || scheduleId <= 0) {
            throw new BizException(400, "排班编号不能为空");
        }
        Long patientId = request.getPatientId();
        if (patientId == null || patientId <= 0) {
            throw new BizException(400, "患者编号不能为空");
        }
        Long doctorId = request.getDoctorId();
        if (doctorId == null || doctorId <= 0) {
            throw new BizException(400, "医生编号不能为空");
        }
        Long departmentId = request.getDepartmentId();
        if (departmentId == null || departmentId <= 0) {
            throw new BizException(400, "科室编号不能为空");
        }
        String clinicTime = DefaultValueUtils.defaultIfBlank(request.getTimeSlot(), "");
        if (clinicTime.isBlank()) {
            throw new BizException(400, "门诊时间不能为空");
        }
        String lockKey = "hlw:lock:appointment:" + patientId + ":" + doctorId + ":" + departmentId + ":" + clinicTime;
        log.info("创建预约单，patientId={}，doctorId={}，departmentId={}，scheduleId={}，clinicTime={}",
            patientId, doctorId, departmentId, scheduleId, clinicTime);
        try {
            if (!redisLockService.tryLock(lockKey, 5, 10, TimeUnit.SECONDS)) {
                throw new BizException(409, "预约创建繁忙，请稍后重试");
            }

            // 校验：同一患者同一时间段只能挂一次未结束的同一医生和科室组合，允许取消后重新预约。
            long existingAppointmentCount = aptAppointmentMapper.selectCount(new LambdaQueryWrapper<AptAppointmentEntity>()
                .eq(AptAppointmentEntity::getPatientId, patientId)
                .eq(AptAppointmentEntity::getDoctorId, doctorId)
                .eq(AptAppointmentEntity::getDepartmentId, departmentId)
                .eq(AptAppointmentEntity::getClinicTime, clinicTime)
                .in(AptAppointmentEntity::getStatus, ACTIVE_DUPLICATE_CHECK_STATUSES));
            if (existingAppointmentCount > 0) {
                log.warn("该患者在此时间段已有相同医生科室预约，patientId={}，doctorId={}，departmentId={}，clinicTime={}",
                    patientId, doctorId, departmentId, clinicTime);
                throw new BizException(409, "您在此时间段已预约该医生科室组合，请勿重复挂号");
            }

            NumberSourceVO numberSource = lockNumberSource(scheduleId);
            AptAppointmentEntity entity = new AptAppointmentEntity();
            entity.setPatientId(patientId);
            entity.setDoctorId(doctorId);
            entity.setDepartmentId(departmentId);
            entity.setScheduleId(scheduleId);
            entity.setNumberSourceId(numberSource.getId());
            entity.setAppointmentType(DefaultValueUtils.defaultIfBlank(request.getAppointmentType(), DEFAULT_APPOINTMENT_TYPE));
            entity.setAppointmentNo("");
            entity.setPatientName(DefaultValueUtils.defaultIfBlank(request.getPatientName(), ""));
            entity.setDoctorName(DefaultValueUtils.defaultIfBlank(request.getDoctorName(), ""));
            entity.setClinicTime(clinicTime);
            entity.setSource(DefaultValueUtils.defaultIfBlank(request.getSource(), DEFAULT_SOURCE));
            entity.setStatus(AppointmentStatus.PENDING_PAY.dbValue());
            entity.setFeeAmount(DefaultValueUtils.defaultIfNull(request.getFeeAmount(), new BigDecimal("30")));
            aptAppointmentMapper.insert(entity);
            entity.setAppointmentNo(resolveAppointmentNo(entity.getId()));
            aptAppointmentMapper.updateById(entity);
            createBoundConsult(entity, request.getChiefComplaint(), "UNPAID");
            return toAppointmentVO(entity);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(409, "预约创建被中断");
        } finally {
            redisLockService.unlock(lockKey);
        }
    }

    /**
     * 支付预约单。
     *
     * @param id 预约单编号
     * @return 支付后的预约单
     */
    @Transactional
    public AppointmentVO pay(Long id) {
        TokenPrincipalContext.ensureBusinessTenantContext("预约模块操作缺少有效租户上下文");
        log.info("支付预约单，appointmentId={}", id);
        AptAppointmentEntity entity = requireActiveAppointment(id);
        if (isPaidStatus(entity.getStatus()) || isCheckedInStatus(entity.getStatus()) || isCompletedStatus(entity.getStatus())) {
            log.info("预约单无需重复支付，appointmentId={}，status={}", id, entity.getStatus());
            return toAppointmentVO(entity);
        }
        if (!isPendingPayStatus(entity.getStatus())) {
            throw new BizException(409, "预约单当前状态不允许支付");
        }
        entity.setStatus(STATUS_PAID);
        entity.setPayTime(LocalDateTime.now());
        aptAppointmentMapper.updateById(entity);
        createBoundConsult(entity, "患者暂未填写问题描述", "PAID");
        return toAppointmentVO(entity);
    }

    /**
     * 预约签到。
     *
     * @param id 预约单编号
     * @return 签到后的预约单
     */
    @Transactional
    public AppointmentVO checkIn(Long id) {
        TokenPrincipalContext.ensureBusinessTenantContext("预约模块操作缺少有效租户上下文");
        log.info("预约签到，appointmentId={}", id);
        AptAppointmentEntity entity = requireActiveAppointment(id);
        if (isCheckedInStatus(entity.getStatus()) || isCompletedStatus(entity.getStatus())) {
            log.info("预约单无需重复签到，appointmentId={}，status={}", id, entity.getStatus());
            return toAppointmentVO(entity);
        }
        if (!isPaidStatus(entity.getStatus())) {
            throw new BizException(409, "预约单当前状态不允许签到");
        }
        entity.setStatus(STATUS_CHECKED_IN);
        entity.setCheckInTime(LocalDateTime.now());
        aptAppointmentMapper.updateById(entity);
        markNumberSourceUsed(entity.getNumberSourceId());
        return toAppointmentVO(entity);
    }

    /**
     * 取消预约单。
     * <p>未支付直接释放号源；已支付则先退款（占位，待接入真实支付），再释放号源。</p>
     *
     * @param id 预约单编号
     * @return 取消后的预约单
     */
    @Transactional
    public AppointmentVO cancel(Long id) {
        TokenPrincipalContext.ensureBusinessTenantContext("预约模块操作缺少有效租户上下文");
        log.info("取消预约单，appointmentId={}", id);
        AptAppointmentEntity entity = requireActiveAppointment(id);
        if (isCancelledStatus(entity.getStatus())) {
            log.info("预约单无需重复取消，appointmentId={}", id);
            return toAppointmentVO(entity);
        }
        if (isCompletedStatus(entity.getStatus())) {
            throw new BizException(409, "已完成预约单不允许取消");
        }
        if (isRejectedStatus(entity.getStatus())) {
            throw new BizException(409, "已拒诊预约单不允许取消");
        }
        // 已支付：先退款（占位），再释放号源
        if (isPaidStatus(entity.getStatus()) || isCheckedInStatus(entity.getStatus())) {
            refundAppointment(entity);
        }
        // 释放号源
        releaseNumberSource(entity.getNumberSourceId());
        entity.setStatus(STATUS_CANCELLED);
        entity.setCancelTime(LocalDateTime.now());
        entity.setCancelReason("患者取消预约");
        aptAppointmentMapper.updateById(entity);
        syncConsultStatus(entity.getId(), "4", null, "患者取消预约");
        log.info("预约单已取消，appointmentId={}", id);
        return toAppointmentVO(entity);
    }

    /**
     * 释放号源（将 LOCKED 状态改为 AVAILABLE）。
     *
     * @param numberSourceId 号源编号
     */
    private void releaseNumberSource(Long numberSourceId) {
        if (numberSourceId == null) {
            return;
        }
        aptNumberSourceMapper.update(null, new LambdaUpdateWrapper<AptNumberSourceEntity>()
            .eq(AptNumberSourceEntity::getId, numberSourceId)
            .set(AptNumberSourceEntity::getStatus, NUMBER_STATUS_AVAILABLE)
            .set(AptNumberSourceEntity::getLockTime, null));
        log.info("号源已释放，numberSourceId={}", numberSourceId);
    }

    /**
     * 退款占位方法，待接入真实支付后实现实际退款逻辑。
     *
     * @param entity 预约单实体
     */
    private void refundAppointment(AptAppointmentEntity entity) {
        log.info("[支付占位] 预约取消需退款，appointmentId={}，amount={}，待接入真实支付后实现", entity.getId(), entity.getFeeAmount());
    }

    /**
     * 内部接口：支付成功回调，更新预约单状态为已支付。
     *
     * @param id 预约单编号
     * @return 更新后的预约单
     */
    @Transactional
    public AppointmentVO onPaySuccess(Long id) {
        log.info("支付成功回调更新预约单，appointmentId={}", id);
        AptAppointmentEntity entity = requireActiveAppointment(id);
        if (isPaidStatus(entity.getStatus()) || isCheckedInStatus(entity.getStatus()) || isCompletedStatus(entity.getStatus())) {
            log.info("预约单无需重复支付回调，appointmentId={}，status={}", id, entity.getStatus());
            return toAppointmentVO(entity);
        }
        if (!isPendingPayStatus(entity.getStatus())) {
            throw new BizException(409, "预约单当前状态不允许支付回调");
        }
        entity.setStatus(STATUS_PAID);
        entity.setPayTime(LocalDateTime.now());
        aptAppointmentMapper.updateById(entity);
        createBoundConsult(entity, "患者暂未填写问题描述", "PAID");
        return toAppointmentVO(entity);
    }

    /**
     * 内部接口：医生拒诊后同步预约单状态。
     *
     * @param id 预约单编号
     * @param reason 拒诊原因
     * @return 更新后的预约单
     */
    @Transactional
    public AppointmentVO rejectFromConsult(Long id, String reason) {
        log.info("问诊拒诊同步预约单，appointmentId={}，reason={}", id, reason);
        AptAppointmentEntity entity = requireActiveAppointment(id);
        if (isRejectedStatus(entity.getStatus())) {
            log.info("预约单无需重复拒诊，appointmentId={}", id);
            return toAppointmentVO(entity);
        }
        if (isCompletedStatus(entity.getStatus()) || isCancelledStatus(entity.getStatus())) {
            throw new BizException(409, "预约单当前状态不允许拒诊");
        }
        if (isPaidStatus(entity.getStatus()) || isCheckedInStatus(entity.getStatus())) {
            refundAppointment(entity);
        }
        releaseNumberSource(entity.getNumberSourceId());
        entity.setStatus(STATUS_REJECTED);
        entity.setRejectTime(LocalDateTime.now());
        entity.setRejectReason(DefaultValueUtils.defaultIfBlank(reason, "医生拒诊"));
        aptAppointmentMapper.updateById(entity);
        return toAppointmentVO(entity);
    }

    /**
     * 抢便民门诊预约单。
     *
     * @param id 预约单编号
     * @param doctorId 医生编号
     * @return 抢单是否成功
     */
    @Transactional
    public Boolean grab(Long id, Long doctorId) {
        TokenPrincipalContext.ensureBusinessTenantContext("预约模块操作缺少有效租户上下文");
        if (doctorId == null) {
            throw new BizException(400, "医生编号不能为空");
        }
        log.info("抢便民门诊预约单，appointmentId={}，doctorId={}", id, doctorId);
        AptAppointmentEntity entity = requireActiveAppointment(id);
        if (isCheckedInStatus(entity.getStatus()) || isGrabbedStatus(entity.getStatus())) {
            log.info("预约单无需重复抢单，appointmentId={}，status={}", id, entity.getStatus());
            return true;
        }
        if (isCancelledStatus(entity.getStatus()) || isCompletedStatus(entity.getStatus())) {
            throw new BizException(409, "预约单当前状态不允许抢单");
        }
        entity.setDoctorId(doctorId);
        entity.setStatus(STATUS_GRABBED);
        return aptAppointmentMapper.updateById(entity) > 0;
    }

    /**
     * 使用 Redis 分布式锁，按需生成并锁定一个号源。
     * <p>不再从预生成的 AVAILABLE 记录中选取，而是校验释放容量后直接 INSERT 一条 LOCKED 记录。</p>
     *
     * @param scheduleId 排班编号
     * @return 锁定后的号源
     */
    @Transactional
    public NumberSourceVO lockNumberSource(Long scheduleId) {
        TokenPrincipalContext.ensureBusinessTenantContext("预约模块操作缺少有效租户上下文");
        String lockKey = "hlw:lock:number:" + scheduleId;
        log.info("尝试获取号源分布式锁，scheduleId={}", scheduleId);
        try {
            if (!redisLockService.tryLock(lockKey, 5, 10, TimeUnit.SECONDS)) {
                throw new BizException(409, "号源锁定繁忙，请稍后重试");
            }
            // 统计当前排班已占号源数（LOCKED + USED）
            Long usedCount = aptNumberSourceMapper.selectCount(new LambdaQueryWrapper<AptNumberSourceEntity>()
                .eq(AptNumberSourceEntity::getScheduleId, scheduleId)
                .in(AptNumberSourceEntity::getStatus, NUMBER_STATUS_LOCKED, NUMBER_STATUS_USED));
            // 查询放号配置总容量（SUM(release_count)）
            long capacity = aptReleaseConfigMapper.selectList(new LambdaQueryWrapper<AptReleaseConfigEntity>()
                    .eq(AptReleaseConfigEntity::getScheduleId, scheduleId))
                .stream()
                .mapToLong(AptReleaseConfigEntity::getReleaseCount)
                .sum();
            if (capacity <= 0) {
                throw new BizException(404, "该排班暂无放号配置，请先创建放号配置");
            }
            if (usedCount >= capacity) {
                throw new BizException(404, "暂无可锁号源");
            }
            // 计算下一个序号：已占序列最大值 + 1
            int nextSeq = aptNumberSourceMapper.selectList(new LambdaQueryWrapper<AptNumberSourceEntity>()
                    .eq(AptNumberSourceEntity::getScheduleId, scheduleId))
                .stream()
                .map(AptNumberSourceEntity::getNumberSeq)
                .max(Integer::compareTo)
                .orElse(0) + 1;
            // 直接插入一条 LOCKED 号源
            AptNumberSourceEntity entity = new AptNumberSourceEntity();
            entity.setScheduleId(scheduleId);
            entity.setNumberSeq(nextSeq);
            entity.setStatus(NUMBER_STATUS_LOCKED);
            entity.setLockTime(LocalDateTime.now());
            aptNumberSourceMapper.insert(entity);
            log.info("按需生成并锁定号源，scheduleId={}，numberSeq={}，sourceId={}", scheduleId, nextSeq, entity.getId());
            return toNumberSourceVO(entity);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(409, "号源锁定被中断");
        } finally {
            redisLockService.unlock(lockKey);
        }
    }

    /**
     * 创建放号配置。
     *
     * @param request 放号配置请求
     * @return 创建后的配置
     */
    @Transactional
    public ReleaseConfigVO createReleaseConfig(CreateReleaseConfigRequest request) {
        TokenPrincipalContext.ensureBusinessTenantContext("预约模块操作缺少有效租户上下文");
        Integer releaseCount = DefaultValueUtils.defaultIfNull(request.getReleaseCount(), 10);
        LocalDateTime releaseTime = parseDateTime(request.getReleaseAt());
        log.info("创建放号配置，scheduleId={}，releaseAt={}，releaseCount={}",
            request.getScheduleId(), request.getReleaseAt(), releaseCount);
        AptReleaseConfigEntity entity = new AptReleaseConfigEntity();
        entity.setScheduleId(request.getScheduleId());
        entity.setReleaseTime(releaseTime);
        entity.setReleaseCount(releaseCount);
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), DEFAULT_RELEASE_STATUS));
        aptReleaseConfigMapper.insert(entity);
        releaseNumberSources(request.getScheduleId(), releaseCount);
        return toReleaseConfigVO(entity);
    }

    /**
     * 创建放号配置并释放号源（内部接口），使用当前时间作为放号时间。
     *
     * @param request 内部请求
     * @return 创建后的配置
     */
    @Transactional
    public ReleaseConfigVO createReleaseConfig(InternalCreateReleaseConfigRequest request) {
        log.info("内部创建放号配置，scheduleId={}，releaseCount={}", request.getScheduleId(), request.getReleaseCount());
        AptReleaseConfigEntity entity = new AptReleaseConfigEntity();
        entity.setScheduleId(request.getScheduleId());
        entity.setReleaseTime(LocalDateTime.now());
        entity.setReleaseCount(request.getReleaseCount());
        entity.setStatus(DEFAULT_RELEASE_STATUS);
        aptReleaseConfigMapper.insert(entity);
        releaseNumberSources(request.getScheduleId(), request.getReleaseCount());
        return toReleaseConfigVO(entity);
    }

    /**
     * 查询预约单并校验存在。
     *
     * @param id 预约单编号
     * @return 预约单实体
     */
    private AptAppointmentEntity requireActiveAppointment(Long id) {
        AptAppointmentEntity entity = aptAppointmentMapper.selectOne(new LambdaQueryWrapper<AptAppointmentEntity>()
            .eq(AptAppointmentEntity::getId, id)
            .last("limit 1"));
        if (entity == null) {
            throw new BizException(404, "预约单不存在");
        }
        return entity;
    }

    /**
     * 查询首个可用号源。
     *
     * @param scheduleId 排班编号
     * @return 号源实体
     */
    private AptNumberSourceEntity requireFirstAvailableNumberSource(Long scheduleId) {
        AptNumberSourceEntity entity = aptNumberSourceMapper.selectOne(new LambdaQueryWrapper<AptNumberSourceEntity>()
            .eq(AptNumberSourceEntity::getScheduleId, scheduleId)
            .eq(AptNumberSourceEntity::getStatus, NUMBER_STATUS_AVAILABLE)
            .orderByAsc(AptNumberSourceEntity::getNumberSeq)
            .orderByAsc(AptNumberSourceEntity::getId)
            .last("limit 1"));
        if (entity == null) {
            throw new BizException(404, "暂无可用号源");
        }
        return entity;
    }

    /**
     * 判断预约单是否处于待支付状态。
     *
     * @param status 预约单状态
     * @return 是否待支付状态
     */
    private boolean isPendingPayStatus(String status) {
        return STATUS_PENDING_PAY.equals(status) || LEGACY_STATUS_PENDING_PAY.equals(status);
    }

    /**
     * 判断预约单是否处于已支付状态。
     *
     * @param status 预约单状态
     * @return 是否已支付状态
     */
    private boolean isPaidStatus(String status) {
        return STATUS_PAID.equals(status) || LEGACY_STATUS_PAID.equals(status);
    }

    /**
     * 判断预约单是否处于已签到状态。
     *
     * @param status 预约单状态
     * @return 是否已签到状态
     */
    private boolean isCheckedInStatus(String status) {
        return STATUS_CHECKED_IN.equals(status) || LEGACY_STATUS_CHECKED_IN.equals(status);
    }

    /**
     * 判断预约单是否处于已完成状态。
     *
     * @param status 预约单状态
     * @return 是否已完成状态
     */
    private boolean isCompletedStatus(String status) {
        return STATUS_COMPLETED.equals(status) || LEGACY_STATUS_COMPLETED.equals(status);
    }

    /**
     * 判断预约单是否处于已取消状态。
     *
     * @param status 预约单状态
     * @return 是否已取消状态
     */
    private boolean isCancelledStatus(String status) {
        return STATUS_CANCELLED.equals(status) || LEGACY_STATUS_CANCELLED.equals(status);
    }

    /**
     * 判断预约单是否处于已拒诊状态。
     *
     * @param status 预约单状态
     * @return 是否已拒诊状态
     */
    private boolean isRejectedStatus(String status) {
        return STATUS_REJECTED.equals(status) || LEGACY_STATUS_REJECTED.equals(status);
    }

    /**
     * 判断预约单是否处于已接单状态。
     *
     * @param status 预约单状态
     * @return 是否已接单状态
     */
    private boolean isGrabbedStatus(String status) {
        return STATUS_GRABBED.equals(status) || LEGACY_STATUS_GRABBED.equals(status);
    }

    /**
     * 标记号源已使用。
     *
     * @param numberSourceId 号源编号
     */
    private void markNumberSourceUsed(Long numberSourceId) {
        if (numberSourceId == null) {
            return;
        }
        aptNumberSourceMapper.update(null, new LambdaUpdateWrapper<AptNumberSourceEntity>()
            .eq(AptNumberSourceEntity::getId, numberSourceId)
            .set(AptNumberSourceEntity::getStatus, NUMBER_STATUS_USED));
    }

    /**
     * 创建预约绑定问诊单。
     *
     * @param entity 预约单实体
     * @param chiefComplaint 患者问题描述
     * @param payStatus 支付状态
     */
    private void createBoundConsult(AptAppointmentEntity entity, String chiefComplaint, String payStatus) {
        InternalCreateConsultFromAppointmentRequest request = new InternalCreateConsultFromAppointmentRequest();
        request.setAppointmentId(entity.getId());
        request.setPatientId(entity.getPatientId());
        request.setDoctorId(entity.getDoctorId());
        request.setPatientName(entity.getPatientName());
        request.setDoctorName(entity.getDoctorName());
        request.setFeeAmount(entity.getFeeAmount());
        request.setPayStatus(payStatus);
        request.setChiefComplaint(DefaultValueUtils.defaultIfBlank(chiefComplaint, "患者暂未填写问题描述"));
        log.info("创建预约绑定问诊，appointmentId={}，patientId={}，doctorId={}，payStatus={}",
            entity.getId(), entity.getPatientId(), entity.getDoctorId(), payStatus);
        var response = consultFeignClient.createFromAppointment(request);
        if (response == null || response.code() != 200) {
            log.warn("创建预约绑定问诊失败，appointmentId={}，response={}", entity.getId(), response);
            throw new BizException(500, "创建预约绑定问诊失败");
        }
    }

    /**
     * 同步问诊状态。
     *
     * @param appointmentId 预约单编号
     * @param status 问诊状态
     * @param payStatus 支付状态
     * @param reason 同步原因
     */
    private void syncConsultStatus(Long appointmentId, String status, String payStatus, String reason) {
        InternalSyncConsultStatusRequest request = new InternalSyncConsultStatusRequest();
        request.setAppointmentId(appointmentId);
        request.setStatus(status);
        request.setPayStatus(payStatus);
        request.setReason(reason);
        log.info("同步问诊状态，appointmentId={}，status={}，payStatus={}，reason={}", appointmentId, status, payStatus, reason);
        var response = consultFeignClient.syncStatus(request);
        if (response == null || response.code() != 200) {
            log.warn("同步问诊状态失败，appointmentId={}，response={}", appointmentId, response);
            throw new BizException(500, "同步问诊状态失败");
        }
    }

    /**
     * 按放号配置生成可用号源（已改为按需生成，占号时再 INSERT）。
     * <p>放号配置写入 release_config 后不再预生成号源记录，
     * 实际号源在 lockNumberSource 时按容量校验并动态插入。</p>
     *
     * @param scheduleId 排班编号
     * @param releaseCount 放号数量（当前仅用于日志记录，不再插入号源）
     */
    private void releaseNumberSources(Long scheduleId, Integer releaseCount) {
        log.info("放号配置已记录，scheduleId={}，releaseCount={}，号源将在占号时按需生成", scheduleId, releaseCount);
    }

    /**
     * 转换预约单展示对象。
     *
     * @param entity 预约单实体
     * @return 预约单展示对象
     */
    private AppointmentVO toAppointmentVO(AptAppointmentEntity entity) {
        AppointmentVO vo = new AppointmentVO();
        vo.setId(entity.getId());
        vo.setAppointmentNo(DefaultValueUtils.defaultIfBlank(entity.getAppointmentNo(), resolveAppointmentNo(entity.getId())));
        vo.setPatientName(DefaultValueUtils.defaultIfBlank(entity.getPatientName(), ""));
        vo.setDoctorName(DefaultValueUtils.defaultIfBlank(entity.getDoctorName(), ""));
        vo.setClinicTime(DefaultValueUtils.defaultIfBlank(entity.getClinicTime(), ""));
        vo.setSource(DefaultValueUtils.defaultIfBlank(entity.getSource(), DEFAULT_SOURCE));
        vo.setStatus(DefaultValueUtils.defaultIfBlank(entity.getStatus(), STATUS_PENDING_PAY));
        vo.setFeeAmount(DefaultValueUtils.defaultIfNull(entity.getFeeAmount(), BigDecimal.ZERO).toPlainString());
        return vo;
    }

    /**
     * 内部查询预约单基本信息（供 consult 模块 Feign 调用）。
     *
     * @param id 预约单编号
     * @return 预约单内部响应
     */
    public InternalAppointmentResp getInternalAppointment(Long id) {
        log.info("内部查询预约单，appointmentId={}", id);
        AptAppointmentEntity entity = requireActiveAppointment(id);
        return new InternalAppointmentResp(
            entity.getId(),
            entity.getPatientId(),
            entity.getDoctorId(),
            DefaultValueUtils.defaultIfBlank(entity.getPatientName(), ""),
            DefaultValueUtils.defaultIfBlank(entity.getDoctorName(), ""),
            DefaultValueUtils.defaultIfNull(entity.getFeeAmount(), BigDecimal.ZERO).toPlainString(),
            DefaultValueUtils.defaultIfBlank(entity.getStatus(), STATUS_PENDING_PAY)
        );
    }

    /**
     * 转换号源展示对象。
     *
     * @param entity 号源实体
     * @return 号源展示对象
     */
    private NumberSourceVO toNumberSourceVO(AptNumberSourceEntity entity) {
        NumberSourceVO vo = new NumberSourceVO();
        vo.setId(entity.getId());
        vo.setScheduleId(entity.getScheduleId());
        vo.setNumberSeq(entity.getNumberSeq());
        vo.setStatus(entity.getStatus());
        return vo;
    }

    /**
     * 转换放号配置展示对象。
     *
     * @param entity 放号配置实体
     * @return 放号配置展示对象
     */
    private ReleaseConfigVO toReleaseConfigVO(AptReleaseConfigEntity entity) {
        ReleaseConfigVO vo = new ReleaseConfigVO();
        vo.setId(entity.getId());
        vo.setScheduleId(entity.getScheduleId());
        vo.setReleaseAt(entity.getReleaseTime() == null ? "" : entity.getReleaseTime().format(DATE_TIME_FORMATTER));
        vo.setReleaseCount(entity.getReleaseCount());
        vo.setStatus(entity.getStatus());
        return vo;
    }

    /**
     * 生成预约单号。
     *
     * @param id 预约单编号
     * @return 预约单号
     */
    private String resolveAppointmentNo(Long id) {
        return "YY" + LocalDate.now().format(DATE_FORMATTER) + String.format("%04d", id);
    }

    /**
     * 解析日期时间。
     *
     * @param value 日期时间字符串
     * @return 日期时间
     */
    private LocalDateTime parseDateTime(String value) {
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new BizException(400, "放号时间格式必须为 yyyy-MM-dd HH:mm:ss");
        }
    }

}
