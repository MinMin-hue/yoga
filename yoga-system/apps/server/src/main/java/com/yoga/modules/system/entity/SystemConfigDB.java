package com.yoga.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据库表 system_config 对应实体
 * (注意: 与 com.yoga.config.SystemConfig 区分, 那个是运行时配置类)
 */
@Data
@TableName("system_config")
public class SystemConfigDB {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private LocalDateTime updatedAt;
}
