package com.yoga.modules.booking.dto;

import lombok.Data;

@Data
public class BookingQuery {
    private Long memberId;
    private Long scheduleId;
    private String status;
    private String keyword; // 会员手机号/姓名
    private Long pageNum = 1L;
    private Long pageSize = 20L;
}
