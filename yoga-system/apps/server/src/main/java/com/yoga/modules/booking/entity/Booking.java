package com.yoga.modules.booking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("booking")
public class Booking {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String bookingNo;
    private Long memberId;
    private String memberName;
    private Long scheduleId;
    private Long cardId;
    private String cardNo;
    private Integer costTimes;
    private String status;
    private LocalDateTime bookedAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;
    private LocalDateTime checkedInAt;
    private LocalDateTime completedAt;
    private Integer isPenalty;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
