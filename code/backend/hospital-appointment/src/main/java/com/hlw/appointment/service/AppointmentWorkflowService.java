package com.hlw.appointment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
 * 预约工作流服务，负责预约单、号源锁定和放号配置业务编排�?
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentWorkflowService {
    private static final String DEFAULT_SOURCE = "PATIENT_H5";
    private static final String DEFAULT_APPOINTMENT_TYPE = "普通门�?;
    private static final String STATUS_PENDING_PAY = AppointmentStatus.PENDING_PAY.dbValue();
    private static final String STATUS_PAID = AppointmentStatus.PAID.dbValue();
    private static final String STATUS_CHECKED_IN = AppointmentStatus.CHECKED_IN.dbValue();
    private static final String STATUS_COMPLETED = AppointmentStatus.COMPLETED.dbValue();
    private static final String STATUS_CANCELLED = AppointmentStatus.CANCELLED.dbValue();
    private static final String STATUS_GRABBED = AppointmentStatus.GRABBED.dbValue();
    private static final String NUMBER_STATUS_AVAILABLE = "AVAILABLE";
    private static final String NUMBER_STATUS_LOCKED = "LOCKED";
    private static final String NUMBER_STATUS_USED = "USED";
    private static final String DEFAULT_RELEASE_STATUS = "启用";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 预约单数据访问组件�?*/
    private final AptAppointmentMapper aptAppointmentMapper;
    /** 预约号源数据访问组件�?*/
    private final AptNumberSourceMapper aptNumberSourceMapper;
    /** 放号配置数据访问组件�?*/
    private final AptReleaseConfigMapper aptReleaseConfigMapper;
    /** Redis 分布式锁服务�?*/
    private final RedisLockService redisLockService;

    /**
     * 查询预约单列表�?
     *
     * @return 预约单展示列�?
     */
    public List<AppointmentVO> listAppointments() {
        log.info("查询预约单列�?);
        return aptAppointmentMapper.selectList(new LambdaQueryWrapper<>())
            .stream()
            .sorted(Comparator.comparing(AptAppointmentEntity::getId))
            .map(this::toAppointmentVO)
            .toList();
    }

    /**
     * 查询号源列表�?
     *
     * @return 号源展示列表
     */
    public List<NumberSourceVO> listNumberSources() {
        log.info("查询号源列表");
        return aptNumberSourceMapper.selectList(new LambdaQueryWrapper<>())
            .stream()
            .sorted(Comparator.comparing(AptNumberSourceEntity::getScheduleId)
                .thenComparing(AptNumberSourceEntity::getNumberSeq)
                .thenComparing(AptNumberSourceEntity::getId))
            .map(this::toNumberSourceVO)
            .toList();
    }

    /**
     * 查询号源统计信息（按排班编号汇总容量与状态分布）�?
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
        // 可锁号源�?
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
     * 创建预约单�?
     *
     * @param request 预约创建请求
     * @return 创建后的预约�?
     */
    @Transactional
    public AppointmentVO createAppointment(CreateAppointmentRequest request) {
        TokenPrincipalContext.ensureBusinessTenantContext("预约模块操作缺少有效租户上下�?);
        Long scheduleId = request.getScheduleId();
        if (scheduleId == null || scheduleId <= 0) {
            throw new BizException(400, "排班编号不能为空");
        }
        Long patientId = request.getPatientId();
        if (patientId == null || patientId <= 0) {
            throw new BizException(400, "患者编号不能为�?);
        }
        log.info("创建预约单，patientId={}，doctorId={}，scheduleId={}",
            request.getPatientId(), request.getDoctorId(), scheduleId);

        // 校验：同一患者同一排班时间段不能重复挂号（已取消或已完成的不算�?
        long existingAppointmentCount = aptAppointmentMapper.selectCount(new LambdaQueryWrapper<AptAppointmentEntity>()
            .eq(AptAppointmentEntity::getPatientId, patientId)
            .eq(AptAppointmentEntity::getScheduleId, scheduleId)
            .notIn(AptAppointmentEntity::getStatus, AppointmentStatus.CANCELLED.dbValue(), AppointmentStatus.COMPLETED.dbValue()));
        if (existingAppointmentCount > 0) {
            log.warn("该患者在此时间段已有预约记录，patientId={}，scheduleId={}", patientId, scheduleId);
            throw new BizException(409, "您在此时间段已有预约记录，请勿重复挂�?);
        }

        NumberSourceVO numberSource = lockNumberSource(scheduleId);
        AptAppointmentEntity entity = new AptAppointmentEntity();
        entity.setPatientId(patientId);
        entity.setDoctorId(request.getDoctorId());
        entity.setDepartmentId(request.getDepartmentId());
        entity.setScheduleId(scheduleId);
        entity.setNumberSourceId(numberSource.getId());
        entity.setAppointmentType(DefaultValueUtils.defaultIfBlank(request.getAppointmentType(), DEFAULT_APPOINTMENT_TYPE));
        entity.setAppointmentNo("");
        entity.setPatientName(DefaultValueUtils.defaultIfBlank(request.getPatientName(), ""));
        entity.setDoctorName(DefaultValueUtils.defaultIfBlank(request.getDoctorName(), ""));
        entity.setClinicTime(DefaultValueUtils.defaultIfBlank(request.getTimeSlot(), ""));
        entity.setSource(DefaultValueUtils.defaultIfBlank(request.getSource(), DEFAULT_SOURCE));
        entity.setStatus(AppointmentStatus.PENDING_PAY.dbValue());
        entity.setFeeAmount(DefaultValueUtils.defaultIfNull(request.getFeeAmount(), new BigDecimal("30")));
        aptAppointmentMapper.insert(entity);
        entity.setAppointmentNo(resolveAppointmentNo(entity.getId()));
        aptAppointmentMapper.updateById(entity);
        return toAppointmentVO(entity);
    }

    /**
     * 支付预约单�?
     *
     * @param id 预约单编�?
     * @return 支付后的预约�?
     */
    @Transactional
    public AppointmentVO pay(Long id) {
        TokenPrincipalContext.ensureBusinessTenantContext("预约模块操作缺少有效租户上下�?);
        log.info("支付预约单，appointmentId={}", id);
        AptAppointmentEntity entity = requireActiveAppointment(id);
        if (STATUS_PAID.equals(entity.getStatus()) || STATUS_CHECKED_IN.equals(entity.getStatus()) || STATUS_COMPLETED.equals(entity.getStatus())) {
            log.info("预约单无需重复支付，appointmentId={}，status={}", id, entity.getStatus());
            return toAppointmentVO(entity);
        }
        if (!STATUS_PENDING_PAY.equals(entity.getStatus())) {
            throw new BizException(409, "预约单当前状态不允许支付");
        }
        entity.setStatus(STATUS_PAID);
        entity.setPayTime(LocalDateTime.now());
        aptAppointmentMapper.updateById(entity);
        return toAppointmentVO(entity);
    }

    /**
     * 预约签到�?
     *
     * @param id 预约单编�?
     * @return 签到后的预约�?
     */
    @Transactional
    public AppointmentVO checkIn(Long id) {
        TokenPrincipalContext.ensureBusinessTenantContext("预约模块操作缺少有效租户上下�?);
        log.info("预约签到，appointmentId={}", id);
        AptAppointmentEntity entity = requireActiveAppointment(id);
        if (STATUS_CHECKED_IN.equals(entity.getStatus()) || STATUS_COMPLETED.equals(entity.getStatus())) {
            log.info("预约单无需重复签到，appointmentId={}，status={}", id, entity.getStatus());
            return toAppointmentVO(entity);
        }
        if (!STATUS_PAID.equals(entity.getStatus())) {
            throw new BizException(409, "预约单当前状态不允许签到");
        }
        entity.setStatus(STATUS_CHECKED_IN);
        entity.setCheckInTime(LocalDateTime.now());
        aptAppointmentMapper.updateById(entity);
        markNumberSourceUsed(entity.getNumberSourceId());
        return toAppointmentVO(entity);
    }

    /**
     * 抢便民门诊预约单�?
     *
     * @param id 预约单编�?
     * @param doctorId 医生编号
     * @return 抢单是否成功
     */
    @Transactional
    public Boolean grab(Long id, Long doctorId) {
        TokenPrincipalContext.ensureBusinessTenantContext("预约模块操作缺少有效租户上下�?);
        if (doctorId == null) {
            throw new BizException(400, "医生编号不能为空");
        }
        log.info("抢便民门诊预约单，appointmentId={}，doctorId={}", id, doctorId);
        AptAppointmentEntity entity = requireActiveAppointment(id);
        if (STATUS_CHECKED_IN.equals(entity.getStatus()) || STATUS_GRABBED.equals(entity.getStatus())) {
            log.info("预约单无需重复抢单，appointmentId={}，status={}", id, entity.getStatus());
            return true;
        }
        if (STATUS_CANCELLED.equals(entity.getStatus()) || STATUS_COMPLETED.equals(entity.getStatus())) {
            throw new BizException(409, "预约单当前状态不允许抢单");
        }
        entity.setDoctorId(doctorId);
        entity.setStatus(STATUS_GRABBED);
        return aptAppointmentMapper.updateById(entity) > 0;
    }

    /**
     * 使用 Redis 分布式锁，按需生成并锁定一个号源�?
     * <p>不再从预生成�?AVAILABLE 记录中选取，而是校验释放容量后直�?INSERT 一�?LOCKED 记录�?/p>
     *
     * @param scheduleId 排班编号
     * @return 锁定后的号源
     */
    @Transactional
    public NumberSourceVO lockNumberSource(Long scheduleId) {
        TokenPrincipalContext.ensureBusinessTenantContext("预约模块操作缺少有效租户上下�?);
        String lockKey = "hlw:lock:number:" + scheduleId;
        log.info("尝试获取号源分布式锁，scheduleId={}", scheduleId);
        try {
            if (!redisLockService.tryLock(lockKey, 5, 10, TimeUnit.SECONDS)) {
                throw new BizException(409, "号源锁定繁忙，请稍后重试");
            }
            // 统计当前排班已占号源数（LOCKED + USED�?
            Long usedCount = aptNumberSourceMapper.selectCount(new LambdaQueryWrapper<AptNumberSourceEntity>()
                .eq(AptNumberSourceEntity::getScheduleId, scheduleId)
                .in(AptNumberSourceEntity::getStatus, NUMBER_STATUS_LOCKED, NUMBER_STATUS_USED));
            // 查询放号配置总容量（SUM(release_count)�?
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
            // 计算下一个序号：已占序列最大�?+ 1
            int nextSeq = aptNumberSourceMapper.selectList(new LambdaQueryWrapper<AptNumberSourceEntity>()
                    .eq(AptNumberSourceEntity::getScheduleId, scheduleId))
                .stream()
                .map(AptNumberSourceEntity::getNumberSeq)
                .max(Integer::compareTo)
                .orElse(0) + 1;
            // 直接插入一�?LOCKED 号源
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
            throw new BizException(409, "号源锁定被中�?);
        } finally {
            redisLockService.unlock(lockKey);
        }
    }

    /**
     * 创建放号配置�?
     *
     * @param request 放号配置请求
     * @return 创建后的配置
     */
    @Transactional
    public ReleaseConfigVO createReleaseConfig(CreateReleaseConfigRequest request) {
        TokenPrincipalContext.ensureBusinessTenantContext("预约模块操作缺少有效租户上下�?);
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
     * 创建放号配置并释放号源（内部接口），使用当前时间作为放号时间�?
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
     * 构造号源激活查询条件�?
     *
     * @return 查询条件
    }

    /**
     * 查询预约单并校验存在�?
     *
     * @param id 预约单编�?
     * @return 预约单实�?
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
     * 查询首个可用号源�?
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
     * 标记号源已使用�?
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
     * 按放号配置生成可用号源（已改为按需生成，占号时�?INSERT）�?
     * <p>放号配置写入 release_config 后不再预生成号源记录�?
     * 实际号源�?lockNumberSource 时按容量校验并动态插入�?/p>
     *
     * @param scheduleId 排班编号
     * @param releaseCount 放号数量（当前仅用于日志记录，不再插入号源）
     */
    private void releaseNumberSources(Long scheduleId, Integer releaseCount) {
        log.info("放号配置已记录，scheduleId={}，releaseCount={}，号源将在占号时按需生成", scheduleId, releaseCount);
    }

    /**
     * 转换预约单展示对象�?
     *
     * @param entity 预约单实�?
     * @return 预约单展示对�?
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
     * 内部查询预约单基本信息（�?consult 模块 Feign 调用）�?
     *
     * @param id 预约单编�?
     * @return 预约单内部响�?
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
            DefaultValueUtils.defaultIfNull(entity.getFeeAmount(), BigDecimal.ZERO).toPlainString()
        );
    }

    /**
     * 转换号源展示对象�?
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
     * 转换放号配置展示对象�?
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
     * 生成预约单号�?
     *
     * @param id 预约单编�?
     * @return 预约单号
     */
    private String resolveAppointmentNo(Long id) {
        return "YY" + LocalDate.now().format(DATE_FORMATTER) + String.format("%04d", id);
    }

    /**
     * 解析日期时间�?
     *
     * @param value 日期时间字符�?
     * @return 日期时间
     */
    private LocalDateTime parseDateTime(String value) {
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new BizException(400, "放号时间格式必须�?yyyy-MM-dd HH:mm:ss");
        }
    }

}