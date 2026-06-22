package com.yoga.modules.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_info")
public class OrderInfo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long memberId;
    private String memberName;
    private String orderType;
    private Long cardTypeId;
    private String cardTypeName;
    private Long courseTypeId;
    private String courseTypeName;
    private BigDecimal amount;
    private String payMethod;
    private String status;
    private LocalDateTime payTime;
    private Long paidBy;
    private LocalDateTime refundTime;
    private String refundReason;
    private String remark;
    private LocalDateTime expireAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
