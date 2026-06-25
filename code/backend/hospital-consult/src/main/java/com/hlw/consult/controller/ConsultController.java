package com.hlw.consult.controller;

import com.hlw.common.core.domain.R;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.tenant.TokenPrincipalContext;
import com.hlw.common.minio.domain.MinioDownloadResult;
import com.hlw.common.minio.domain.MinioUploadResult;
import com.hlw.common.minio.service.HlwMinioService;
import com.hlw.consult.dto.AcceptConsultRequest;
import com.hlw.consult.dto.CreateConsultRequest;
import com.hlw.consult.dto.InternalCreateConsultFromAppointmentRequest;
import com.hlw.consult.dto.InternalSyncConsultStatusRequest;
import com.hlw.consult.dto.RejectConsultRequest;
import com.hlw.consult.service.ConsultWorkflowService;
import com.hlw.consult.service.DoctorConsultWorkbenchService;
import com.hlw.consult.vo.ConsultVO;
import com.hlw.consult.vo.ConsultImageUploadVO;
import com.hlw.consult.vo.DoctorConsultWorkbenchVO;
import com.hlw.consult.ws.ConsultMessage;
import com.hlw.consult.ws.ConsultMessageRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 问诊管理控制器。
 */
@RestController
@RequestMapping("/consult")
@Slf4j
public class ConsultController {
    private final ConsultWorkflowService consultWorkflowService;
    private final ConsultMessageRepository consultMessageRepository;
    private final DoctorConsultWorkbenchService doctorConsultWorkbenchService;
    private final HlwMinioService hlwMinioService;

    /**
     * 构造问诊控制器。
     *
     * @param consultWorkflowService 问诊工作流服务
     * @param consultMessageRepository 问诊消息仓储
     * @param doctorConsultWorkbenchService 医生咨询工作台服务
     * @param hlwMinioService MinIO 文件存储服务
     */
    public ConsultController(
        ConsultWorkflowService consultWorkflowService,
        ConsultMessageRepository consultMessageRepository,
        DoctorConsultWorkbenchService doctorConsultWorkbenchService,
        HlwMinioService hlwMinioService
    ) {
        this.consultWorkflowService = consultWorkflowService;
        this.consultMessageRepository = consultMessageRepository;
        this.doctorConsultWorkbenchService = doctorConsultWorkbenchService;
        this.hlwMinioService = hlwMinioService;
    }

    /**
     * 查询问诊单列表。
     *
     * @return 问诊单列表
     */
    @GetMapping("/consults")
    public R<List<ConsultVO>> consults() {
        log.info("查询问诊单列表");
        return R.ok(consultWorkflowService.listConsults());
    }

    /**
     * 创建问诊。
     *
     * @param request 创建请求
     * @return 创建结果
     */
    @PostMapping("/consults")
    public R<ConsultVO> createConsult(@Valid @RequestBody CreateConsultRequest request) {
        log.info("创建问诊，doctorId={}，patientId={}", request.getDoctorId(), request.getPatientId());
        return R.ok(consultWorkflowService.createConsult(request));
    }

    /**
     * 接单问诊。
     *
     * @param id 问诊编号
     * @param request 接单请求
     * @return 接单结果
     */
    @PostMapping("/consults/{id}/accept")
    public R<ConsultVO> accept(@PathVariable Long id, @RequestBody AcceptConsultRequest request) {
        log.info("接单问诊，consultId={}", id);
        return R.ok(consultWorkflowService.accept(id, request));
    }

    /**
     * 拒诊问诊。
     *
     * @param id 问诊编号
     * @param request 拒诊请求
     * @return 拒诊结果
     */
    @PostMapping("/consults/{id}/reject")
    public R<ConsultVO> reject(@PathVariable Long id, @RequestBody RejectConsultRequest request) {
        log.info("拒诊问诊，consultId={}", id);
        return R.ok(consultWorkflowService.reject(id, request));
    }

    /**
     * 完成问诊。
     *
     * @param id 问诊编号
     * @return 完成结果
     */
    @PostMapping("/consults/{id}/complete")
    public R<ConsultVO> complete(@PathVariable Long id) {
        log.info("完成问诊，consultId={}", id);
        return R.ok(consultWorkflowService.complete(id));
    }

