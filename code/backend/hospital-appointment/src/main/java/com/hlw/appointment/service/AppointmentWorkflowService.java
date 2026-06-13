package com.hlw.appointment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hlw.appointment.dto.CreateAppointmentRequest;
import com.hlw.appointment.dto.CreateReleaseConfigRequest;
import com.hlw.appointment.entity.AptAppointmentEntity;
import com.hlw.appointment.entity.AptNumberSourceEntity;
import com.hlw.appointment.entity.AptReleaseConfigEntity;
import com.hlw.appointment.mapper.AptAppointmentMapper;
import com.hlw.appointment.mapper.AptNumberSourceMapper;
import com.hlw.appointment.mapper.AptReleaseConfigMapper;
import com.hlw.appointment.vo.AppointmentVO;
import com.hlw.appointment.vo.NumberSourceVO;
import com.hlw.appointment.vo.ReleaseConfigVO;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

/**
 * 预约工作流服务，负责预约单、号源锁定和放号配置业务编排。
 */
@Service
@RequiredArgsConstructor
public class AppointmentWorkflowService {
    private static final Logger log = LoggerFactory.getLogger(AppointmentWorkflowService.class);
    private static final Long DEFAULT_PATIENT_ID = 1L;
    private static final Long DEFAULT_DOCTOR_ID = 1L;
    private static final Long DEFAULT_DEPARTMENT_ID = 10L;
    private static final Long DEFAULT_SCHEDULE_ID = 1L;
    private static final String DEFAULT_PATIENT_NAME = "赵晓岚";
    private static final String DEFAULT_DOCTOR_NAME = "陈知衡";
    private static final String DEFAULT_CLINIC_TIME = "2026-06-13 上午";
    private static final String DEFAULT_SOURCE = "小程序";
    private static final String DEFAULT_APPOINTMENT_TYPE = "普通门诊";
    private static final String STATUS_PENDING_PAY = "待支付";
    private static final String STATUS_PAID = "已支付";
    private static final String STATUS_CHECKED_IN = "已签到";
    private static final String STATUS_COMPLETED = "已完成";
    private static final String STATUS_CANCELLED = "已取消";
    private static final String STATUS_GRABBED = "已接单";
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

    /**
     * 查询预约单列表。
     *
     * @return 预约单展示列表
     */
    public List<AppointmentVO> listAppointments() {
        log.info("查询预约单列表");
        return aptAppointmentMapper.selectList(activeAppointmentWrapper())
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
        return aptNumberSourceMapper.selectList(activeNumberSourceWrapper())
            .stream()
            .sorted(Comparator.comparing(AptNumberSourceEntity::getScheduleId)
                .thenComparing(AptNumberSourceEntity::getNumberSeq)
                .thenComparing(AptNumberSourceEntity::getId))
            .map(this::toNumberSourceVO)
            .toList();
    }

    /**
     * 创建预约单。
     *
     * @param request 预约创建请求
     * @return 创建后的预约单
     */
    @Transactional
    public AppointmentVO createAppointment(CreateAppointmentRequest request) {
        ensureBusinessTenantContext("预约模块操作缺少有效租户上下文");
        Long scheduleId = defaultLong(request.getScheduleId(), DEFAULT_SCHEDULE_ID);
        log.info("创建预约单，patientId={}，doctorId={}，scheduleId={}",
            request.getPatientId(), request.getDoctorId(), scheduleId);
        NumberSourceVO numberSource = lockNumberSource(scheduleId);
        AptAppointmentEntity entity = new AptAppointmentEntity();
        entity.setPatientId(defaultLong(request.getPatientId(), DEFAULT_PATIENT_ID));
        entity.setDoctorId(defaultLong(request.getDoctorId(), DEFAULT_DOCTOR_ID));
        entity.setDepartmentId(defaultLong(request.getDepartmentId(), DEFAULT_DEPARTMENT_ID));
        entity.setScheduleId(scheduleId);
        entity.setNumberSourceId(numberSource.getId());
        entity.setAppointmentType(defaultIfBlank(request.getAppointmentType(), DEFAULT_APPOINTMENT_TYPE));
        entity.setAppointmentNo("");
        entity.setPatientName(defaultIfBlank(request.getPatientName(), DEFAULT_PATIENT_NAME));
        entity.setDoctorName(defaultIfBlank(request.getDoctorName(), DEFAULT_DOCTOR_NAME));
        entity.setClinicTime(defaultIfBlank(request.getTimeSlot(), DEFAULT_CLINIC_TIME));
        entity.setSource(defaultIfBlank(request.getSource(), DEFAULT_SOURCE));
        entity.setStatus(STATUS_PENDING_PAY);
        entity.setFeeAmount(defaultDecimal(request.getFeeAmount(), new BigDecimal("30")));
        entity.setDeleted(0);
        aptAppointmentMapper.insert(entity);
        entity.setAppointmentNo(resolveAppointmentNo(entity.getId()));
        aptAppointmentMapper.updateById(entity);
        return toAppointmentVO(entity);
    }

