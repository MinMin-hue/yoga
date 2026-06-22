package com.yoga.modules.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MemberUpsertDTO {

    private Long id;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String nickname;
    private String avatar;
    private Integer gender;
    private LocalDate birthday;
    private Integer status = 1;
    private String remark;
}
