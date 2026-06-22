package com.yoga.modules.consumption.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("consumption_record")
public class ConsumptionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String type;
    private BigDecimal amount;
    private Integer timesDelta;
    private Long cardId;
    private String cardNo;
    private Long bookingId;
    private Long orderId;
    private String remark;
    private LocalDateTime createdAt;
}
