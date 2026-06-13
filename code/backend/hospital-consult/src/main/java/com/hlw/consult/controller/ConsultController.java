package com.hlw.consult.controller;

import com.hlw.common.core.domain.R;
import com.hlw.common.core.jdbc.DemoDataQuery;
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

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/consult")
public class ConsultController {
    private static final Logger log = LoggerFactory.getLogger(ConsultController.class);

    private final ConsultLifecycleService consultLifecycleService;
    private final ConsultMessageRepository consultMessageRepository;
    private final DemoDataQuery demoDataQuery;

    /**
     * 构造问诊控制器。
     *
     * @param consultLifecycleService 问诊生命周期服务
     * @param consultMessageRepository 问诊消息仓储
     * @param demoDataQuery 演示数据查询器
     */
    public ConsultController(
        ConsultLifecycleService consultLifecycleService,
        ConsultMessageRepository consultMessageRepository,
        DemoDataQuery demoDataQuery
    ) {
        this.consultLifecycleService = consultLifecycleService;
        this.consultMessageRepository = consultMessageRepository;
        this.demoDataQuery = demoDataQuery;
    }

    /**
     * 查询问诊单列表。
     *
     * @return 问诊单列表
     */
    @GetMapping("/consults")
    public R<List<Map<String, Object>>> consults() {
        log.info("查询问诊单列表");
        return R.ok(demoDataQuery.list("问诊单列表", """
            SELECT id::text AS key,
                   consult_no AS "consultNo",
                   patient_name AS "patientName",
                   doctor_name AS "doctorName",
                   channel AS channel,
                   status AS status,
                   updated_at AS "updatedAt"
            FROM con_consult
            WHERE deleted = 0
            ORDER BY id
            """));
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
        log.info("查询问诊消息，consultId={}", id);
        List<ConsultMessage> messages = consultMessageRepository.findByConsultId(id);
        if (messages.isEmpty()) {
            List<ConsultMessage> demoMessages = demoDataQuery.list("问诊消息列表", """
                    SELECT consult_id AS "consultId",
                           sender_id AS "senderId",
                           sender_type AS "senderType",
                           content AS content,
                           content_type AS "contentType",
                           read_flag AS read,
                           create_time AS "createTime"
                    FROM con_message
                    WHERE deleted = 0 AND consult_id = ?
                    ORDER BY id
                    """, id)
                .stream()
                .map(row -> new ConsultMessage(
                    ((Number) row.get("consultId")).longValue(),
                    ((Number) row.get("senderId")).longValue(),
                    String.valueOf(row.get("senderType")),
                    String.valueOf(row.get("content")),
                    String.valueOf(row.get("contentType")),
                    Boolean.TRUE.equals(row.get("read")),
                    toLocalDateTime(row.get("createTime"))
                ))
                .toList();
            return R.ok(demoMessages);
        }
        return R.ok(messages);
    }

    /**
     * 转换数据库时间字段。
     *
     * @param value 数据库时间值
     * @return 本地日期时间
     */
    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return null;
    }
}
