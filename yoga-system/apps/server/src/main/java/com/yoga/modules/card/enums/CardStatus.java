package com.yoga.modules.card.enums;

/**
 * 会员卡状态机
 *
 * 状态:
 *   PENDING   待激活
 *   ACTIVE    正常
 *   EXPIRED   已过期
 *   NO_REMAIN 次数用尽
 *   REFUNDED  已退款
 */
public enum CardStatus {
    PENDING, ACTIVE, EXPIRED, NO_REMAIN, REFUNDED;

    /**
     * 校验状态流转是否合法
     */
    public boolean canTransitTo(CardStatus target) {
        return switch (this) {
            case PENDING   -> target == ACTIVE || target == REFUNDED;
            case ACTIVE    -> target == EXPIRED || target == NO_REMAIN || target == REFUNDED;
            case EXPIRED, NO_REMAIN -> target == REFUNDED;
            case REFUNDED  -> false; // 终态
        };
    }

    public void checkTransit(CardStatus target) {
        if (!canTransitTo(target)) {
            throw new com.yoga.common.BizException(409,
                    "会员卡状态非法流转: " + this + " -> " + target);
        }
    }
}
