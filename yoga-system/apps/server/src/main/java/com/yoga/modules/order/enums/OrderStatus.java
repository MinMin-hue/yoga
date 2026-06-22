package com.yoga.modules.order.enums;

public enum OrderStatus {
    PENDING,    // 待支付
    PAID,       // 已支付
    CANCELLED,  // 已取消
    REFUNDED;   // 已退款

    public boolean canTransitTo(OrderStatus target) {
        return switch (this) {
            case PENDING -> target == PAID || target == CANCELLED;
            case PAID    -> target == REFUNDED;
            case CANCELLED, REFUNDED -> false;
        };
    }

    public void checkTransit(OrderStatus target) {
        if (!canTransitTo(target)) {
            throw new com.yoga.common.BizException(409, "订单状态非法流转: " + this + " -> " + target);
        }
    }
}
