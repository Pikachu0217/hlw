package com.hlw.consult.controller;

import com.hlw.common.core.domain.R;
import com.hlw.consult.service.Consult;
import com.hlw.consult.service.ConsultLifecycleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/consult")
public class ConsultController {
    private final ConsultLifecycleService consultLifecycleService;

    public ConsultController(ConsultLifecycleService consultLifecycleService) {
        this.consultLifecycleService = consultLifecycleService;
    }

    @PostMapping("/consults")
    public R<Map<String, Object>> createConsult(@RequestBody Map<String, Object> command) {
        return R.ok(command);
    }

    @PostMapping("/consults/{id}/accept")
    public R<Consult> accept(@PathVariable Long id, @RequestBody Map<String, Long> command) {
        return R.ok(consultLifecycleService.accept(id, command.get("tenantId")));
    }

    @PostMapping("/consults/{id}/complete")
    public R<Map<String, Object>> complete(@PathVariable Long id) {
        return R.ok(Map.of("id", id, "status", "COMPLETED"));
    }

    @PostMapping("/consults/{id}/extend")
    public R<Map<String, Object>> extend(@PathVariable Long id) {
        return R.ok(Map.of("id", id, "status", "EXTENDED"));
    }

    @GetMapping("/consults/{id}/messages")
    public R<List<Map<String, Object>>> messages(@PathVariable Long id) {
        return R.ok(List.of());
    }
}
