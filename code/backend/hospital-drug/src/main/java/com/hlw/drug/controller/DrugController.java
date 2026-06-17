package com.hlw.drug.controller;

import com.hlw.common.core.domain.R;
import com.hlw.drug.dto.CreateDrugRequest;
import com.hlw.drug.dto.CreateStockRequest;
import com.hlw.drug.service.DrugCatalogService;
import com.hlw.drug.vo.DeliveryShipVO;
import com.hlw.drug.vo.DrugVO;
import com.hlw.drug.vo.StockVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 药品与库存管理控制器。
 */
@RestController
@RequestMapping("/drug")
@Slf4j
public class DrugController {
    private final DrugCatalogService drugCatalogService;

    /**
     * 构造药品控制器。
     *
     * @param drugCatalogService 药品目录和库存服务
     */
    public DrugController(DrugCatalogService drugCatalogService) {
        this.drugCatalogService = drugCatalogService;
    }

    /**
     * 查询药品列表。
     *
     * @return 药品列表
     */
    @GetMapping("/drugs")
    public R<List<DrugVO>> drugs() {
        log.info("查询药品列表");
        return R.ok(drugCatalogService.listDrugs());
    }

    /**
     * 创建药品资料。
     *
     * @param request 药品创建请求
     * @return 创建结果
     */
    @PostMapping("/drugs")
    public R<DrugVO> createDrug(@Valid @RequestBody CreateDrugRequest request) {
        return R.ok(drugCatalogService.createDrug(request));
    }

    /**
     * 查询库存列表。
     *
     * @return 库存列表
     */
    @GetMapping("/stocks")
    public R<List<StockVO>> stocks() {
        log.info("查询库存列表");
        return R.ok(drugCatalogService.listStocks());
    }

    /**
     * 创建库存记录。
     *
     * @param request 库存创建请求
     * @return 创建结果
     */
    @PostMapping("/stocks")
    public R<StockVO> createStock(@Valid @RequestBody CreateStockRequest request) {
        return R.ok(drugCatalogService.createStock(request));
    }

    /**
     * 将指定配送单更新为已发货。
     *
     * @param id 配送单编号
     * @return 发货结果
     */
    @PostMapping("/deliveries/{id}/ship")
    public R<DeliveryShipVO> ship(@PathVariable Long id) {
        return R.ok(drugCatalogService.shipDelivery(id));
    }
}
