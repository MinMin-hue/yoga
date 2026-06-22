package com.yoga.modules.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderPayDTO {

    @NotNull
    private Long orderId;

    @NotNull
    private String payMethod; // OFFLINE

    private String remark;
}
