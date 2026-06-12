package com.hlw.order.controller;

import com.hlw.common.core.domain.R;
import com.hlw.order.service.MockPaymentService;
import com.hlw.order.service.Order;
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
 * 订单控制器，提供创建、支付和查询接口。
 */
@RestController
@RequestMapping("/order")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final MockPaymentService mockPaymentService;

    /**
     * 构造订单控制器。
     *
     * @param mockPaymentService 模拟支付服务
     */
    public OrderController(MockPaymentService mockPaymentService) {
        this.mockPaymentService = mockPaymentService;
    }

    /**
     * 创建订单。
     *
     * @param command 订单创建命令
     * @return 创建结果
     */
    @PostMapping("/orders")
    public R<Map<String, Object>> create(@RequestBody Map<String, Object> command) {
        return R.ok(command);
    }

    /**
     * 执行模拟支付。
     *
     * @param id 订单编号
     * @param command 支付命令
     * @return 支付后的订单
     */
    @PostMapping("/orders/{id}/pay")
    public R<Order> pay(@PathVariable Long id, @RequestBody Map<String, String> command) {
        return R.ok(mockPaymentService.pay(id, command.getOrDefault("payMethod", "MOCK_PAY")));
    }

    /**
     * 查询订单列表。
     *
     * @return 订单列表
     */
    @GetMapping("/orders")
    public R<List<Map<String, Object>>> orders() {
        log.info("查询订单列表");
        return R.ok(List.of(
            Map.of("key", "1", "orderNo", "DD20260612001", "businessType", "门诊预约", "patientName", "赵晓岚", "amount", "¥58.00", "payStatus", "已支付", "createdAt", "09:12"),
            Map.of("key", "2", "orderNo", "DD20260612002", "businessType", "图文咨询", "patientName", "沈博远", "amount", "¥39.90", "payStatus", "待支付", "createdAt", "09:35")
        ));
    }
}
