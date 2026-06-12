package com.hlw.drug.controller;

import com.hlw.common.core.domain.R;
import com.hlw.drug.service.DrugDeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 药品与库存管理控制器。
 */
@RestController
@RequestMapping("/drug")
public class DrugController {
    private static final Logger log = LoggerFactory.getLogger(DrugController.class);

    private final DrugDeliveryService drugDeliveryService;

    /**
     * 构造药品控制器。
     *
     * @param drugDeliveryService 药品配送服务
     */
    public DrugController(DrugDeliveryService drugDeliveryService) {
        this.drugDeliveryService = drugDeliveryService;
    }

    /**
     * 查询药品列表。
     *
     * @return 药品列表
     */
    @GetMapping("/drugs")
    public R<List<Map<String, Object>>> drugs() {
        log.info("查询药品列表");
        return R.ok(List.of(
            Map.of("key", "1", "drugName", "阿托伐他汀钙片", "spec", "20mg*14片", "inventory", 124, "unit", "盒", "warningStatus", "正常"),
            Map.of("key", "2", "drugName", "盐酸二甲双胍缓释片", "spec", "0.5g*30片", "inventory", 42, "unit", "盒", "warningStatus", "预警")
        ));
    }

    /**
     * 创建药品资料。
     *
     * @param command 药品创建命令
     * @return 创建结果
     */
    @PostMapping("/drugs")
    public R<Map<String, Object>> createDrug(@RequestBody Map<String, Object> command) {
        return R.ok(command);
    }

    /**
     * 查询库存列表。
     *
     * @return 库存列表
     */
    @GetMapping("/stocks")
    public R<List<Map<String, Object>>> stocks() {
        log.info("查询库存列表");
        return R.ok(List.of(
            Map.of("key", "1", "drugName", "阿托伐他汀钙片", "warehouseName", "中心药房", "inventory", 124, "warningStatus", "正常"),
            Map.of("key", "2", "drugName", "盐酸二甲双胍缓释片", "warehouseName", "中心药房", "inventory", 42, "warningStatus", "预警")
        ));
    }

    /**
     * 创建库存记录。
     *
     * @param command 库存创建命令
     * @return 创建结果
     */
    @PostMapping("/stocks")
    public R<Map<String, Object>> createStock(@RequestBody Map<String, Object> command) {
        return R.ok(command);
    }

    /**
     * 将指定配送单更新为已发货。
     *
     * @param id 配送单编号
     * @return 发货结果
     */
    @PostMapping("/deliveries/{id}/ship")
    public R<Map<String, Object>> ship(@PathVariable Long id) {
        drugDeliveryService.ship(id);
        return R.ok(Map.of("id", id, "status", "SHIPPED"));
    }
}
