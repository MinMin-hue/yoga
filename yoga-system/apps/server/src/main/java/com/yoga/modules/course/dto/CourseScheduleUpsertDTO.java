package com.yoga.modules.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CourseScheduleUpsertDTO {

    private Long id;

    @NotNull
    private Long courseTypeId;

    @NotNull
    private Long coachId;

    private Long roomId;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    private Integer capacity = 20;
    private Integer checkinBefore = 15;
    private String remark;

    /** 周期排课专用 */
    private String repeatType; // ONCE / DAILY / WEEKLY
    private Integer repeatCount;
    private List<Integer> weekDays;
}
