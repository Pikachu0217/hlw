package com.hlw.drug.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.tenant.TokenPrincipalContext;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.common.mq.enums.MessageQueueEnum;
import com.hlw.common.mq.service.producer.MessageQueueProducer;
import com.hlw.drug.dto.CreateDrugRequest;
import com.hlw.drug.dto.CreateStockRequest;
import com.hlw.drug.entity.DrugDeliveryEntity;
import com.hlw.drug.entity.DrugInfoEntity;
import com.hlw.drug.entity.DrugStockEntity;
import com.hlw.drug.mapper.DrugDeliveryMapper;
import com.hlw.drug.mapper.DrugInfoMapper;
import com.hlw.drug.mapper.DrugStockMapper;
import com.hlw.drug.vo.DeliveryShipVO;
import com.hlw.drug.vo.DrugVO;
import com.hlw.drug.vo.StockVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 药品目录和库存服务，负责药品资料、库存记录和配送发货落库。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DrugCatalogService {
    private static final String DEFAULT_UNIT = "盒";
    private static final String DEFAULT_WAREHOUSE_NAME = "中心药房";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_SHIPPED = "SHIPPED";

    /** 药品信息数据访问组件。 */
    private final DrugInfoMapper drugInfoMapper;
    /** 药品库存数据访问组件。 */
    private final DrugStockMapper drugStockMapper;
    /** 药品配送数据访问组件。 */
    private final DrugDeliveryMapper drugDeliveryMapper;
    /** 消息生产者。 */
    private final MessageQueueProducer<String, Long> messageQueueProducer;

    /**
     * 查询药品列表。
     *
     * @return 药品展示列表
     */
    public List<DrugVO> listDrugs() {
        log.info("查询药品列表");
        return drugInfoMapper.selectList(new LambdaQueryWrapper<DrugInfoEntity>())
            .stream()
            .sorted(Comparator.comparing(DrugInfoEntity::getId))
            .map(this::toDrugVO)
            .toList();
    }

    /**
     * 查询库存列表。
     *
     * @return 库存展示列表
     */
    public List<StockVO> listStocks() {
        log.info("查询药品库存列表");
        return drugStockMapper.selectList(new LambdaQueryWrapper<DrugStockEntity>())
            .stream()
            .sorted(Comparator.comparing(DrugStockEntity::getId))
            .map(this::toStockVO)
            .toList();
    }

    /**
     * 创建药品资料。
     *
     * @param request 药品创建请求
     * @return 创建后的药品资料
     */
    @Transactional
    public DrugVO createDrug(CreateDrugRequest request) {
        TokenPrincipalContext.ensureBusinessTenantContext("药品模块操作缺少有效租户上下文");
        int inventory = DefaultValueUtils.defaultIfNull(request.getInventory(), 0);
        String unit = DefaultValueUtils.defaultIfBlank(request.getUnit(), DEFAULT_UNIT);
        String warningStatus = warningStatus(inventory);
        log.info("创建药品资料，drugName={}，inventory={}", request.getDrugName(), inventory);

        DrugInfoEntity entity = new DrugInfoEntity();
        entity.setName(request.getDrugName());
        entity.setDrugName(request.getDrugName());
        entity.setSpec(request.getSpec());
        entity.setInventory(inventory);
        entity.setUnit(unit);
        entity.setWarningStatus(warningStatus);
        drugInfoMapper.insert(entity);
        if (inventory > 0) {
            createStockEntity(entity.getId(), DEFAULT_WAREHOUSE_NAME, inventory, warningStatus);
        }
        return toDrugVO(entity);
    }

    /**
     * 创建库存记录。
     *
     * @param request 库存创建请求
     * @return 创建后的库存记录
     */
    @Transactional
    public StockVO createStock(CreateStockRequest request) {
        TokenPrincipalContext.ensureBusinessTenantContext("药品模块操作缺少有效租户上下文");
        int inventory = DefaultValueUtils.defaultIfNull(request.getInventory(), 0);
        String warningStatus = warningStatus(inventory);
        log.info("创建库存记录，drugId={}，warehouseName={}，inventory={}",
            request.getDrugId(), request.getWarehouseName(), inventory);
        requireActiveDrug(request.getDrugId());
        DrugStockEntity stock = createStockEntity(request.getDrugId(), request.getWarehouseName(), inventory, warningStatus);
        refreshDrugInventory(request.getDrugId());
        return toStockVO(stock);
    }

    /**
     * 将配送单更新为已发货并发送发货事件。
     *
     * @param deliveryId 配送单编号
     * @return 发货结果
     */
    @Transactional
    public DeliveryShipVO shipDelivery(Long deliveryId) {
        TokenPrincipalContext.ensureBusinessTenantContext("药品模块操作缺少有效租户上下文");
        log.info("药品配送发货，deliveryId={}", deliveryId);
        DrugDeliveryEntity delivery = requireActiveDelivery(deliveryId);
        if (STATUS_SHIPPED.equals(delivery.getStatus())) {
            log.info("药品配送单已发货，跳过重复事件，deliveryId={}", deliveryId);
            return toDeliveryShipVO(delivery);
        }
        if (!STATUS_PENDING.equals(delivery.getStatus())) {
            throw new BizException(400, "当前配送状态不允许发货");
        }
        delivery.setStatus(STATUS_SHIPPED);
        delivery.setShipTime(LocalDateTime.now());
        drugDeliveryMapper.updateById(delivery);
        messageQueueProducer.send(MessageQueueEnum.QUEUE_DRUG_SHIPPED, "{\"deliveryId\":" + deliveryId + "}");
        log.info("药品配送事件已发布，deliveryId={}", deliveryId);
        return toDeliveryShipVO(delivery);
    }

    /**
     * 创建库存实体。
     *
     * @param drugId 药品编号
     * @param warehouseName 仓库名称
     * @param inventory 库存数量
     * @param warningStatus 预警状态
     * @return 库存实体
     */
    private DrugStockEntity createStockEntity(Long drugId, String warehouseName, int inventory, String warningStatus) {
        DrugStockEntity stock = new DrugStockEntity();
        stock.setDrugId(drugId);
        stock.setWarehouseName(warehouseName);
        stock.setInventory(inventory);
        stock.setWarningStatus(warningStatus);
        stock.setStockQty(BigDecimal.valueOf(inventory));
        drugStockMapper.insert(stock);
        return stock;
    }

    /**
     * 刷新药品主表库存冗余字段。
     *
     * @param drugId 药品编号
     */
    private void refreshDrugInventory(Long drugId) {
        DrugInfoEntity drug = requireActiveDrug(drugId);
        int totalInventory = drugStockMapper.selectList(new LambdaQueryWrapper<DrugStockEntity>()
                .eq(DrugStockEntity::getDrugId, drugId))
            .stream()
            .map(DrugStockEntity::getInventory)
            .reduce(0, Integer::sum);
        drug.setInventory(totalInventory);
        drug.setWarningStatus(warningStatus(totalInventory));
        drugInfoMapper.updateById(drug);
    }

    /**
     * 查询药品并校验存在。
     *
     * @param drugId 药品编号
     * @return 药品实体
     */
    private DrugInfoEntity requireActiveDrug(Long drugId) {
        DrugInfoEntity entity = drugInfoMapper.selectOne(new LambdaQueryWrapper<DrugInfoEntity>()
            .eq(DrugInfoEntity::getId, drugId)
            .last("limit 1"));
        if (entity == null) {
            throw new BizException(404, "药品不存在");
        }
        return entity;
    }

    /**
     * 查询配送单并校验存在。
     *
     * @param deliveryId 配送单编号
     * @return 配送单实体
     */
    private DrugDeliveryEntity requireActiveDelivery(Long deliveryId) {
        DrugDeliveryEntity entity = drugDeliveryMapper.selectOne(new LambdaQueryWrapper<DrugDeliveryEntity>()
            .eq(DrugDeliveryEntity::getId, deliveryId)
            .last("limit 1"));
        if (entity == null) {
            throw new BizException(404, "配送单不存在");
        }
        return entity;
    }

    /**
     * 转换药品展示对象。
     *
     * @param entity 药品实体
     * @return 药品展示对象
     */
    private DrugVO toDrugVO(DrugInfoEntity entity) {
        DrugVO vo = new DrugVO();
        vo.setId(entity.getId());
        vo.setDrugName(DefaultValueUtils.defaultIfBlank(entity.getDrugName(), entity.getName()));
        vo.setSpec(DefaultValueUtils.defaultIfBlank(entity.getSpec(), ""));
        vo.setInventory(DefaultValueUtils.defaultIfNull(entity.getInventory(), 0));
        vo.setUnit(DefaultValueUtils.defaultIfBlank(entity.getUnit(), DEFAULT_UNIT));
        vo.setWarningStatus(DefaultValueUtils.defaultIfBlank(entity.getWarningStatus(), warningStatus(vo.getInventory())));
        return vo;
    }

    /**
     * 转换库存展示对象。
     *
     * @param entity 库存实体
     * @return 库存展示对象
     */
    private StockVO toStockVO(DrugStockEntity entity) {
        StockVO vo = new StockVO();
        vo.setId(entity.getId());
        vo.setDrugName(requireActiveDrug(entity.getDrugId()).getDrugName());
        vo.setWarehouseName(DefaultValueUtils.defaultIfBlank(entity.getWarehouseName(), DEFAULT_WAREHOUSE_NAME));
        vo.setInventory(DefaultValueUtils.defaultIfNull(entity.getInventory(), 0));
        vo.setWarningStatus(DefaultValueUtils.defaultIfBlank(entity.getWarningStatus(), warningStatus(vo.getInventory())));
        return vo;
    }

    /**
     * 转换配送发货展示对象。
     *
     * @param entity 配送实体
     * @return 发货展示对象
     */
    private DeliveryShipVO toDeliveryShipVO(DrugDeliveryEntity entity) {
        DeliveryShipVO vo = new DeliveryShipVO();
        vo.setId(entity.getId());
        vo.setStatus(entity.getStatus());
        return vo;
    }

    /**
     * 根据库存数量计算预警状态。
     *
     * @param inventory 库存数量
     * @return 预警状态
     */
    private String warningStatus(int inventory) {
        return inventory < 50 ? "预警" : "正常";
    }

}
