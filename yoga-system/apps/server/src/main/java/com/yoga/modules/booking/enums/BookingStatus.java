package com.yoga.modules.booking.enums;

import com.yoga.common.BizException;

public enum BookingStatus {
    BOOKED,        // 已预约
    CANCELLED,     // 已取消
    CHECKED_IN,    // 已签到
    COMPLETED,     // 已核销
    NO_SHOW;       // 已爽约

    public boolean canTransitTo(BookingStatus target) {
        return switch (this) {
            case BOOKED     -> target == CANCELLED || target == CHECKED_IN || target == NO_SHOW;
            case CHECKED_IN -> target == COMPLETED;
            case CANCELLED, COMPLETED, NO_SHOW -> false;
        };
    }

    public void checkTransit(BookingStatus target) {
        if (!canTransitTo(target)) {
            throw new BizException(409, "预约状态非法流转: " + this + " -> " + target);
        }
    }
}
