package com.yoga.modules.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yoga.modules.booking.entity.Booking;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookingMapper extends BaseMapper<Booking> {
}
