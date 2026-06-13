package com.hlw.order.controller;

import com.hlw.common.core.domain.R;
import com.hlw.common.core.jdbc.DemoDataQuery;
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
    private final DemoDataQuery demoDataQuery;

    /**
     * 构造订单控制器。
     *
     * @param mockPaymentService 模拟支付服务
     * @param demoDataQuery 演示数据查询器
     */
    public OrderController(MockPaymentService mockPaymentService, DemoDataQuery demoDataQuery) {
        this.mockPaymentService = mockPaymentService;
        this.demoDataQuery = demoDataQuery;
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
        return R.ok(demoDataQuery.list("订单列表", """
            SELECT id::text AS key,
                   order_no AS "orderNo",
                   business_type AS "businessType",
                   patient_name AS "patientName",
                   '¥' || to_char(amount, 'FM999999990.00') AS amount,
                   pay_status AS "payStatus",
                   created_at AS "createdAt"
            FROM ord_order
            WHERE deleted = 0
            ORDER BY id
            """));
    }
}
