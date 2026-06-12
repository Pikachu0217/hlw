package com.hlw.drug.controller;

import com.hlw.common.core.domain.R;
import com.hlw.drug.service.DrugDeliveryService;
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
        return R.ok(List.of());
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
        return R.ok(List.of());
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
