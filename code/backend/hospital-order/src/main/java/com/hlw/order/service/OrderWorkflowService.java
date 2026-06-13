package com.hlw.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.tenant.TenantContext;
import com.hlw.common.mq.core.MqProducer;
import com.hlw.common.mq.model.MqMessage;
import com.hlw.order.dto.CreateOrderRequest;
import com.hlw.order.dto.PayOrderRequest;
import com.hlw.order.entity.OrdOrderEntity;
import com.hlw.order.mapper.OrdOrderMapper;
import com.hlw.order.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * 订单工作流服务，负责订单创建、支付状态变更和支付事件发布。
 */
@Service
@RequiredArgsConstructor
public class OrderWorkflowService {
    private static final Logger log = LoggerFactory.getLogger(OrderWorkflowService.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter ORDER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final long DEFAULT_PATIENT_ID = 1L;
    private static final String DEFAULT_BIZ_TYPE = "APPOINTMENT";
    private static final String DEFAULT_PATIENT_NAME = "张小满";
    private static final String DEFAULT_PAY_METHOD = "MOCK_PAY";
    private static final String STATUS_PENDING_PAY = "待支付";
    private static final String STATUS_PAID = "已支付";

    /** 订单数据访问组件。 */
    private final OrdOrderMapper ordOrderMapper;
    /** 消息生产者。 */
    private final MqProducer mqProducer;

    /**
     * 查询订单列表。
     *
     * @return 订单展示列表
     */
    public List<OrderVO> listOrders() {
        log.info("查询订单列表");
        return ordOrderMapper.selectList(activeOrderWrapper())
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
        ensureBusinessTenantContext("订单模块操作缺少有效租户上下文");
        String bizType = defaultIfBlank(request.getBizType(), defaultIfBlank(request.getBusinessType(), DEFAULT_BIZ_TYPE));
        String businessType = businessTypeName(bizType);
        Long bizId = defaultLong(request.getBizId(), 0L);
        Long patientId = defaultLong(request.getPatientId(), DEFAULT_PATIENT_ID);
        String patientName = defaultIfBlank(request.getPatientName(), DEFAULT_PATIENT_NAME);
        BigDecimal amount = defaultDecimal(request.getAmount(), new BigDecimal("25.00"));
        String createdAt = defaultIfBlank(request.getCreatedAt(), currentDisplayTime());
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
        entity.setDeleted(0);
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
        ensureBusinessTenantContext("订单模块操作缺少有效租户上下文");
        String payMethod = request == null ? DEFAULT_PAY_METHOD : defaultIfBlank(request.getPayMethod(), DEFAULT_PAY_METHOD);
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
        mqProducer.publish(new MqMessage("order.paid", "{\"orderId\":" + id + "}", 0, 0, 3));
        return toOrderVO(order);
    }

    /**
     * 构造激活订单查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<OrdOrderEntity> activeOrderWrapper() {
        return new LambdaQueryWrapper<OrdOrderEntity>().eq(OrdOrderEntity::getDeleted, 0);
    }

    /**
     * 校验当前请求处于有效业务租户上下文。
     *
     * @param message 不满足条件时的错误消息
     */
    private void ensureBusinessTenantContext(String message) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId <= 0L || TenantContext.isPlatformRequest()) {
            throw new BizException(403, message);
        }
    }

    /**
     * 查询订单并校验存在。
     *
     * @param id 订单编号
     * @return 订单实体
     */
    private OrdOrderEntity requireActiveOrder(Long id) {
        OrdOrderEntity entity = ordOrderMapper.selectOne(new LambdaQueryWrapper<OrdOrderEntity>()
            .eq(OrdOrderEntity::getDeleted, 0)
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
        vo.setKey(String.valueOf(entity.getId()));
        vo.setId(entity.getId());
        vo.setOrderNo(defaultIfBlank(entity.getOrderNo(), resolveOrderNo(entity.getId())));
        vo.setBusinessType(defaultIfBlank(entity.getBusinessType(), businessTypeName(entity.getBizType())));
        vo.setPatientName(defaultIfBlank(entity.getPatientName(), ""));
        vo.setAmount(formatAmount(defaultDecimal(entity.getAmount(), BigDecimal.ZERO)));
        vo.setPayStatus(defaultIfBlank(entity.getPayStatus(), STATUS_PENDING_PAY));
        vo.setCreatedAt(defaultIfBlank(entity.getCreatedAt(), ""));
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
        return switch (defaultIfBlank(bizType, DEFAULT_BIZ_TYPE).toUpperCase()) {
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

    /**
     * 设置默认字符串。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后的字符串
     */
    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    /**
     * 设置默认长整型。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后的长整型
     */
    private Long defaultLong(Long value, Long defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * 设置默认金额。
     *
     * @param value 原始金额
     * @param defaultValue 默认金额
     * @return 处理后的金额
     */
    private BigDecimal defaultDecimal(BigDecimal value, BigDecimal defaultValue) {
        return value == null ? defaultValue : value;
    }
}
