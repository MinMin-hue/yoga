package com.yoga.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yoga.modules.system.entity.SystemConfigDB;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfigDB> {
}
