package com.hlw.consult.controller;

import com.hlw.common.core.domain.R;
import com.hlw.consult.service.Consult;
import com.hlw.consult.service.ConsultLifecycleService;
import com.hlw.consult.ws.ConsultMessage;
import com.hlw.consult.ws.ConsultMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/consult")
public class ConsultController {
    private static final Logger log = LoggerFactory.getLogger(ConsultController.class);

    private final ConsultLifecycleService consultLifecycleService;
    private final ConsultMessageRepository consultMessageRepository;

    /**
     * 构造问诊控制器。
     *
     * @param consultLifecycleService 问诊生命周期服务
     * @param consultMessageRepository 问诊消息仓储
     */
    public ConsultController(
        ConsultLifecycleService consultLifecycleService,
        ConsultMessageRepository consultMessageRepository
    ) {
        this.consultLifecycleService = consultLifecycleService;
        this.consultMessageRepository = consultMessageRepository;
    }

    /**
     * 查询问诊单列表。
     *
     * @return 问诊单列表
     */
    @GetMapping("/consults")
    public R<List<Map<String, Object>>> consults() {
        log.info("查询问诊单列表");
        return R.ok(List.of(
            Map.of("key", "1", "consultNo", "ZX20260612001", "patientName", "赵晓岚", "doctorName", "陈知衡", "channel", "图文", "status", "待接单", "updatedAt", "10:18"),
            Map.of("key", "2", "consultNo", "ZX20260612002", "patientName", "沈博远", "doctorName", "顾清和", "channel", "视频", "status", "咨询中", "updatedAt", "10:07")
        ));
    }

    /**
     * 创建问诊。
     *
     * @param command 创建命令
     * @return 创建结果
     */
    @PostMapping("/consults")
    public R<Map<String, Object>> createConsult(@RequestBody Map<String, Object> command) {
        return R.ok(Map.of(
            "id", 1L,
            "status", "WAITING",
            "type", command.getOrDefault("type", "IMAGE_TEXT"),
            "chiefComplaint", command.getOrDefault("chiefComplaint", "")
        ));
    }

    /**
     * 接单问诊。
     *
     * @param id 问诊编号
     * @param command 接单命令
     * @return 问诊对象
     */
    @PostMapping("/consults/{id}/accept")
    public R<Consult> accept(@PathVariable Long id, @RequestBody Map<String, Long> command) {
        return R.ok(consultLifecycleService.accept(id, command.get("tenantId")));
    }

    /**
     * 完成问诊。
     *
     * @param id 问诊编号
     * @return 完成结果
     */
    @PostMapping("/consults/{id}/complete")
    public R<Map<String, Object>> complete(@PathVariable Long id) {
        return R.ok(Map.of("id", id, "status", "COMPLETED"));
    }

    /**
     * 延长问诊。
     *
     * @param id 问诊编号
     * @return 延长结果
     */
    @PostMapping("/consults/{id}/extend")
    public R<Map<String, Object>> extend(@PathVariable Long id) {
        return R.ok(Map.of("id", id, "status", "EXTENDED"));
    }

    /**
     * 查询问诊消息。
     *
     * @param id 问诊编号
     * @return 消息列表
     */
    @GetMapping("/consults/{id}/messages")
    public R<List<ConsultMessage>> messages(@PathVariable Long id) {
        List<ConsultMessage> messages = consultMessageRepository.findByConsultId(id);
        if (messages.isEmpty()) {
            return R.ok(List.of(
                new ConsultMessage(id, 2L, "DOCTOR", "哪里不舒服", "TEXT", false, LocalDateTime.now()),
                new ConsultMessage(id, 1L, "PATIENT", "孩子从昨晚开始发烧", "TEXT", false, LocalDateTime.now())
            ));
        }
        return R.ok(messages);
    }
}
