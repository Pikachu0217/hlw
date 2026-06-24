package com.hlw.consult.ws;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.core.domain.R;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.security.TokenPrincipal;
import com.hlw.consult.client.DoctorFeignClient;
import com.hlw.consult.client.PatientFeignClient;
import com.hlw.consult.client.resp.InternalDoctorResp;
import com.hlw.consult.client.resp.InternalPatientResp;
import com.hlw.consult.entity.ConConsultEntity;
import com.hlw.consult.mapper.ConConsultMapper;
import com.hlw.consult.service.ConsultParticipantType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 问诊 WebSocket 参与人鉴权服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultWebSocketPermissionService {
    /** 问诊单数据访问组件。 */
    private final ConConsultMapper conConsultMapper;
    /** 医生服务内部客户端。 */
    private final DoctorFeignClient doctorFeignClient;
    /** 患者服务内部客户端。 */
    private final PatientFeignClient patientFeignClient;

    /**
     * 解析当前登录用户在问诊中的发送人类型。
     *
     * @param consultId 问诊编号
     * @param principal 登录主体
     * @return 发送人类型
     */
    public String resolveSenderType(Long consultId, TokenPrincipal principal) {
        ConConsultEntity consult = requireConsult(consultId);
        if (!consult.getTenantId().equals(principal.getTenantId())) {
            log.warn("问诊 WebSocket 租户不匹配，consultId={}，consultTenantId={}，loginTenantId={}", consultId, consult.getTenantId(), principal.getTenantId());
            throw new BizException(403, "无权连接该问诊");
        }
        InternalPatientResp patient = resolvePatient(principal);
        if (patient != null && consult.getPatientId().equals(patient.id())) {
            return ConsultParticipantType.PATIENT;
        }
        InternalDoctorResp doctor = resolveDoctor(principal);
        if (doctor != null && consult.getDoctorId().equals(doctor.id())) {
            return ConsultParticipantType.DOCTOR;
        }
        log.warn("问诊 WebSocket 参与人校验失败，consultId={}，userId={}", consultId, principal.getBusinessUserId());
        throw new BizException(403, "无权连接该问诊");
    }

    /**
     * 查询并校验问诊单存在。
     *
     * @param consultId 问诊编号
     * @return 问诊单实体
     */
    private ConConsultEntity requireConsult(Long consultId) {
        ConConsultEntity consult = conConsultMapper.selectOne(new LambdaQueryWrapper<ConConsultEntity>()
            .eq(ConConsultEntity::getId, consultId)
            .eq(ConConsultEntity::getDeleted, 0)
            .last("limit 1"));
        if (consult == null) {
            throw new BizException(404, "问诊单不存在");
        }
        return consult;
    }

    /**
     * 解析登录用户对应患者档案。
     *
     * @param principal 登录主体
     * @return 患者档案，不存在时返回 null
     */
    private InternalPatientResp resolvePatient(TokenPrincipal principal) {
        try {
            R<InternalPatientResp> response = patientFeignClient.findByUser(principal.getTenantId(), principal.getBusinessUserId());
            return response == null || response.code() != 200 ? null : response.data();
        } catch (RuntimeException exception) {
            log.warn("问诊 WebSocket 查询患者档案失败，tenantId={}，userId={}", principal.getTenantId(), principal.getBusinessUserId());
            return null;
        }
    }

    /**
     * 解析登录用户对应医生档案。
     *
     * @param principal 登录主体
     * @return 医生档案，不存在时返回 null
     */
    private InternalDoctorResp resolveDoctor(TokenPrincipal principal) {
        try {
            R<InternalDoctorResp> response = doctorFeignClient.findByUser(principal.getTenantId(), principal.getBusinessUserId());
            return response == null || response.code() != 200 ? null : response.data();
        } catch (RuntimeException exception) {
            log.warn("问诊 WebSocket 查询医生档案失败，tenantId={}，userId={}", principal.getTenantId(), principal.getBusinessUserId());
            return null;
        }
    }
}
