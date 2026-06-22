package com.yoga.modules.statistics.service;

import com.yoga.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final com.yoga.modules.order.mapper.OrderInfoMapper orderInfoMapper;
    private final com.yoga.modules.booking.mapper.BookingMapper bookingMapper;
    private final com.yoga.modules.member.mapper.MemberMapper memberMapper;
    private final com.yoga.modules.course.mapper.CourseScheduleMapper scheduleMapper;
    private final com.yoga.modules.course.mapper.CourseTypeMapper courseTypeMapper;
    private final com.yoga.modules.auth.mapper.AdminUserMapper adminUserMapper;

    /**
     * 营业统计
     *   - 区间: day/week/month/year
     *   - 字段: 营业额(PAID订单的amount), 订单数, 退款额
     */
    public R<Map<String, Object>> revenue(String range) {
        LocalDateTime[] span = calcRange(range);
        var paid = orderInfoMapper.selectList(new LambdaQueryWrapper<com.yoga.modules.order.entity.OrderInfo>()
                .eq(com.yoga.modules.order.entity.OrderInfo::getStatus, "PAID")
                .ge(com.yoga.modules.order.entity.OrderInfo::getPayTime, span[0])
                .lt(com.yoga.modules.order.entity.OrderInfo::getPayTime, span[1]));
        var refunded = orderInfoMapper.selectList(new LambdaQueryWrapper<com.yoga.modules.order.entity.OrderInfo>()
                .eq(com.yoga.modules.order.entity.OrderInfo::getStatus, "REFUNDED")
                .ge(com.yoga.modules.order.entity.OrderInfo::getRefundTime, span[0])
                .lt(com.yoga.modules.order.entity.OrderInfo::getRefundTime, span[1]));

        BigDecimal total = paid.stream()
                .map(com.yoga.modules.order.entity.OrderInfo::getAmount)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal refundAmount = refunded.stream()
                .map(com.yoga.modules.order.entity.OrderInfo::getAmount)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> byType = new HashMap<>();
        Map<String, BigDecimal> amountByType = new HashMap<>();
        for (var o : paid) {
            byType.merge(o.getOrderType(), 1L, Long::sum);
            amountByType.merge(o.getOrderType(),
                    o.getAmount() == null ? BigDecimal.ZERO : o.getAmount(),
                    BigDecimal::add);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", total);
        data.put("refundAmount", refundAmount);
        data.put("orderCount", paid.size());
        data.put("byTypeCount", byType);
        data.put("byTypeAmount", amountByType);
        data.put("range", range);
        return R.ok(data);
    }

    /**
     * 课程统计: 各课程类型 排课数/已约数/上座率
     */
    public R<List<Map<String, Object>>> course(String range) {
        LocalDateTime[] span = calcRange(range);
        var schedules = scheduleMapper.selectList(new LambdaQueryWrapper<com.yoga.modules.course.entity.CourseSchedule>()
                .ge(com.yoga.modules.course.entity.CourseSchedule::getStartTime, span[0])
                .lt(com.yoga.modules.course.entity.CourseSchedule::getStartTime, span[1])
                .ne(com.yoga.modules.course.entity.CourseSchedule::getStatus, "CANCELLED"));
        var types = courseTypeMapper.selectList(null);
        Map<Long, com.yoga.modules.course.entity.CourseType> typeMap = new HashMap<>();
        for (var t : types) typeMap.put(t.getId(), t);

        Map<Long, int[]> agg = new HashMap<>(); // [count, booked, capacity]
        for (var s : schedules) {
            int[] a = agg.computeIfAbsent(s.getCourseTypeId(), k -> new int[3]);
            a[0] += 1;
            a[1] += s.getBookedCount() == null ? 0 : s.getBookedCount();
            a[2] += s.getCapacity() == null ? 0 : s.getCapacity();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (var t : types) {
            int[] a = agg.getOrDefault(t.getId(), new int[3]);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("courseTypeId", t.getId());
            item.put("courseTypeName", t.getName());
            item.put("scheduleCount", a[0]);
            item.put("bookedCount", a[1]);
            item.put("capacity", a[2]);
            item.put("rate", a[2] == 0 ? 0 : Math.round(a[1] * 10000.0 / a[2]) / 100.0);
            result.add(item);
        }
        result.sort((a, b) -> Integer.compare((int) b.get("scheduleCount"), (int) a.get("scheduleCount")));
        return R.ok(result);
    }

    /**
     * 会员统计: 新增/活跃/流失
     *   活跃: 区间内有预约记录的会员
     */
    public R<Map<String, Object>> member(String range) {
        LocalDateTime[] span = calcRange(range);
        // 新增
        Long newCount = memberMapper.selectCount(new LambdaQueryWrapper<com.yoga.modules.member.entity.Member>()
                .ge(com.yoga.modules.member.entity.Member::getCreatedAt, span[0])
                .lt(com.yoga.modules.member.entity.Member::getCreatedAt, span[1]));
        // 活跃: 区间内有 booking 的去重会员数
        var activeList = bookingMapper.selectList(new LambdaQueryWrapper<com.yoga.modules.booking.entity.Booking>()
                .ge(com.yoga.modules.booking.entity.Booking::getBookedAt, span[0])
                .lt(com.yoga.modules.booking.entity.Booking::getBookedAt, span[1]));
        long active = activeList.stream().map(com.yoga.modules.booking.entity.Booking::getMemberId)
                .filter(Objects::nonNull).distinct().count();

        Long total = memberMapper.selectCount(null);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("newCount", newCount);
        data.put("activeCount", active);
        data.put("totalCount", total);
        return R.ok(data);
    }

    /**
     * 教练统计: 课时数(完成的预约数)
     */
    public R<List<Map<String, Object>>> coach(String range) {
        LocalDateTime[] span = calcRange(range);
        var schedules = scheduleMapper.selectList(new LambdaQueryWrapper<com.yoga.modules.course.entity.CourseSchedule>()
                .ge(com.yoga.modules.course.entity.CourseSchedule::getStartTime, span[0])
                .lt(com.yoga.modules.course.entity.CourseSchedule::getStartTime, span[1]));
        var coaches = adminUserMapper.selectList(new LambdaQueryWrapper<com.yoga.modules.auth.entity.AdminUser>()
                .eq(com.yoga.modules.auth.entity.AdminUser::getRole, "COACH"));

        Map<Long, int[]> agg = new HashMap<>();
        for (var s : schedules) {
            int[] a = agg.computeIfAbsent(s.getCoachId(), k -> new int[2]);
            a[0] += 1;
            a[1] += s.getBookedCount() == null ? 0 : s.getBookedCount();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (var c : coaches) {
            int[] a = agg.getOrDefault(c.getId(), new int[2]);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("coachId", c.getId());
            item.put("coachName", c.getRealName());
            item.put("scheduleCount", a[0]);
            item.put("studentCount", a[1]);
            result.add(item);
        }
        result.sort((a, b) -> Integer.compare((int) b.get("scheduleCount"), (int) a.get("scheduleCount")));
        return R.ok(result);
    }

    private LocalDateTime[] calcRange(String range) {
        LocalDate today = LocalDate.now();
        LocalDateTime start;
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        if (range == null) range = "month";
        switch (range) {
            case "day" -> start = today.atStartOfDay();
            case "week" -> start = today.minusDays(today.getDayOfWeek().getValue() - 1L).atStartOfDay();
            case "year" -> start = today.withDayOfYear(1).atStartOfDay();
            default -> start = today.withDayOfMonth(1).atStartOfDay(); // month
        }
        return new LocalDateTime[]{start, end};
    }
}
