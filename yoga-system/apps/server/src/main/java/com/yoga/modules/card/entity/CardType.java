package com.yoga.modules.card.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("card_type")
public class CardType {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String cardKind;
    private BigDecimal price;
    private Integer validDays;
    private Integer totalTimes;
    private String applicableTypes;
    private String description;
    private Integer status;
    private Integer sort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
