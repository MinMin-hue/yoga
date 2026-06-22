package com.yoga.modules.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_type")
public class CourseType {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String category;
    private Integer timesCost;
    private String cover;
    private String description;
    private Integer status;
    private Integer sort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