    /**
     * 延长问诊。
     *
     * @param id 问诊编号
     * @return 延长结果
     */
    @PostMapping("/consults/{id}/extend")
    public R<ConsultVO> extend(@PathVariable Long id) {
        log.info("延长问诊，consultId={}", id);
        return R.ok(consultWorkflowService.extend(id));
    }

    /**
     * 查询当前登录医生咨询工作台。
     *
     * @return 医生咨询工作台列表
     */
    @GetMapping("/doctor/workbench")
    public R<List<DoctorConsultWorkbenchVO>> doctorWorkbench() {
        log.info("查询当前登录医生咨询工作台");
        return R.ok(doctorConsultWorkbenchService.listCurrentDoctorWorkbench());
    }

    /**
     * 从已支付预约单创建问诊。
     *
     * @param appointmentId 预约单编号
     * @return 问诊单
     */
    @PostMapping("/consults/from-appointment/{appointmentId}")
    public R<ConsultVO> createConsultFromAppointment(@PathVariable Long appointmentId) {
        log.info("从预约单创建问诊，appointmentId={}", appointmentId);
        return R.ok(consultWorkflowService.createConsultFromAppointment(appointmentId));
    }

    /**
     * 内部接口：创建预约绑定问诊。
     *
     * @param request 创建请求
     * @return 创建结果
     */
    @PostMapping("/internal/consults/from-appointment")
    public R<Void> createInternalConsultFromAppointment(@RequestBody InternalCreateConsultFromAppointmentRequest request) {
        log.info("内部创建预约绑定问诊，appointmentId={}", request == null ? null : request.getAppointmentId());
        consultWorkflowService.createConsultFromAppointment(request);
        return R.ok();
    }

    /**
     * 内部接口：同步预约对应问诊状态。
     *
     * @param request 同步请求
     * @return 同步结果
     */
    @PostMapping("/internal/consults/sync-status")
    public R<Void> syncConsultStatus(@RequestBody InternalSyncConsultStatusRequest request) {
        log.info("内部同步问诊状态，appointmentId={}，status={}，payStatus={}",
            request == null ? null : request.getAppointmentId(),
            request == null ? null : request.getStatus(),
            request == null ? null : request.getPayStatus());
        consultWorkflowService.syncConsultStatus(request);
        return R.ok();
    }

    /**
     * 查询问诊消息。
     *
     * @param id 问诊编号
     * @return 消息列表
     */
    @GetMapping("/consults/{id}/messages")
    public R<List<ConsultMessage>> messages(@PathVariable Long id) {
        log.info("查询问诊消息，consultId={}", id);
        return R.ok(consultMessageRepository.findByConsultId(id));
    }

    /**
     * 上传问诊图片。
     *
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping(value = "/files/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<ConsultImageUploadVO> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("上传问诊图片，fileName={}，size={}，contentType={}",
            file == null ? null : file.getOriginalFilename(),
            file == null ? null : file.getSize(),
            file == null ? null : file.getContentType());
        TokenPrincipalContext.ensureBusinessTenantContext("上传问诊图片缺少有效租户上下文");
        if (file == null || file.isEmpty()) {
            throw new BizException(400, "图片文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BizException(400, "仅支持上传图片文件");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BizException(400, "图片不能超过 5MB");
        }
        MinioUploadResult result = hlwMinioService.uploadPublicFile(file, "consult/images");
        ConsultImageUploadVO vo = new ConsultImageUploadVO();
        vo.setBucket(result.bucket());
        vo.setObjectName(result.objectName());
        vo.setUrl(result.url());
        log.info("问诊图片上传成功，bucket={}，objectName={}", result.bucket(), result.objectName());
        return R.ok(vo);
    }

    /**
     * 读取问诊图片。
     *
     * @param objectName MinIO 对象名称
     * @return 图片内容
     */
    @GetMapping("/files/images")
    public ResponseEntity<byte[]> image(@RequestParam("objectName") String objectName) {
        log.info("读取问诊图片，objectName={}", objectName);
        MinioDownloadResult result = hlwMinioService.readPublicFile(objectName);
        return ResponseEntity.ok()
            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
            .contentType(resolveImageMediaType(result.contentType()))
            .body(result.bytes());
    }

    /**
     * 解析图片响应类型。
     *
     * @param contentType 文件内容类型
     * @return 图片响应类型
     */
    private MediaType resolveImageMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException exception) {
            log.warn("图片响应类型解析失败，contentType={}", contentType);
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
