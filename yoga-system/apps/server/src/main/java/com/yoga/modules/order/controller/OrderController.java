package com.yoga.modules.order.controller;

import com.yoga.common.R;
import com.yoga.modules.order.dto.OrderCreateDTO;
import com.yoga.modules.order.dto.OrderPayDTO;
import com.yoga.modules.order.dto.OrderQuery;
import com.yoga.modules.order.entity.OrderInfo;
import com.yoga.modules.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ==================== H5 ====================
    @PostMapping("/h5/order/create")
    public R<OrderInfo> create(@RequestBody @Valid OrderCreateDTO dto) {
        return orderService.create(dto);
    }

    @PostMapping("/h5/order/cancel/{id}")
    public R<OrderInfo> h5Cancel(@PathVariable Long id) {
        return orderService.cancel(id);
    }

    // ==================== 管理后台 ====================
    @PostMapping("/admin/order/page")
    public R<Map<String, Object>> page(@RequestBody OrderQuery q) {
        return orderService.page(q);
    }

    @GetMapping("/admin/order/{id}")
    public R<OrderInfo> detail(@PathVariable Long id) {
        return orderService.detail(id);
    }

    @PostMapping("/admin/order/confirm-pay")
    public R<OrderInfo> confirmPay(@RequestBody @Valid OrderPayDTO dto) {
        return orderService.confirmOfflinePay(dto);
    }

    @PostMapping("/admin/order/cancel/{id}")
    public R<OrderInfo> adminCancel(@PathVariable Long id) {
        return orderService.cancel(id);
    }

    @PostMapping("/admin/order/refund/{id}")
    public R<OrderInfo> refund(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return orderService.refund(id, reason);
    }
}