    /**
     * 支付预约单。
     *
     * @param id 预约单编号
     * @return 支付后的预约单
     */
    @Transactional
    public AppointmentVO pay(Long id) {
        ensureBusinessTenantContext("预约模块操作缺少有效租户上下文");
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
     * 预约签到。
     *
     * @param id 预约单编号
     * @return 签到后的预约单
     */
    @Transactional
    public AppointmentVO checkIn(Long id) {
        ensureBusinessTenantContext("预约模块操作缺少有效租户上下文");
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
     * 抢便民门诊预约单。
     *
     * @param id 预约单编号
     * @param doctorId 医生编号
     * @return 抢单是否成功
     */
    @Transactional
    public Boolean grab(Long id, Long doctorId) {
        ensureBusinessTenantContext("预约模块操作缺少有效租户上下文");
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
     * 锁定一个可用号源。
     *
     * @param scheduleId 排班编号
     * @return 锁定后的号源
     */
    @Transactional
    public NumberSourceVO lockNumberSource(Long scheduleId) {
        ensureBusinessTenantContext("预约模块操作缺少有效租户上下文");
        log.info("锁定号源，scheduleId={}", scheduleId);
        AptNumberSourceEntity entity = requireFirstAvailableNumberSource(scheduleId);
        int updated = aptNumberSourceMapper.update(null, new LambdaUpdateWrapper<AptNumberSourceEntity>()
            .eq(AptNumberSourceEntity::getId, entity.getId())
            .eq(AptNumberSourceEntity::getStatus, NUMBER_STATUS_AVAILABLE)
            .eq(AptNumberSourceEntity::getDeleted, 0)
            .set(AptNumberSourceEntity::getStatus, NUMBER_STATUS_LOCKED)
            .set(AptNumberSourceEntity::getLockTime, LocalDateTime.now()));
        if (updated == 0) {
            throw new BizException(409, "号源已被锁定");
        }
        entity.setStatus(NUMBER_STATUS_LOCKED);
        entity.setLockTime(LocalDateTime.now());
        return toNumberSourceVO(entity);
    }

    /**
     * 创建放号配置。
     *
     * @param request 放号配置请求
     * @return 创建后的配置
     */
    @Transactional
    public ReleaseConfigVO createReleaseConfig(CreateReleaseConfigRequest request) {
        ensureBusinessTenantContext("预约模块操作缺少有效租户上下文");
        Integer releaseCount = defaultInt(request.getReleaseCount(), 10);
        LocalDateTime releaseTime = parseDateTime(request.getReleaseAt());
        log.info("创建放号配置，scheduleId={}，releaseAt={}，releaseCount={}",
            request.getScheduleId(), request.getReleaseAt(), releaseCount);
        AptReleaseConfigEntity entity = new AptReleaseConfigEntity();
        entity.setScheduleId(request.getScheduleId());
        entity.setReleaseTime(releaseTime);
        entity.setReleaseCount(releaseCount);
        entity.setStatus(defaultIfBlank(request.getStatus(), DEFAULT_RELEASE_STATUS));
        entity.setDeleted(0);
        aptReleaseConfigMapper.insert(entity);
        releaseNumberSources(request.getScheduleId(), releaseCount);
        return toReleaseConfigVO(entity);
    }

    /**
     * 构造预约单激活查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<AptAppointmentEntity> activeAppointmentWrapper() {
        return new LambdaQueryWrapper<AptAppointmentEntity>().eq(AptAppointmentEntity::getDeleted, 0);
    }

    /**
     * 构造号源激活查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<AptNumberSourceEntity> activeNumberSourceWrapper() {
        return new LambdaQueryWrapper<AptNumberSourceEntity>().eq(AptNumberSourceEntity::getDeleted, 0);
    }

    /**
     * 校验当前请求处于有效业务租户上下文。
     *
     * @param message 不满足条件时的错误消息
     */
    private void ensureBusinessTenantContext(String message) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId <= 0L || TenantContext.isPlatformRequest()) {
            throw new BizException(403, message);
        }
    }

    /**
     * 查询预约单并校验存在。
     *
     * @param id 预约单编号
     * @return 预约单实体
     */
    private AptAppointmentEntity requireActiveAppointment(Long id) {
        AptAppointmentEntity entity = aptAppointmentMapper.selectOne(new LambdaQueryWrapper<AptAppointmentEntity>()
            .eq(AptAppointmentEntity::getDeleted, 0)
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
            .eq(AptNumberSourceEntity::getDeleted, 0)
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
            .eq(AptNumberSourceEntity::getDeleted, 0)
            .set(AptNumberSourceEntity::getStatus, NUMBER_STATUS_USED));
    }

    /**
     * 按放号配置生成可用号源。
     *
     * @param scheduleId 排班编号
     * @param releaseCount 放号数量
     */
    private void releaseNumberSources(Long scheduleId, Integer releaseCount) {
        int startSeq = aptNumberSourceMapper.selectList(new LambdaQueryWrapper<AptNumberSourceEntity>()
                .eq(AptNumberSourceEntity::getDeleted, 0)
                .eq(AptNumberSourceEntity::getScheduleId, scheduleId))
            .stream()
            .map(AptNumberSourceEntity::getNumberSeq)
            .max(Integer::compareTo)
            .orElse(0);
        for (int index = 1; index <= releaseCount; index++) {
            AptNumberSourceEntity numberSource = new AptNumberSourceEntity();
            numberSource.setScheduleId(scheduleId);
            numberSource.setNumberSeq(startSeq + index);
            numberSource.setStatus(NUMBER_STATUS_AVAILABLE);
            numberSource.setDeleted(0);
            aptNumberSourceMapper.insert(numberSource);
        }
    }

    /**
     * 转换预约单展示对象。
     *
     * @param entity 预约单实体
     * @return 预约单展示对象
     */
    private AppointmentVO toAppointmentVO(AptAppointmentEntity entity) {
        AppointmentVO vo = new AppointmentVO();
        vo.setKey(String.valueOf(entity.getId()));
        vo.setId(entity.getId());
        vo.setAppointmentNo(defaultIfBlank(entity.getAppointmentNo(), resolveAppointmentNo(entity.getId())));
        vo.setPatientName(defaultIfBlank(entity.getPatientName(), ""));
        vo.setDoctorName(defaultIfBlank(entity.getDoctorName(), ""));
        vo.setClinicTime(defaultIfBlank(entity.getClinicTime(), ""));
        vo.setSource(defaultIfBlank(entity.getSource(), DEFAULT_SOURCE));
        vo.setStatus(defaultIfBlank(entity.getStatus(), STATUS_PENDING_PAY));
        vo.setFeeAmount(defaultDecimal(entity.getFeeAmount(), BigDecimal.ZERO).toPlainString());
        return vo;
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

    /**
     * 设置默认字符串。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后的字符串
     */
    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    /**
     * 设置默认长整型。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后的长整型
     */
    private Long defaultLong(Long value, Long defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * 设置默认整型。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后的整型
     */
    private Integer defaultInt(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * 设置默认金额。
     *
     * @param value 原始金额
     * @param defaultValue 默认金额
     * @return 处理后的金额
     */
    private BigDecimal defaultDecimal(BigDecimal value, BigDecimal defaultValue) {
        return value == null ? defaultValue : value;
    }
}
