package com.hlw.order.controller;

import com.hlw.common.core.domain.R;
import com.hlw.order.service.MockPaymentService;
import com.hlw.order.service.Order;
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
        return R.ok(List.of());
    }
}
