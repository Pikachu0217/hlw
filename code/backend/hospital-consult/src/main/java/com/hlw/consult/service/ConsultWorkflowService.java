package com.hlw.consult.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.core.domain.R;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.tenant.TokenPrincipalContext;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.consult.client.AppointmentFeignClient;
import com.hlw.consult.client.PatientFeignClient;
import com.hlw.consult.client.resp.InternalAppointmentResp;
import com.hlw.consult.client.resp.InternalPatientResp;
import com.hlw.consult.dto.AcceptConsultRequest;
import com.hlw.consult.dto.CreateConsultRequest;
import com.hlw.consult.entity.ConConsultEntity;
import com.hlw.consult.entity.ConMessageEntity;
import com.hlw.consult.mapper.ConConsultMapper;
import com.hlw.consult.mapper.ConMessageMapper;
import com.hlw.consult.vo.ConsultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 问诊工作流服务，负责问诊创建、接单、完成和延长状态落库。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultWorkflowService {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter CONSULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final long DEFAULT_DURATION_LIMIT = 30;
    private static final int EXTEND_MINUTES = 15;
    private static final String DEFAULT_CONSULT_TYPE = "text_and_image_consultation";

    /** 问诊单数据访问组件。 */
    private final ConConsultMapper conConsultMapper;
    /** 问诊消息数据访问组件。 */
    private final ConMessageMapper conMessageMapper;
    /** 患者服务内部客户端。 */
    private final PatientFeignClient patientFeignClient;
    /** 预约服务内部客户端。 */
    private final AppointmentFeignClient appointmentFeignClient;

    /**
     * 查询问诊单列表。
     *
     * @return 问诊单展示列表
     */
    public List<ConsultVO> listConsults() {
        log.info("查询问诊单列表");
        return conConsultMapper.selectList(activeConsultWrapper())
            .stream()
            .sorted(Comparator.comparing(ConConsultEntity::getId))
            .map(this::toConsultVO)
            .toList();
    }

    /**
     * 创建问诊单并记录首条主诉消息。
     *
     * @param request 问诊创建请求
     * @return 创建后的问诊单
     */
    @Transactional
    public ConsultVO createConsult(CreateConsultRequest request) {
        ensureBusinessTenantContext("问诊模块操作缺少有效租户上下文");
        InternalPatientResp currentPatient = request.getPatientId() == null ? resolveCurrentPatient() : null;
        Long patientId = defaultLong(request.getPatientId(), 0);
        Long doctorId = defaultLong(request.getDoctorId(), 0);
        String consultType = defaultIfBlank(request.getType(), DEFAULT_CONSULT_TYPE);
        String patientName = defaultIfBlank(request.getPatientName(), currentPatient == null ? "" : currentPatient.patientName());
        String doctorName = defaultIfBlank(request.getDoctorName(), "");
        String channel = defaultIfBlank(request.getChannel(), channelName(consultType));
        String chiefComplaint = defaultIfBlank(request.getChiefComplaint(), "");
        BigDecimal feeAmount = defaultDecimal(request.getFeeAmount(), BigDecimal.ZERO);
        log.info("创建问诊单，patientId={}，doctorId={}，consultType={}", patientId, doctorId, consultType);

        ConConsultEntity entity = new ConConsultEntity();
        entity.setPatientId(patientId);
        entity.setDoctorId(doctorId);
        entity.setConsultType(consultType);
        entity.setConsultNo("");
        entity.setPatientName(patientName);
        entity.setDoctorName(doctorName);
        entity.setChannel(channel);
        entity.setStatus(ConsultDisplayStatus.WAITING);
        entity.setFeeAmount(feeAmount);
        entity.setDurationLimit(DEFAULT_DURATION_LIMIT);
        entity.setRemainingSeconds(DEFAULT_DURATION_LIMIT * 60);
        entity.setUpdatedAt(currentDisplayTime());
        conConsultMapper.insert(entity);
        entity.setConsultNo(resolveConsultNo(entity.getId()));
        conConsultMapper.updateById(entity);
        insertChiefComplaintMessage(entity.getId(), patientId, chiefComplaint);
        return toConsultVO(entity);
    }

    /**
     * 从已支付预约单创建问诊，幂等（已存在关联问诊时直接返回）。
     *
     * @param appointmentId 预约单编号
     * @return 问诊单
     */
    @Transactional
    public ConsultVO createConsultFromAppointment(Long appointmentId) {
        ensureBusinessTenantContext("问诊模块操作缺少有效租户上下文");
        log.info("从预约单创建问诊，appointmentId={}", appointmentId);

        // 幂等：已存在关联问诊时直接返回
        ConConsultEntity existing = conConsultMapper.selectOne(
            new LambdaQueryWrapper<ConConsultEntity>()
                .eq(ConConsultEntity::getAppointmentId, appointmentId)
                .last("limit 1")
        );
        if (existing != null) {
            log.info("预约单已关联问诊单，consultId={}", existing.getId());
            return toConsultVO(existing);
        }

        // 查询预约单，获取医生、患者和费用信息
        R<InternalAppointmentResp> aptResponse = appointmentFeignClient.getAppointment(appointmentId);
        InternalAppointmentResp appointment = (aptResponse == null || aptResponse.code() != 200 || aptResponse.data() == null)
            ? null : aptResponse.data();
        if (appointment == null) {
            log.warn("预约单不存在或查询失败，appointmentId={}", appointmentId);
            throw new BizException(404, "预约单不存在");
        }
        Long doctorId = appointment.doctorId();
        String doctorName = appointment.doctorName();
        Long patientId = appointment.patientId();
        String patientName = appointment.patientName();
        String feeAmount = appointment.feeAmount();

        log.info("从预约单创建问诊，appointmentId={}，patientId={}，patientName={}，doctorId={}，doctorName={}，feeAmount={}",
            appointmentId, patientId, patientName, doctorId, doctorName, feeAmount);

        ConConsultEntity entity = new ConConsultEntity();
        entity.setPatientId(patientId);
        entity.setDoctorId(doctorId);
        entity.setAppointmentId(appointmentId);
        entity.setConsultType(DEFAULT_CONSULT_TYPE);
        entity.setConsultNo("");
        entity.setPatientName(patientName);
        entity.setDoctorName(doctorName);
        entity.setChannel(channelName(DEFAULT_CONSULT_TYPE));
        entity.setStatus(ConsultDisplayStatus.WAITING);
        entity.setPayStatus("PAID");
        entity.setFeeAmount(new BigDecimal(feeAmount));
        entity.setDurationLimit(DEFAULT_DURATION_LIMIT);
        entity.setRemainingSeconds(DEFAULT_DURATION_LIMIT * 60);
        entity.setUpdatedAt(currentDisplayTime());
        conConsultMapper.insert(entity);
        entity.setConsultNo(resolveConsultNo(entity.getId()));
        conConsultMapper.updateById(entity);
        log.info("已从预约单创建问诊，consultId={}，appointmentId={}", entity.getId(), appointmentId);
        return toConsultVO(entity);
    }

    /**
     * 医生接单问诊。
     *
     * @param id 问诊编号
     * @param request 接单请求
     * @return 接单后的问诊单
     */
    @Transactional
    public ConsultVO accept(Long id, AcceptConsultRequest request) {
        ensureBusinessTenantContext("问诊模块操作缺少有效租户上下文");
        ConConsultEntity consult = requireActiveConsult(id);
        Long doctorId = request == null ? consult.getDoctorId() : defaultLong(request.getDoctorId(), consult.getDoctorId());
        log.info("医生接单问诊，consultId={}，doctorId={}", id, doctorId);
        if (ConsultDisplayStatus.IN_PROGRESS.equals(consult.getStatus()) || ConsultDisplayStatus.EXTENDED.equals(consult.getStatus())) {
            log.info("问诊单无需重复接单，consultId={}，status={}", id, consult.getStatus());
            return toConsultVO(consult);
        }
        if (!ConsultDisplayStatus.WAITING.equals(consult.getStatus())) {
            throw new BizException(409, "问诊单当前状态不允许接单");
        }
        consult.setDoctorId(doctorId);
        consult.setStatus(ConsultDisplayStatus.IN_PROGRESS);
        consult.setStartTime(LocalDateTime.now());
        consult.setDurationLimit(consult.getDurationLimit() == null || consult.getDurationLimit() <= 0 ? DEFAULT_DURATION_LIMIT : consult.getDurationLimit());
        consult.setRemainingSeconds(consult.getRemainingSeconds() == null || consult.getRemainingSeconds() <= 0 ? DEFAULT_DURATION_LIMIT * 60 : consult.getRemainingSeconds());
        consult.setUpdatedAt(currentDisplayTime());
        conConsultMapper.updateById(consult);
        return toConsultVO(consult);
    }

    /**
     * 完成问诊。
     *
     * @param id 问诊编号
     * @return 完成后的问诊单
     */
    @Transactional
    public ConsultVO complete(Long id) {
        ensureBusinessTenantContext("问诊模块操作缺少有效租户上下文");
        log.info("完成问诊，consultId={}", id);
        ConConsultEntity consult = requireActiveConsult(id);
        if (ConsultDisplayStatus.FINISHED.equals(consult.getStatus())) {
            log.info("问诊单无需重复完成，consultId={}", id);
            return toConsultVO(consult);
        }
        if (ConsultDisplayStatus.CANCELLED.equals(consult.getStatus()) || ConsultDisplayStatus.TIMEOUT.equals(consult.getStatus())) {
            throw new BizException(409, "问诊单当前状态不允许完成");
        }
        consult.setStatus(ConsultDisplayStatus.FINISHED);
        consult.setRemainingSeconds(0);
        consult.setEndTime(LocalDateTime.now());
        consult.setUpdatedAt(currentDisplayTime());
        conConsultMapper.updateById(consult);
        return toConsultVO(consult);
    }

    /**
     * 延长问诊服务时长。
     *
     * @param id 问诊编号
     * @return 延长后的问诊单
     */
    @Transactional
    public ConsultVO extend(Long id) {
        ensureBusinessTenantContext("问诊模块操作缺少有效租户上下文");
        log.info("延长问诊，consultId={}，extendMinutes={}", id, EXTEND_MINUTES);
        ConConsultEntity consult = requireActiveConsult(id);
        if (!ConsultDisplayStatus.IN_PROGRESS.equals(consult.getStatus()) && !ConsultDisplayStatus.EXTENDED.equals(consult.getStatus())) {
            throw new BizException(409, "问诊单当前状态不允许延长");
        }
        consult.setStatus(ConsultDisplayStatus.EXTENDED);
        consult.setDurationLimit(defaultInt(consult.getDurationLimit(), 0) + EXTEND_MINUTES);
        consult.setRemainingSeconds(defaultInt(consult.getRemainingSeconds(), 0) + EXTEND_MINUTES * 60);
        consult.setUpdatedAt(currentDisplayTime());
        conConsultMapper.updateById(consult);
        return toConsultVO(consult);
    }

    /**
     * 写入首条主诉消息。
     *
     * @param consultId 问诊编号
     * @param patientId 患者编号
     * @param chiefComplaint 主诉内容
     */
    private void insertChiefComplaintMessage(Long consultId, Long patientId, String chiefComplaint) {
        if (chiefComplaint.isBlank()) {
            return;
        }
        ConMessageEntity message = new ConMessageEntity();
        message.setConsultId(consultId);
        message.setSenderId(patientId);
        message.setSenderType(ConsultParticipantType.PATIENT);
        message.setContent(chiefComplaint);
        message.setContentType(ConsultMessageType.TEXT);
        message.setIsRead(0);
        conMessageMapper.insert(message);
    }

    /**
     * 解析当前登录患者档案。
     *
     * @return 内部患者档案，演示兼容场景下可能返回 null
     */
    private InternalPatientResp resolveCurrentPatient() {
        if (TokenPrincipalContext.get() == null || TokenPrincipalContext.get().getBusinessUserId() == null || TokenPrincipalContext.get().getBusinessUserId().isBlank()) {
            log.warn("创建问诊时登录用户为空，使用默认患者信息");
            return null;
        }
        Long tenantId = TokenPrincipalContext.get().getTenantId();
        String userId = TokenPrincipalContext.get().getBusinessUserId();
        R<InternalPatientResp> response = patientFeignClient.findByUser(tenantId, userId);
        if (response == null || response.code() != 200 || response.data() == null) {
            log.warn("创建问诊时未查询到当前患者档案，tenantId={}，userId={}", tenantId, userId);
            throw new BizException(403, "当前登录账号未绑定患者档案");
        }
        return response.data();
    }

    /**
     * 构造激活问诊查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<ConConsultEntity> activeConsultWrapper() {
        return new LambdaQueryWrapper<ConConsultEntity>();
    }

    /**
     * 校验当前请求处于有效业务租户上下文。
     *
     * @param message 不满足条件时的错误消息
     */
    private void ensureBusinessTenantContext(String message) {
        Long tenantId = TokenPrincipalContext.get().getTenantId();
        if (tenantId == null || tenantId <= 0L || TokenPrincipalContext.get().getPlatformRequest()) {
            throw new BizException(403, message);
        }
    }

    /**
     * 查询问诊单并校验存在。
     *
     * @param id 问诊编号
     * @return 问诊单实体
     */
    private ConConsultEntity requireActiveConsult(Long id) {
        ConConsultEntity entity = conConsultMapper.selectOne(new LambdaQueryWrapper<ConConsultEntity>()
            .eq(ConConsultEntity::getId, id)
            .last("limit 1"));
        if (entity == null) {
            throw new BizException(404, "问诊单不存在");
        }
        return entity;
    }

    /**
     * 转换问诊单展示对象。
     *
     * @param entity 问诊单实体
     * @return 问诊单展示对象
     */
    private ConsultVO toConsultVO(ConConsultEntity entity) {
        ConsultVO vo = new ConsultVO();
        vo.setId(entity.getId());
        vo.setConsultNo(defaultIfBlank(entity.getConsultNo(), resolveConsultNo(entity.getId())));
        vo.setPatientName(defaultIfBlank(entity.getPatientName(), ""));
        vo.setDoctorName(defaultIfBlank(entity.getDoctorName(), ""));
        vo.setDoctorId(entity.getDoctorId());
        vo.setChannel(defaultIfBlank(entity.getChannel(), channelName(entity.getConsultType())));
        vo.setStatus(ConsultDisplayStatus.labelOf(defaultIfBlank(entity.getStatus(), ConsultDisplayStatus.WAITING)));
        vo.setPayStatus(defaultIfBlank(entity.getPayStatus(), "UNPAID"));
        vo.setAppointmentId(entity.getAppointmentId());
        vo.setFeeAmount(entity.getFeeAmount() == null ? "0.00" : entity.getFeeAmount().toPlainString());
        vo.setRemainingSeconds(entity.getRemainingSeconds());
        vo.setUpdatedAt(defaultIfBlank(entity.getUpdatedAt(), ""));
        return vo;
    }

    /**
     * 生成问诊单号。
     *
     * @param id 问诊编号
     * @return 问诊单号
     */
    private String resolveConsultNo(Long id) {
        return "ZX" + LocalDate.now().format(CONSULT_DATE_FORMATTER) + String.format("%04d", id);
    }

    /**
     * 按问诊类型转换展示渠道。
     *
     * @param consultType 问诊类型
     * @return 展示渠道
     */
    private String channelName(String consultType) {
        if ("VIDEO".equalsIgnoreCase(consultType)) {
            return "视频";
        }
        return "图文";
    }

    /**
     * 获取当前展示时间。
     *
     * @return 时分展示值
     */
    private String currentDisplayTime() {
        return LocalTime.now().format(TIME_FORMATTER);
    }

    /**
     * 设置默认字符串。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后的字符串
     */
    private String defaultIfBlank(String value, String defaultValue) {
        return DefaultValueUtils.defaultIfBlank(value, defaultValue);
    }

    /**
     * 设置默认长整型。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后的长整型
     */
    private Long defaultLong(Long value, Long defaultValue) {
        return DefaultValueUtils.defaultIfNull(value, defaultValue);
    }

    /**
     * 设置默认整型。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后的整型
     */
    private int defaultInt(Integer value, int defaultValue) {
        return DefaultValueUtils.defaultIfNull(value, defaultValue);
    }

    /**
     * 设置默认金额。
     *
     * @param value 原始金额
     * @param defaultValue 默认金额
     * @return 处理后的金额
     */
    private BigDecimal defaultDecimal(BigDecimal value, BigDecimal defaultValue) {
        return DefaultValueUtils.defaultIfNull(value, defaultValue);
    }
}
