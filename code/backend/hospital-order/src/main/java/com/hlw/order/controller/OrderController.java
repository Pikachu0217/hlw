package com.hlw.order.controller;

import com.hlw.common.core.domain.R;
import com.hlw.order.dto.CreateOrderRequest;
import com.hlw.order.dto.PayCallbackRequest;
import com.hlw.order.dto.PayOrderRequest;
import com.hlw.order.dto.RefundCallbackRequest;
import com.hlw.order.service.OrderWorkflowService;
import com.hlw.order.vo.OrderVO;
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
 * 订单控制器，提供创建、支付和查询接口。
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    private final OrderWorkflowService orderWorkflowService;

    /**
     * 构造订单控制器。
     *
     * @param orderWorkflowService 订单工作流服务
     */
    public OrderController(OrderWorkflowService orderWorkflowService) {
        this.orderWorkflowService = orderWorkflowService;
    }

    /**
     * 创建订单。
     *
     * @param request 订单创建请求
     * @return 创建结果
     */
    @PostMapping("/orders")
    public R<OrderVO> create(@Valid @RequestBody CreateOrderRequest request) {
        return R.ok(orderWorkflowService.create(request));
    }

    /**
     * 执行模拟支付。
     *
     * @param id 订单编号
     * @param request 支付请求
     * @return 支付后的订单
     */
    @PostMapping("/orders/{id}/pay")
    public R<OrderVO> pay(@PathVariable Long id, @RequestBody PayOrderRequest request) {
        return R.ok(orderWorkflowService.pay(id, request));
    }

    /**
     * 查询订单列表。
     *
     * @return 订单列表
     */
    @GetMapping("/orders")
    public R<List<OrderVO>> orders() {
        log.info("查询订单列表");
        return R.ok(orderWorkflowService.listOrders());
    }

    /**
     * 支付成功回调（供支付网关或手动调用）。
     *
     * @param request 回调请求
     * @return 更新后的订单
     */
    @PostMapping("/callback/pay-success")
    public R<OrderVO> payCallback(@RequestBody PayCallbackRequest request) {
        log.info("支付成功回调，orderNo={}", request.getOrderNo());
        return R.ok(orderWorkflowService.payCallback(request));
    }

    /**
     * 退款成功回调（供支付网关或手动调用）。
     *
     * @param request 退款回调请求
     * @return 更新后的订单
     */
    @PostMapping("/callback/refund-success")
    public R<OrderVO> refundCallback(@RequestBody RefundCallbackRequest request) {
        log.info("退款成功回调，orderNo={}", request.getOrderNo());
        return R.ok(orderWorkflowService.refundCallback(request));
    }
}
