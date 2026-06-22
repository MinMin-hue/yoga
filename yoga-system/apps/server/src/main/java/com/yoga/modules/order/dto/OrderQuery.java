package com.yoga.modules.order.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OrderQuery {
    private String orderNo;
    private Long memberId;
    private String status;
    private String orderType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long pageNum = 1L;
    private Long pageSize = 20L;
}
