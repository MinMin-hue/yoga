package com.yoga.modules.card.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CardTypeUpsertDTO {

    private Long id;

    @NotBlank(message = "卡名称不能为空")
    private String name;

    @NotBlank(message = "卡类型不能为空")
    private String cardKind; // TIME / TIMES / MIXED

    private BigDecimal price = BigDecimal.ZERO;
    private Integer validDays;
    private Integer totalTimes;
    private List<Long> applicableTypes;
    private String description;
    private Integer status = 1;
    private Integer sort = 0;
}
