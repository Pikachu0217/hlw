package com.hlw.prescription.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.tenant.TokenPrincipalContext;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.common.mq.enums.MessageQueueEnum;
import com.hlw.common.mq.service.producer.MessageQueueProducer;
import com.hlw.prescription.dto.ApprovePrescriptionRequest;
import com.hlw.prescription.dto.CreatePrescriptionRequest;
import com.hlw.prescription.dto.RejectPrescriptionRequest;
import com.hlw.prescription.entity.PrePrescriptionEntity;
import com.hlw.prescription.entity.PrePrescriptionItemEntity;
import com.hlw.prescription.mapper.PrePrescriptionItemMapper;
import com.hlw.prescription.mapper.PrePrescriptionMapper;
import com.hlw.prescription.vo.PrescriptionVO;
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
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * 处方工作流服务，负责处方草稿、提审、审核和驳回落库�?
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionWorkflowService {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter PRESCRIPTION_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final long DEFAULT_CONSULT_ID = 0;
    private static final long DEFAULT_PATIENT_ID = 0;
    private static final long DEFAULT_DOCTOR_ID = 0;
    private static final String DEFAULT_PATIENT_NAME = "";
    private static final String DEFAULT_DOCTOR_NAME = "";
    private static final long DEFAULT_PHARMACIST_ID = 0;
    private static final String STATUS_DRAFT = "草稿";
    private static final String STATUS_SUBMITTED = "待审�?;
    private static final String STATUS_AUDITED = "待发�?;
    private static final String STATUS_REJECTED = "已驳�?;

    /** 处方数据访问组件�?*/
    private final PrePrescriptionMapper prePrescriptionMapper;
    /** 处方药品明细数据访问组件�?*/
    private final PrePrescriptionItemMapper prePrescriptionItemMapper;
    /** 消息生产者�?*/
    private final MessageQueueProducer<String, Long> messageQueueProducer;

    /**
     * 查询处方列表�?
     *
     * @return 处方展示列表
     */
    public List<PrescriptionVO> listPrescriptions() {
        log.info("查询处方列表");
        return prePrescriptionMapper.selectList(new LambdaQueryWrapper<>())
            .stream()
            .sorted(Comparator.comparing(PrePrescriptionEntity::getId))
            .map(this::toPrescriptionVO)
            .toList();
    }

    /**
     * 创建处方草稿并写入药品明细�?
     *
     * @param request 创建请求
     * @return 创建后的处方
     */
    @Transactional
    public PrescriptionVO create(CreatePrescriptionRequest request) {
        TokenPrincipalContext.ensureBusinessTenantContext("处方模块操作缺少有效租户上下�?);
        Long consultId = DefaultValueUtils.defaultIfNull(request.getConsultId(), DEFAULT_CONSULT_ID);
        Long patientId = DefaultValueUtils.defaultIfNull(request.getPatientId(), DEFAULT_PATIENT_ID);
        Long doctorId = DefaultValueUtils.defaultIfNull(request.getDoctorId(), DEFAULT_DOCTOR_ID);
        String patientName = DefaultValueUtils.defaultIfBlank(request.getPatientName(), DEFAULT_PATIENT_NAME);
        String doctorName = DefaultValueUtils.defaultIfBlank(request.getDoctorName(), DEFAULT_DOCTOR_NAME);
        List<Long> drugIds = request.getDrugIds() == null
            ? List.of()
            : request.getDrugIds().stream().filter(Objects::nonNull).toList();
        int drugCount = drugIds.isEmpty() ? DefaultValueUtils.defaultIfNull(request.getDrugCount(), 1) : drugIds.size();
        String issuedAt = DefaultValueUtils.defaultIfBlank(request.getIssuedAt(), currentDisplayTime());
        log.info("创建处方草稿，consultId={}，patientId={}，doctorId={}，drugCount={}", consultId, patientId, doctorId, drugCount);

        PrePrescriptionEntity entity = new PrePrescriptionEntity();
        entity.setConsultId(consultId);
        entity.setPatientId(patientId);
        entity.setDoctorId(doctorId);
        entity.setPrescriptionNo("");
        entity.setPatientName(patientName);
        entity.setDoctorName(doctorName);
        entity.setDrugCount(drugCount);
        entity.setIssuedAt(issuedAt);
        entity.setStatus(STATUS_DRAFT);
        prePrescriptionMapper.insert(entity);
        entity.setPrescriptionNo(resolvePrescriptionNo(entity.getId()));
        prePrescriptionMapper.updateById(entity);
        insertItems(entity.getId(), drugIds);
        return toPrescriptionVO(entity);
    }

    /**
     * 提交处方进入待审方状态�?
     *
     * @param id 处方编号
     * @return 提交后的处方
     */
    @Transactional
    public PrescriptionVO submit(Long id) {
        TokenPrincipalContext.ensureBusinessTenantContext("处方模块操作缺少有效租户上下�?);
        log.info("提交处方，prescriptionId={}", id);
        PrePrescriptionEntity entity = requireActivePrescription(id);
        if (STATUS_SUBMITTED.equals(entity.getStatus())) {
            log.info("处方无需重复提交，prescriptionId={}", id);
            return toPrescriptionVO(entity);
        }
        if (!STATUS_DRAFT.equals(entity.getStatus())) {
            throw new BizException(409, "处方当前状态不允许提交");
        }
        entity.setStatus(STATUS_SUBMITTED);
        entity.setSubmitTime(LocalDateTime.now());
        prePrescriptionMapper.updateById(entity);
        return toPrescriptionVO(entity);
    }

    /**
     * 审核通过处方�?
     *
     * @param id 处方编号
     * @param request 审核请求
     * @return 审核后的处方
     */
    @Transactional
    public PrescriptionVO approve(Long id, ApprovePrescriptionRequest request) {
        TokenPrincipalContext.ensureBusinessTenantContext("处方模块操作缺少有效租户上下�?);
        Long pharmacistId = request == null ? DEFAULT_PHARMACIST_ID : DefaultValueUtils.defaultIfNull(request.getPharmacistId(), DEFAULT_PHARMACIST_ID);
        String remark = request == null ? "" : DefaultValueUtils.defaultIfBlank(request.getRemark(), "");
        log.info("审核通过处方，prescriptionId={}，pharmacistId={}", id, pharmacistId);
        PrePrescriptionEntity entity = requireActivePrescription(id);
        if (STATUS_AUDITED.equals(entity.getStatus())) {
            log.info("处方无需重复审核通过，prescriptionId={}", id);
            return toPrescriptionVO(entity);
        }
        if (!STATUS_SUBMITTED.equals(entity.getStatus())) {
            throw new BizException(409, "处方当前状态不允许审核通过");
        }
        entity.setStatus(STATUS_AUDITED);
        entity.setPharmacistId(pharmacistId);
        entity.setAuditRemark(remark);
        entity.setAuditTime(LocalDateTime.now());
        prePrescriptionMapper.updateById(entity);
        messageQueueProducer.send(MessageQueueEnum.QUEUE_PRESCRIPTION_AUDITED, "{\"prescriptionId\":" + id + "}");
        return toPrescriptionVO(entity);
    }

    /**
     * 驳回处方�?
     *
     * @param id 处方编号
     * @param request 驳回请求
     * @return 驳回后的处方
     */
    @Transactional
    public PrescriptionVO reject(Long id, RejectPrescriptionRequest request) {
        TokenPrincipalContext.ensureBusinessTenantContext("处方模块操作缺少有效租户上下�?);
        String remark = request == null ? "" : DefaultValueUtils.defaultIfBlank(request.getRemark(), "");
        log.info("驳回处方，prescriptionId={}，remark={}", id, remark);
        PrePrescriptionEntity entity = requireActivePrescription(id);
        if (STATUS_REJECTED.equals(entity.getStatus())) {
            log.info("处方无需重复驳回，prescriptionId={}", id);
            return toPrescriptionVO(entity);
        }
        if (!STATUS_SUBMITTED.equals(entity.getStatus())) {
            throw new BizException(409, "处方当前状态不允许驳回");
        }
        entity.setStatus(STATUS_REJECTED);
        entity.setAuditRemark(remark);
        entity.setAuditTime(LocalDateTime.now());
        prePrescriptionMapper.updateById(entity);
        return toPrescriptionVO(entity);
    }

    /**
     * 写入处方药品明细�?
     *
     * @param prescriptionId 处方编号
     * @param drugIds 药品编号列表
     */
    private void insertItems(Long prescriptionId, List<Long> drugIds) {
        if (drugIds.isEmpty()) {
            log.warn("处方无药品明细，prescriptionId={}", prescriptionId);
            return;
        }
        for (Long drugId : drugIds) {
            insertItem(prescriptionId, drugId, "药品" + drugId);
        }
    }

    /**
     * 写入单个处方药品明细�?
     *
     * @param prescriptionId 处方编号
     * @param drugId 药品编号
     * @param drugName 药品名称
     */
    private void insertItem(Long prescriptionId, Long drugId, String drugName) {
        PrePrescriptionItemEntity item = new PrePrescriptionItemEntity();
        item.setPrescriptionId(prescriptionId);
        item.setDrugId(drugId);
        item.setDrugName(drugName);
        item.setDosage("遵医�?);
        item.setFrequency("每日一�?);
        item.setQuantity(BigDecimal.ONE);
        item.setUsageNote("饭后服用");
        prePrescriptionItemMapper.insert(item);
    }
    }

    /**
     * 查询处方并校验存在�?
     *
     * @param id 处方编号
     * @return 处方实体
     */
    private PrePrescriptionEntity requireActivePrescription(Long id) {
        PrePrescriptionEntity entity = prePrescriptionMapper.selectOne(new LambdaQueryWrapper<PrePrescriptionEntity>()
            .eq(PrePrescriptionEntity::getId, id)
            .last("limit 1"));
        if (entity == null) {
            throw new BizException(404, "处方不存�?);
        }
        return entity;
    }

    /**
     * 转换处方展示对象�?
     *
     * @param entity 处方实体
     * @return 处方展示对象
     */
    private PrescriptionVO toPrescriptionVO(PrePrescriptionEntity entity) {
        PrescriptionVO vo = new PrescriptionVO();
        vo.setId(entity.getId());
        vo.setPrescriptionNo(DefaultValueUtils.defaultIfBlank(entity.getPrescriptionNo(), resolvePrescriptionNo(entity.getId())));
        vo.setPatientName(DefaultValueUtils.defaultIfBlank(entity.getPatientName(), ""));
        vo.setDoctorName(DefaultValueUtils.defaultIfBlank(entity.getDoctorName(), ""));
        vo.setDrugCount(DefaultValueUtils.defaultIfNull(entity.getDrugCount(), 0));
        vo.setIssuedAt(DefaultValueUtils.defaultIfBlank(entity.getIssuedAt(), ""));
        vo.setStatus(DefaultValueUtils.defaultIfBlank(entity.getStatus(), STATUS_DRAFT));
        vo.setRemark(DefaultValueUtils.defaultIfBlank(entity.getAuditRemark(), ""));
        return vo;
    }

    /**
     * 生成处方号�?
     *
     * @param id 处方编号
     * @return 处方�?
     */
    private String resolvePrescriptionNo(Long id) {
        return "CF" + LocalDate.now().format(PRESCRIPTION_DATE_FORMATTER) + String.format("%04d", id);
    }

    /**
     * 获取当前展示时间�?
     *
     * @return 时分展示�?
     */
    private String currentDisplayTime() {
        return LocalTime.now().format(TIME_FORMATTER);
    }

    /**
     * 设置默认字符串�?
     *
     * @param value 原始�?
     * @param defaultValue 默认�?
     * @return 处理后的字符�?
     */

    /**
     * 设置默认长整型�?
     *
     * @param value 原始�?
     * @param defaultValue 默认�?
     * @return 处理后的长整�?
     */

    /**
     * 设置默认整型�?
     *
     * @param value 原始�?
     * @param defaultValue 默认�?
     * @return 处理后的整型
     */
}