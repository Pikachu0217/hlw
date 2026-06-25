package com.hlw.consult.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.core.domain.R;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.tenant.TokenPrincipalContext;
import com.hlw.consult.client.DoctorFeignClient;
import com.hlw.consult.client.resp.InternalDoctorResp;
import com.hlw.consult.entity.ConConsultEntity;
import com.hlw.consult.entity.ConMessageEntity;
import com.hlw.consult.mapper.ConConsultMapper;
import com.hlw.consult.mapper.ConMessageMapper;
import com.hlw.consult.vo.DoctorConsultWorkbenchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 医生咨询工作台服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorConsultWorkbenchService {
    private static final DateTimeFormatter MESSAGE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 问诊单数据访问组件。 */
    private final ConConsultMapper conConsultMapper;
    /** 问诊消息数据访问组件。 */
    private final ConMessageMapper conMessageMapper;
    /** 医生服务内部客户端。 */
    private final DoctorFeignClient doctorFeignClient;

    /**
     * 查询当前登录医生的咨询患者工作台。
     *
     * @return 医生咨询工作台列表
     */
    public List<DoctorConsultWorkbenchVO> listCurrentDoctorWorkbench() {
        InternalDoctorResp doctor = resolveCurrentDoctor();
        log.info("查询医生咨询工作台，doctorId={}，doctorName={}", doctor.id(), doctor.doctorName());
        List<ConConsultEntity> consults = conConsultMapper.selectList(new LambdaQueryWrapper<ConConsultEntity>()
                .eq(ConConsultEntity::getDoctorId, doctor.id())
                .in(ConConsultEntity::getStatus, ConsultDisplayStatus.DOCTOR_WORKBENCH_STATUSES)
                .eq(ConConsultEntity::getDeleted, 0))
            .stream()
            .sorted(Comparator.comparing(ConConsultEntity::getId).reversed())
            .toList();
        Map<Long, ConMessageEntity> latestMessageMap = latestMessageMap(consults);
        Map<Long, ConMessageEntity> chiefComplaintMap = chiefComplaintMap(consults);
        return consults.stream()
            .map(entity -> toWorkbenchVO(entity, latestMessageMap.get(entity.getId()), chiefComplaintMap.get(entity.getId())))
            .toList();
    }

    /**
     * 解析当前登录医生档案。
     *
     * @return 内部医生档案
     */
    public InternalDoctorResp resolveCurrentDoctor() {
        if (TokenPrincipalContext.get() == null) {
            log.warn("解析当前医生失败，登录上下文为空");
            throw new BizException(401, "当前登录用户无效");
        }
        Long tenantId = TokenPrincipalContext.get().getTenantId();
        String userId = TokenPrincipalContext.get().getBusinessUserId();
        if (tenantId == null || tenantId <= 0L || Boolean.TRUE.equals(TokenPrincipalContext.get().getPlatformRequest())) {
            log.warn("解析当前医生失败，租户上下文无效，tenantId={}", tenantId);
            throw new BizException(403, "医生工作台仅支持业务租户访问");
        }
        if (userId == null || userId.isBlank()) {
            log.warn("解析当前医生失败，登录用户编号为空");
            throw new BizException(401, "当前登录用户无效");
        }
        R<InternalDoctorResp> response = doctorFeignClient.findByUser(tenantId, userId);
        if (response == null || response.code() != 200 || response.data() == null) {
            log.warn("当前登录账号未绑定医生档案，tenantId={}，userId={}", tenantId, userId);
            throw new BizException(403, "当前登录账号未绑定医生档案");
        }
        return response.data();
    }

    /**
     * 构造最新消息映射。
     *
     * @param consults 问诊单列表
     * @return 最新消息映射
     */
    private Map<Long, ConMessageEntity> latestMessageMap(List<ConConsultEntity> consults) {
        List<Long> consultIds = consults.stream().map(ConConsultEntity::getId).toList();
        if (consultIds.isEmpty()) {
            return Map.of();
        }
        return conMessageMapper.selectList(new LambdaQueryWrapper<ConMessageEntity>()
                .in(ConMessageEntity::getConsultId, consultIds)
                .eq(ConMessageEntity::getDeleted, 0)
                .orderByDesc(ConMessageEntity::getId))
            .stream()
            .collect(Collectors.toMap(ConMessageEntity::getConsultId, Function.identity(), (current, ignored) -> current));
    }

    /**
     * 构造患者问题描述映射。
     *
     * @param consults 问诊单列表
     * @return 患者问题描述映射
     */
    private Map<Long, ConMessageEntity> chiefComplaintMap(List<ConConsultEntity> consults) {
        List<Long> consultIds = consults.stream().map(ConConsultEntity::getId).toList();
        if (consultIds.isEmpty()) {
            return Map.of();
        }
        return conMessageMapper.selectList(new LambdaQueryWrapper<ConMessageEntity>()
                .in(ConMessageEntity::getConsultId, consultIds)
                .eq(ConMessageEntity::getSenderType, ConsultParticipantType.PATIENT)
                .eq(ConMessageEntity::getDeleted, 0)
                .orderByAsc(ConMessageEntity::getId))
            .stream()
            .collect(Collectors.toMap(ConMessageEntity::getConsultId, Function.identity(), (current, ignored) -> current));
    }

    /**
     * 转换工作台展示对象。
     *
     * @param entity 问诊单实体
     * @param latestMessage 最新消息实体
     * @param chiefComplaint 问题描述消息实体
     * @return 工作台展示对象
     */
    private DoctorConsultWorkbenchVO toWorkbenchVO(ConConsultEntity entity, ConMessageEntity latestMessage, ConMessageEntity chiefComplaint) {
        DoctorConsultWorkbenchVO vo = new DoctorConsultWorkbenchVO();
        vo.setConsultId(entity.getId());
        vo.setConsultNo(entity.getConsultNo());
        vo.setPatientId(entity.getPatientId());
        vo.setPatientName(resolvePatientDisplayName(entity.getPatientName(), entity.getPatientId()));
        vo.setDoctorId(entity.getDoctorId());
        vo.setDoctorName(entity.getDoctorName());
        vo.setStatus(ConsultDisplayStatus.labelOf(entity.getStatus()));
        vo.setChannel(entity.getChannel());
        vo.setUpdatedAt(entity.getUpdatedAt());
        vo.setRemainingSeconds(entity.getRemainingSeconds());
        vo.setLastMessage(latestMessage == null ? "" : latestMessage.getContent());
        vo.setChiefComplaint(chiefComplaint == null ? "" : chiefComplaint.getContent());
        vo.setLastMessageTime(latestMessage == null || latestMessage.getCreateTime() == null ? "" : latestMessage.getCreateTime().format(MESSAGE_TIME_FORMATTER));
        return vo;
    }

    /**
     * 解析患者展示姓名。
     *
     * @param patientName 患者姓名
     * @param patientId 患者编号
     * @return 患者展示姓名
     */
    private String resolvePatientDisplayName(String patientName, Long patientId) {
        if (patientName != null && !patientName.isBlank()) {
            return patientName;
        }
        return patientId == null || patientId <= 0L ? "未知患者" : "患者" + patientId;
    }
}
