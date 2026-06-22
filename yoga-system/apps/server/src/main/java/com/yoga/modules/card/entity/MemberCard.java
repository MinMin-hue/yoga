package com.yoga.modules.card.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("member_card")
public class MemberCard {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String cardNo;
    private Long memberId;
    private Long cardTypeId;
    private String cardTypeName;
    private BigDecimal price;
    private String cardKind;
    private Integer validDays;
    private Integer totalTimes;
    private Integer remainTimes;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String status;
    private LocalDateTime activatedAt;
    private LocalDateTime refundedAt;
    private Long orderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
