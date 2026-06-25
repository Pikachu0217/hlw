package com.hlw.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.tenant.TokenPrincipalContext;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.common.mq.enums.MessageQueueEnum;
import com.hlw.common.mq.service.producer.MessageQueueProducer;
import com.hlw.order.dto.CreateOrderRequest;
import com.hlw.order.dto.PayCallbackRequest;
import com.hlw.order.dto.PayOrderRequest;
import com.hlw.order.dto.RefundCallbackRequest;
import com.hlw.order.entity.OrdOrderEntity;
import com.hlw.order.mapper.OrdOrderMapper;
import com.hlw.order.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 订单工作流服务，负责订单创建、支付状态变更和支付事件发布。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderWorkflowService {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter ORDER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String DEFAULT_BIZ_TYPE = "APPOINTMENT";
    private static final String STATUS_PENDING_PAY = "待支付";
    private static final String STATUS_PAID = "已支付";

    /** 订单数据访问组件。 */
    private final OrdOrderMapper ordOrderMapper;
    /** 消息生产者。 */
    private final MessageQueueProducer<String, Long> messageQueueProducer;

    /**
     * 查询订单列表。
     *
     * @return 订单展示列表
     */
    public List<OrderVO> listOrders() {
        log.info("查询订单列表");
        return ordOrderMapper.selectList(new LambdaQueryWrapper<OrdOrderEntity>())
            .stream()
            .sorted(Comparator.comparing(OrdOrderEntity::getId))
            .map(this::toOrderVO)
            .toList();
    }

    /**
     * 创建待支付订单。
     *
     * @param request 订单创建请求
     * @return 创建后的订单
     */
    @Transactional
    public OrderVO create(CreateOrderRequest request) {
        TokenPrincipalContext.ensureBusinessTenantContext("订单模块操作缺少有效租户上下文");
        String bizType = DefaultValueUtils.defaultIfBlank(request.getBizType(), DefaultValueUtils.defaultIfBlank(request.getBusinessType(), DEFAULT_BIZ_TYPE));
        String businessType = businessTypeName(bizType);
        Long bizId = DefaultValueUtils.defaultIfNull(request.getBizId(), 0L);
        Long patientId = DefaultValueUtils.defaultIfNull(request.getPatientId(), 0L);
        String patientName = DefaultValueUtils.defaultIfBlank(request.getPatientName(), "");
        BigDecimal amount = DefaultValueUtils.defaultIfNull(request.getAmount(), BigDecimal.ZERO);
        String createdAt = DefaultValueUtils.defaultIfBlank(request.getCreatedAt(), currentDisplayTime());
        log.info("创建订单，bizType={}，bizId={}，patientId={}，amount={}", bizType, bizId, patientId, amount);

        OrdOrderEntity entity = new OrdOrderEntity();
        entity.setOrderNo("");
        entity.setBizType(bizType);
        entity.setBizId(bizId);
        entity.setPatientId(patientId);
        entity.setBusinessType(businessType);
        entity.setPatientName(patientName);
        entity.setAmount(amount);
        entity.setPayStatus(STATUS_PENDING_PAY);
        entity.setStatus(STATUS_PENDING_PAY);
        entity.setCreatedAt(createdAt);
        ordOrderMapper.insert(entity);
        entity.setOrderNo(resolveOrderNo(entity.getId()));
        ordOrderMapper.updateById(entity);
        return toOrderVO(entity);
    }

    /**
     * 支付订单。
     *
     * @param id 订单编号
     * @param request 支付请求
     * @return 支付后的订单
     */
    @Transactional
    public OrderVO pay(Long id, PayOrderRequest request) {
        TokenPrincipalContext.ensureBusinessTenantContext("订单模块操作缺少有效租户上下文");
        String payMethod = request == null ? "" : DefaultValueUtils.defaultIfBlank(request.getPayMethod(), "");
        log.info("订单支付，orderId={}，payMethod={}", id, payMethod);
        OrdOrderEntity order = requireActiveOrder(id);
        if (STATUS_PAID.equals(order.getPayStatus())) {
            log.info("订单无需重复支付，orderId={}", id);
            return toOrderVO(order);
        }
        if (!STATUS_PENDING_PAY.equals(order.getPayStatus())) {
            throw new BizException(409, "订单当前状态不允许支付");
        }
        order.setPayStatus(STATUS_PAID);
        order.setStatus(STATUS_PAID);
        order.setPayMethod(payMethod);
        order.setPayTime(LocalDateTime.now());
        ordOrderMapper.updateById(order);
        messageQueueProducer.send(MessageQueueEnum.QUEUE_ORDER_PAID, "{\"orderId\":" + id + "}");
        return toOrderVO(order);
    }

    /**
     * 支付成功回调（供支付网关或手动调用）。
     *
     * @param request 回调请求
     * @return 更新后的订单
     */
    @Transactional
    public OrderVO payCallback(PayCallbackRequest request) {
        log.info("支付成功回调，orderNo={}，tradeNo={}，payAmount={}", request.getOrderNo(), request.getTradeNo(), request.getPayAmount());
        OrdOrderEntity order = ordOrderMapper.selectOne(new LambdaQueryWrapper<OrdOrderEntity>()
            .eq(OrdOrderEntity::getOrderNo, request.getOrderNo())
            .last("limit 1"));
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        if (STATUS_PAID.equals(order.getPayStatus())) {
            log.info("订单无需重复支付回调，orderId={}", order.getId());
            return toOrderVO(order);
        }
        order.setPayStatus(STATUS_PAID);
        order.setStatus(STATUS_PAID);
        order.setPayMethod(DefaultValueUtils.defaultIfBlank(request.getPayMethod(), order.getPayMethod()));
        order.setPayTime(request.getPayTime() == null ? LocalDateTime.now() : request.getPayTime());
        ordOrderMapper.updateById(order);
        log.info("支付回调处理完成，orderId={}，orderNo={}", order.getId(), request.getOrderNo());
        return toOrderVO(order);
    }

    /**
     * 退款成功回调（供支付网关或手动调用）。
     *
     * @param request 退款回调请求
     * @return 更新后的订单
     */
    @Transactional
    public OrderVO refundCallback(RefundCallbackRequest request) {
        log.info("退款成功回调，orderNo={}，tradeNo={}，refundAmount={}", request.getOrderNo(), request.getTradeNo(), request.getRefundAmount());
        OrdOrderEntity order = ordOrderMapper.selectOne(new LambdaQueryWrapper<OrdOrderEntity>()
            .eq(OrdOrderEntity::getOrderNo, request.getOrderNo())
            .last("limit 1"));
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        order.setStatus("已退款");
        order.setPayStatus("已退款");
        order.setPayTime(request.getRefundTime() == null ? LocalDateTime.now() : request.getRefundTime());
        ordOrderMapper.updateById(order);
        log.info("退款回调处理完成，orderId={}，orderNo={}", order.getId(), request.getOrderNo());
        return toOrderVO(order);
    }

    /**
     * 查询订单并校验存在。
     *
     * @param id 订单编号
     * @return 订单实体
     */
    private OrdOrderEntity requireActiveOrder(Long id) {
        OrdOrderEntity entity = ordOrderMapper.selectOne(new LambdaQueryWrapper<OrdOrderEntity>()
            .eq(OrdOrderEntity::getId, id)
            .last("limit 1"));
        if (entity == null) {
            throw new BizException(404, "订单不存在");
        }
        return entity;
    }

    /**
     * 转换订单展示对象。
     *
     * @param entity 订单实体
     * @return 订单展示对象
     */
    private OrderVO toOrderVO(OrdOrderEntity entity) {
        OrderVO vo = new OrderVO();
        vo.setId(entity.getId());
        vo.setOrderNo(DefaultValueUtils.defaultIfBlank(entity.getOrderNo(), resolveOrderNo(entity.getId())));
        vo.setBusinessType(DefaultValueUtils.defaultIfBlank(entity.getBusinessType(), businessTypeName(entity.getBizType())));
        vo.setPatientName(DefaultValueUtils.defaultIfBlank(entity.getPatientName(), ""));
        vo.setAmount(formatAmount(DefaultValueUtils.defaultIfNull(entity.getAmount(), BigDecimal.ZERO)));
        vo.setPayStatus(DefaultValueUtils.defaultIfBlank(entity.getPayStatus(), STATUS_PENDING_PAY));
        vo.setCreatedAt(DefaultValueUtils.defaultIfBlank(entity.getCreatedAt(), ""));
        return vo;
    }

    /**
     * 生成订单号。
     *
     * @param id 订单编号
     * @return 订单号
     */
    private String resolveOrderNo(Long id) {
        return "DD" + LocalDate.now().format(ORDER_DATE_FORMATTER) + String.format("%04d", id);
    }

    /**
     * 格式化订单金额。
     *
     * @param amount 订单金额
     * @return 展示金额
     */
    private String formatAmount(BigDecimal amount) {
        return "¥" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * 转换业务类型展示名称。
     *
     * @param bizType 业务类型编码
     * @return 展示名称
     */
    private String businessTypeName(String bizType) {
        return switch (DefaultValueUtils.defaultIfBlank(bizType, DEFAULT_BIZ_TYPE).toUpperCase()) {
            case "CONSULT" -> "图文咨询";
            case "PRESCRIPTION" -> "处方购药";
            case "DRUG" -> "药品配送";
            default -> "门诊预约";
        };
    }

    /**
     * 获取当前展示时间。
     *
     * @return 时分展示值
     */
    private String currentDisplayTime() {
        return LocalTime.now().format(TIME_FORMATTER);
    }

}
