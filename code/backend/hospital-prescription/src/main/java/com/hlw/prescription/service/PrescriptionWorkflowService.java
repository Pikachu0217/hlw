package com.hlw.prescription.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.tenant.TenantContext;
import com.hlw.common.mq.core.MqProducer;
import com.hlw.common.mq.model.MqMessage;
import com.hlw.prescription.dto.ApprovePrescriptionRequest;
import com.hlw.prescription.dto.CreatePrescriptionRequest;
import com.hlw.prescription.dto.RejectPrescriptionRequest;
import com.hlw.prescription.entity.PrePrescriptionEntity;
import com.hlw.prescription.entity.PrePrescriptionItemEntity;
import com.hlw.prescription.mapper.PrePrescriptionItemMapper;
import com.hlw.prescription.mapper.PrePrescriptionMapper;
import com.hlw.prescription.vo.PrescriptionVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * 处方工作流服务，负责处方草稿、提审、审核和驳回落库。
 */
@Service
@RequiredArgsConstructor
public class PrescriptionWorkflowService {
    private static final Logger log = LoggerFactory.getLogger(PrescriptionWorkflowService.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter PRESCRIPTION_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final long DEFAULT_CONSULT_ID = 1L;
    private static final long DEFAULT_PATIENT_ID = 1L;
    private static final long DEFAULT_DOCTOR_ID = 1L;
    private static final long DEFAULT_PHARMACIST_ID = 0L;
    private static final String DEFAULT_PATIENT_NAME = "赵晓岚";
    private static final String DEFAULT_DOCTOR_NAME = "陈知衡";
    private static final String STATUS_DRAFT = "草稿";
    private static final String STATUS_SUBMITTED = "待审方";
    private static final String STATUS_AUDITED = "待发药";
    private static final String STATUS_REJECTED = "已驳回";

    /** 处方数据访问组件。 */
    private final PrePrescriptionMapper prePrescriptionMapper;
    /** 处方药品明细数据访问组件。 */
    private final PrePrescriptionItemMapper prePrescriptionItemMapper;
    /** 消息生产者。 */
    private final MqProducer mqProducer;

    /**
     * 查询处方列表。
     *
     * @return 处方展示列表
     */
    public List<PrescriptionVO> listPrescriptions() {
        log.info("查询处方列表");
        return prePrescriptionMapper.selectList(activePrescriptionWrapper())
            .stream()
            .sorted(Comparator.comparing(PrePrescriptionEntity::getId))
            .map(this::toPrescriptionVO)
            .toList();
    }

    /**
     * 创建处方草稿并写入药品明细。
     *
     * @param request 创建请求
     * @return 创建后的处方
     */
    @Transactional
    public PrescriptionVO create(CreatePrescriptionRequest request) {
        ensureBusinessTenantContext("处方模块操作缺少有效租户上下文");
        Long consultId = defaultLong(request.getConsultId(), DEFAULT_CONSULT_ID);
        Long patientId = defaultLong(request.getPatientId(), DEFAULT_PATIENT_ID);
        Long doctorId = defaultLong(request.getDoctorId(), DEFAULT_DOCTOR_ID);
        String patientName = defaultIfBlank(request.getPatientName(), DEFAULT_PATIENT_NAME);
        String doctorName = defaultIfBlank(request.getDoctorName(), DEFAULT_DOCTOR_NAME);
        List<Long> drugIds = request.getDrugIds() == null
            ? List.of()
            : request.getDrugIds().stream().filter(Objects::nonNull).toList();
        int drugCount = drugIds.isEmpty() ? defaultInt(request.getDrugCount(), 1) : drugIds.size();
        String issuedAt = defaultIfBlank(request.getIssuedAt(), currentDisplayTime());
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
        entity.setDeleted(0);
        prePrescriptionMapper.insert(entity);
        entity.setPrescriptionNo(resolvePrescriptionNo(entity.getId()));
        prePrescriptionMapper.updateById(entity);
        insertItems(entity.getId(), drugIds);
        return toPrescriptionVO(entity);
    }

    /**
     * 提交处方进入待审方状态。
     *
     * @param id 处方编号
     * @return 提交后的处方
     */
    @Transactional
    public PrescriptionVO submit(Long id) {
        ensureBusinessTenantContext("处方模块操作缺少有效租户上下文");
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
     * 审核通过处方。
     *
     * @param id 处方编号
     * @param request 审核请求
     * @return 审核后的处方
     */
    @Transactional
    public PrescriptionVO approve(Long id, ApprovePrescriptionRequest request) {
        ensureBusinessTenantContext("处方模块操作缺少有效租户上下文");
        Long pharmacistId = request == null ? DEFAULT_PHARMACIST_ID : defaultLong(request.getPharmacistId(), DEFAULT_PHARMACIST_ID);
        String remark = request == null ? "" : defaultIfBlank(request.getRemark(), "");
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
        mqProducer.publish(new MqMessage("prescription.audited", "{\"prescriptionId\":" + id + "}", 0, 0, 3));
        return toPrescriptionVO(entity);
    }

    /**
     * 驳回处方。
     *
     * @param id 处方编号
     * @param request 驳回请求
     * @return 驳回后的处方
     */
    @Transactional
    public PrescriptionVO reject(Long id, RejectPrescriptionRequest request) {
        ensureBusinessTenantContext("处方模块操作缺少有效租户上下文");
        String remark = request == null ? "" : defaultIfBlank(request.getRemark(), "");
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
     * 写入处方药品明细。
     *
     * @param prescriptionId 处方编号
     * @param drugIds 药品编号列表
     */
    private void insertItems(Long prescriptionId, List<Long> drugIds) {
        if (drugIds.isEmpty()) {
            insertItem(prescriptionId, 1L, "接口测试药品");
            return;
        }
        for (Long drugId : drugIds) {
            insertItem(prescriptionId, drugId, "药品" + drugId);
        }
    }

    /**
     * 写入单个处方药品明细。
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
        item.setDosage("遵医嘱");
        item.setFrequency("每日一次");
        item.setQuantity(BigDecimal.ONE);
        item.setUsageNote("饭后服用");
        item.setDeleted(0);
        prePrescriptionItemMapper.insert(item);
    }

    /**
     * 构造激活处方查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<PrePrescriptionEntity> activePrescriptionWrapper() {
        return new LambdaQueryWrapper<PrePrescriptionEntity>().eq(PrePrescriptionEntity::getDeleted, 0);
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
     * 查询处方并校验存在。
     *
     * @param id 处方编号
     * @return 处方实体
     */
    private PrePrescriptionEntity requireActivePrescription(Long id) {
        PrePrescriptionEntity entity = prePrescriptionMapper.selectOne(new LambdaQueryWrapper<PrePrescriptionEntity>()
            .eq(PrePrescriptionEntity::getDeleted, 0)
            .eq(PrePrescriptionEntity::getId, id)
            .last("limit 1"));
        if (entity == null) {
            throw new BizException(404, "处方不存在");
        }
        return entity;
    }

    /**
     * 转换处方展示对象。
     *
     * @param entity 处方实体
     * @return 处方展示对象
     */
    private PrescriptionVO toPrescriptionVO(PrePrescriptionEntity entity) {
        PrescriptionVO vo = new PrescriptionVO();
        vo.setKey(String.valueOf(entity.getId()));
        vo.setId(entity.getId());
        vo.setPrescriptionNo(defaultIfBlank(entity.getPrescriptionNo(), resolvePrescriptionNo(entity.getId())));
        vo.setPatientName(defaultIfBlank(entity.getPatientName(), ""));
        vo.setDoctorName(defaultIfBlank(entity.getDoctorName(), ""));
        vo.setDrugCount(defaultInt(entity.getDrugCount(), 0));
        vo.setIssuedAt(defaultIfBlank(entity.getIssuedAt(), ""));
        vo.setStatus(defaultIfBlank(entity.getStatus(), STATUS_DRAFT));
        vo.setRemark(defaultIfBlank(entity.getAuditRemark(), ""));
        return vo;
    }

    /**
     * 生成处方号。
     *
     * @param id 处方编号
     * @return 处方号
     */
    private String resolvePrescriptionNo(Long id) {
        return "CF" + LocalDate.now().format(PRESCRIPTION_DATE_FORMATTER) + String.format("%04d", id);
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
        return value == null || value.isBlank() ? defaultValue : value.trim();
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
    private int defaultInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }
}
