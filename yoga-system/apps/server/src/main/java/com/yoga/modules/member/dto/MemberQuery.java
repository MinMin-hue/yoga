package com.yoga.modules.member.dto;

import lombok.Data;

@Data
public class MemberQuery {
    private String keyword;
    private Integer status;
    private Long pageNum = 1L;
    private Long pageSize = 20L;
}
