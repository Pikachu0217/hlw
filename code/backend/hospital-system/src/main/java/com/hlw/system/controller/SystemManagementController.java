package com.hlw.system.controller;

import com.hlw.common.core.domain.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system")
public class SystemManagementController {
    @GetMapping("/tenants")
    public R<List<Map<String, Object>>> tenants() {
        return R.ok(List.of());
    }

    @PostMapping("/tenants")
    public R<Map<String, Object>> createTenant() {
        return R.ok(Map.of());
    }

    @GetMapping("/roles")
    public R<List<Map<String, Object>>> roles() {
        return R.ok(List.of());
    }

    @GetMapping("/menus")
    public R<List<Map<String, Object>>> menus() {
        return R.ok(List.of());
    }
}
