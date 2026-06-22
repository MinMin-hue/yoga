package com.yoga.modules.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderCreateDTO {

    @NotNull
    private String orderType;     // PURCHASE_CARD / RECHARGE / SINGLE_COURSE

    private Long cardTypeId;      // 购卡时填
    private Long courseTypeId;    // 单课购买时填

    private BigDecimal amount;
    private String remark;
}
