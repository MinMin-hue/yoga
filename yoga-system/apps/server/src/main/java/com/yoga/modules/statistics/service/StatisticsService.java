package com.yoga.modules.statistics.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yoga.common.utils.PageUtils;
import com.yoga.common.utils.Query;
import com.yoga.modules.admin.entity.AdminUser;
import com.yoga.modules.admin.mapper.AdminUserMapper;
import com.yoga.modules.booking.entity.Booking;
import com.yoga.modules.booking.mapper.BookingMapper;
import com.yoga.modules.course.entity.CourseSchedule;
import com.yoga.modules.course.entity.CourseType;
import com.yoga.modules.course.mapper.CourseScheduleMapper;
import com.yoga.modules.course.mapper.CourseTypeMapper;
import com.yoga.modules.member.entity.Member;
import com.yoga.modules.member.mapper.MemberMapper;
import com.yoga.modules.order.entity.OrderInfo;
import com.yoga.modules.order.mapper.OrderInfoMapper;
import com.yoga.modules.statistics.dto.StatisticsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired private OrderInfoMapper orderInfoMapper;
    @Autowired private BookingMapper bookingMapper;
    @Autowired private CourseScheduleMapper courseScheduleMapper;
    @Autowired private CourseTypeMapper courseTypeMapper;
    @Autowired private MemberMapper memberMapper;
    @Autowired private AdminUserMapper adminUserMapper;

    public StatisticsDTO overview() {
        StatisticsDTO dto = new StatisticsDTO();
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        LocalDateTime startOfMonth = firstOfMonth.atStartOfDay();

        // 今日营收
        BigDecimal todayRevenue = orderInfoMapper.selectList(
            new QueryWrapper<OrderInfo>()
                .eq("status", "PAID")
                .between("paid_at", startOfDay, endOfDay)
        ).stream().map(OrderInfo::getAmount).filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTodayRevenue(todayRevenue);

        // 月累计营收
        BigDecimal monthRevenue = orderInfoMapper.selectList(
            new QueryWrapper<OrderInfo>()
                .eq("status", "PAID")
                .ge("paid_at", startOfMonth)
        ).stream().map(OrderInfo::getAmount).filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setMonthRevenue(monthRevenue);

        // 今日预约
        Long todayBookings = bookingMapper.selectCount(
            new QueryWrapper<Booking>().between("booked_at", startOfDay, endOfDay)
        );
        dto.setTodayBookings(todayBookings);

        // 今日已签到
        Long todayCheckIns = bookingMapper.selectCount(
            new QueryWrapper<Booking>()
                .between("check_in_at", startOfDay, endOfDay)
                .isNotNull("check_in_at")
        );
        dto.setTodayCheckIns(todayCheckIns);

        // 总会员 / 活跃会员
        Long totalMembers = memberMapper.selectCount(null);
        Long activeMembers = memberMapper.selectCount(
            new QueryWrapper<Member>().eq("status", 1)
        );
        dto.setTotalMembers(totalMembers);
        dto.setActiveMembers(activeMembers);

        // 近 7 天出勤率
        LocalDateTime sevenDaysAgo = today.minusDays(7).atStartOfDay();
        Long totalIn7 = bookingMapper.selectCount(
            new QueryWrapper<Booking>().ge("booked_at", sevenDaysAgo)
        );
        Long checkedIn7 = bookingMapper.selectCount(
            new QueryWrapper<Booking>()
                .ge("booked_at", sevenDaysAgo)
                .isNotNull("check_in_at")
        );
        double rate = totalIn7 == 0 ? 0 : Math.round(checkedIn7 * 10000.0 / totalIn7) / 100.0;
        dto.setAttendanceRate(rate);

        // Top 5 课程上座率
        dto.setTopCourses(topCourses(5));
        // Top 5 教练课时
        dto.setTopCoaches(topCoaches(5));
        return dto;
    }

    public StatisticsDTO range(int days) {
        StatisticsDTO dto = new StatisticsDTO();
        LocalDateTime from = LocalDate.now().minusDays(days).atStartOfDay();
        // 营收
        BigDecimal revenue = orderInfoMapper.selectList(
            new QueryWrapper<OrderInfo>().eq("status", "PAID").ge("paid_at", from)
        ).stream().map(OrderInfo::getAmount).filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setRevenue(revenue);
        // 预约 / 签到
        Long bookings = bookingMapper.selectCount(
            new QueryWrapper<Booking>().ge("booked_at", from)
        );
        Long checkedIn = bookingMapper.selectCount(
            new QueryWrapper<Booking>().ge("booked_at", from).isNotNull("check_in_at")
        );
        dto.setBookings(bookings);
        dto.setCheckInRate(bookings == 0 ? 0 : Math.round(checkedIn * 10000.0 / bookings) / 100.0);
        // 新增会员
        Long newMembers = memberMapper.selectCount(
            new QueryWrapper<Member>().ge("created_at", from)
        );
        Long active = memberMapper.selectCount(new QueryWrapper<Member>().eq("status", 1));
        dto.setNewMembers(newMembers);
        dto.setActiveMembers(active);
        // 出勤率
        Long totalInRange = bookings;
        Long checkIn = checkedIn;
        double attendance = totalInRange == 0 ? 0 : Math.round(checkIn * 10000.0 / totalInRange) / 100.0;
        dto.setAttendanceRate(attendance);
        // 课程数
        Long totalCourses = courseScheduleMapper.selectCount(
            new QueryWrapper<CourseSchedule>().ge("start_time", from)
        );
        dto.setTotalCourses(totalCourses);
        // 排名
        dto.setTopCourses(topCourses(10));
        dto.setTopCoaches(topCoaches(10));
        // 订单类型分布
        dto.setOrderTypeDist(orderTypeDist(from));
        return dto;
    }

    private List<StatisticsDTO.CourseRank> topCourses(int n) {
        // 按课程类型聚合
        List<Booking> bookings = bookingMapper.selectList(null);
        Map<Long, int[]> map = new HashMap<>(); // [booked, capacity]
        for (Booking b : bookings) {
            CourseSchedule s = courseScheduleMapper.selectById(b.getScheduleId());
            if (s == null) continue;
            map.computeIfAbsent(s.getCourseTypeId(), k -> new int[2]);
            int[] arr = map.get(s.getCourseTypeId());
            arr[0]++;
        }
        // 容量
        for (Map.Entry<Long, int[]> e : map.entrySet()) {
            List<CourseSchedule> list = courseScheduleMapper.selectList(
                new QueryWrapper<CourseSchedule>().eq("course_type_id", e.getKey())
            );
            int cap = list.stream().mapToInt(s -> s.getCapacity() == null ? 0 : s.getCapacity()).sum();
            e.getValue()[1] = cap;
        }
        return map.entrySet().stream()
            .map(en -> {
                StatisticsDTO.CourseRank r = new StatisticsDTO.CourseRank();
                r.setCourseTypeId(en.getKey());
                CourseType t = courseTypeMapper.selectById(en.getKey());
                r.setCourseTypeName(t == null ? "未知" : t.getName());
                int cap = en.getValue()[1];
                double rate = cap == 0 ? 0 : Math.round(en.getValue()[0] * 10000.0 / cap) / 100.0;
                r.setRate(rate);
                return r;
            })
            .sorted(Comparator.comparingDouble(StatisticsDTO.CourseRank::getRate).reversed())
            .limit(n)
            .collect(Collectors.toList());
    }

    private List<StatisticsDTO.CoachRank> topCoaches(int n) {
        List<CourseSchedule> schedules = courseScheduleMapper.selectList(null);
        Map<Long, Long> hoursMap = schedules.stream()
            .filter(s -> s.getCoachId() != null)
            .collect(Collectors.groupingBy(CourseSchedule::getCoachId, Collectors.counting()));
        return hoursMap.entrySet().stream()
            .map(en -> {
                StatisticsDTO.CoachRank r = new StatisticsDTO.CoachRank();
                r.setCoachId(en.getKey());
                AdminUser u = adminUserMapper.selectById(en.getKey());
                r.setCoachName(u == null ? "未知" : (u.getRealName() != null ? u.getRealName() : u.getUsername()));
                r.setHours(en.getValue() * 1L);
                return r;
            })
            .sorted(Comparator.comparingLong(StatisticsDTO.CoachRank::getHours).reversed())
            .limit(n)
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> orderTypeDist(LocalDateTime from) {
        List<OrderInfo> list = orderInfoMapper.selectList(
            new QueryWrapper<OrderInfo>().eq("status", "PAID").ge("paid_at", from)
        );
        Map<String, BigDecimal> sum = new HashMap<>();
        for (OrderInfo o : list) {
            if (o.getAmount() == null) continue;
            sum.merge(o.getOrderType() == null ? "OTHER" : o.getOrderType(), o.getAmount(), BigDecimal::add);
        }
        List<Map<String, Object>> res = new ArrayList<>();
        sum.forEach((k, v) -> {
            Map<String, Object> m = new HashMap<>();
            m.put("name", k); m.put("value", v);
            res.add(m);
        });
        return res;
    }

    public PageUtils page(Map<String, Object> params) {
        Query<CourseSchedule> q = new Query<>(params);
        return new PageUtils(courseScheduleMapper.selectPage(q, null));
    }
}
