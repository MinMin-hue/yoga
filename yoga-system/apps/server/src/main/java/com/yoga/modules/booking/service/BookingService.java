package com.yoga.modules.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yoga.common.BizException;
import com.yoga.common.R;
import com.yoga.common.SecurityContext;
import com.yoga.config.SystemConfig;
import com.yoga.modules.booking.dto.BookingCreateDTO;
import com.yoga.modules.booking.dto.BookingQuery;
import com.yoga.modules.booking.entity.Booking;
import com.yoga.modules.booking.enums.BookingStatus;
import com.yoga.modules.booking.mapper.BookingMapper;
import com.yoga.modules.card.entity.CardType;
import com.yoga.modules.card.entity.MemberCard;
import com.yoga.modules.card.mapper.CardTypeMapper;
import com.yoga.modules.card.mapper.MemberCardMapper;
import com.yoga.modules.card.service.CardTypeService;
import com.yoga.modules.card.service.MemberCardService;
import com.yoga.modules.consumption.entity.ConsumptionRecord;
import com.yoga.modules.consumption.mapper.ConsumptionRecordMapper;
import com.yoga.modules.course.entity.CourseSchedule;
import com.yoga.modules.course.entity.CourseType;
import com.yoga.modules.course.mapper.CourseScheduleMapper;
import com.yoga.modules.course.mapper.CourseTypeMapper;
import com.yoga.modules.member.entity.Member;
import com.yoga.modules.member.mapper.MemberMapper;
import com.yoga.utils.Asserts;
import com.yoga.utils.BizNoGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingMapper bookingMapper;
    private final CourseScheduleMapper scheduleMapper;
    private final CourseTypeMapper courseTypeMapper;
    private final MemberCardMapper memberCardMapper;
    private final CardTypeMapper cardTypeMapper;
    private final MemberMapper memberMapper;
    private final MemberCardService memberCardService;
    private final ConsumptionRecordMapper consumptionRecordMapper;
    private final SystemConfig systemConfig;

    /**
     * 会员预约
     * 流程:
     *   1. 校验排课
     *   2. 校验会员卡(状态/有效期/类型匹配/剩余次数)
     *   3. 扣减课程容量(乐观锁)
     *   4. 扣减会员卡次数
     *   5. 写入预约记录 + 流水
     */
    @Transactional
    public R<Booking> create(BookingCreateDTO dto) {
        Long memberId = SecurityContext.getUserId();
        Asserts.hasLogin();

        // 1. 校验排课
        CourseSchedule schedule = scheduleMapper.selectById(dto.getScheduleId());
        if (schedule == null) throw BizException.notFound("排课不存在");
        if (!"SCHEDULED".equals(schedule.getStatus())) {
            throw BizException.conflict("该课程不可预约");
        }
        int stopMinutes = Integer.parseInt(systemConfig.getOrDefault("booking.stop_minutes", "15"));
        if (LocalDateTime.now().plusMinutes(stopMinutes).isAfter(schedule.getStartTime())) {
            throw BizException.conflict("已超过该课程的预约截止时间");
        }
        // 已预约过该课程?
        Long exist = bookingMapper.selectCount(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getMemberId, memberId)
                .eq(Booking::getScheduleId, dto.getScheduleId())
                .in(Booking::getStatus, List.of("BOOKED", "CHECKED_IN", "COMPLETED")));
        if (exist > 0) throw BizException.conflict("您已预约过该课程");

        // 2. 校验会员卡
        MemberCard card = memberCardMapper.selectById(dto.getCardId());
        if (card == null) throw BizException.notFound("会员卡不存在");
        if (!card.getMemberId().equals(memberId)) throw BizException.forbidden("无权使用该卡");
        if (!"ACTIVE".equals(card.getStatus())) {
            throw BizException.conflict("会员卡不可用, 当前状态: " + card.getStatus());
        }
        if (card.getValidTo() != null && card.getValidTo().isBefore(LocalDateTime.now())) {
            throw BizException.conflict("会员卡已过期");
        }
        CardType cardType = cardTypeMapper.selectById(card.getCardTypeId());
        List<Long> applicable = CardTypeService.splitIds(cardType.getApplicableTypes());
        if (!applicable.isEmpty() && !applicable.contains(schedule.getCourseTypeId())) {
            throw BizException.conflict("该会员卡不能预约此类课程");
        }
        CourseType courseType = courseTypeMapper.selectById(schedule.getCourseTypeId());
        if (card.getRemainTimes() != null && card.getRemainTimes() < courseType.getTimesCost()) {
            throw BizException.conflict("会员卡剩余次数不足");
        }

        // 3. 乐观锁扣减课程容量
        int affected = scheduleMapper.update(null,
                new LambdaUpdateWrapper<CourseSchedule>()
                        .eq(CourseSchedule::getId, schedule.getId())
                        .eq(CourseSchedule::getVersion, schedule.getVersion())
                        .set(CourseSchedule::getBookedCount, schedule.getBookedCount() + 1)
                        .set(CourseSchedule::getVersion, schedule.getVersion() + 1));
        if (affected == 0) throw BizException.conflict("课程容量不足, 请重试");

        // 4. 扣减会员卡次数
        memberCardService.deductTimes(card.getId(), courseType.getTimesCost());

        // 5. 写入预约
        Member member = memberMapper.selectById(memberId);
        Booking b = new Booking();
        b.setBookingNo(BizNoGenerator.bookingNo());
        b.setMemberId(memberId);
        b.setMemberName(member != null ? member.getNickname() : null);
        b.setScheduleId(dto.getScheduleId());
        b.setCardId(card.getId());
        b.setCardNo(card.getCardNo());
        b.setCostTimes(courseType.getTimesCost());
        b.setStatus(BookingStatus.BOOKED.name());
        b.setBookedAt(LocalDateTime.now());
        b.setIsPenalty(0);
        bookingMapper.insert(b);

        // 6. 流水
        ConsumptionRecord r = new ConsumptionRecord();
        r.setMemberId(memberId);
        r.setType("CHECKIN");
        r.setAmount(java.math.BigDecimal.ZERO);
        r.setTimesDelta(-courseType.getTimesCost());
        r.setCardId(card.getId());
        r.setCardNo(card.getCardNo());
        r.setBookingId(b.getId());
        r.setRemark("预约课程: " + schedule.getCourseTypeName());
        consumptionRecordMapper.insert(r);

        return R.ok(b);
    }

    /**
     * 取消预约
     * 规则:
     *   - 课程开始前 N 分钟内取消: 扣减 N 次违约次数 (默认 1 次)
     *   - 课程开始前可免费取消的窗口外: 扣次数
     */
    @Transactional
    public R<Booking> cancel(Long bookingId, String reason) {
        Long memberId = SecurityContext.getUserId();
        Asserts.hasLogin();
        Booking b = bookingMapper.selectById(bookingId);
        if (b == null) throw BizException.notFound("预约不存在");
        if (!b.getMemberId().equals(memberId)) throw BizException.forbidden("无权操作");
        BookingStatus current = BookingStatus.valueOf(b.getStatus());
        current.checkTransit(BookingStatus.CANCELLED);

        CourseSchedule schedule = scheduleMapper.selectById(b.getScheduleId());
        int cancelMinutes = Integer.parseInt(systemConfig.getOrDefault("booking.cancel_minutes", "120"));
        int penalty = Integer.parseInt(systemConfig.getOrDefault("booking.no_show_penalty", "1"));
        boolean isLateCancel = schedule != null
                && LocalDateTime.now().isAfter(schedule.getStartTime().minusMinutes(cancelMinutes));

        b.setStatus(BookingStatus.CANCELLED.name());
        b.setCancelledAt(LocalDateTime.now());
        b.setCancelReason(reason);
        b.setIsPenalty(isLateCancel ? 1 : 0);
        bookingMapper.updateById(b);

        // 释放课程容量
        if (schedule != null) {
            scheduleMapper.update(null,
                    new LambdaUpdateWrapper<CourseSchedule>()
                            .eq(CourseSchedule::getId, schedule.getId())
                            .set(CourseSchedule::getBookedCount,
                                    Math.max(0, schedule.getBookedCount() - 1))
                            .set(CourseSchedule::getVersion, schedule.getVersion() + 1));
        }
        // 释放会员卡次数(仅在已扣减的情况下)
        memberCardService.releaseTimes(b.getCardId(), b.getCostTimes());

        if (isLateCancel) {
            // 违约: 再额外扣 N 次
            try {
                memberCardService.deductTimes(b.getCardId(), penalty);
                ConsumptionRecord r = new ConsumptionRecord();
                r.setMemberId(memberId);
                r.setType("PENALTY");
                r.setAmount(java.math.BigDecimal.ZERO);
                r.setTimesDelta(-penalty);
                r.setCardId(b.getCardId());
                r.setCardNo(b.getCardNo());
                r.setBookingId(b.getId());
                r.setRemark("违约取消: " + penalty + " 次");
                consumptionRecordMapper.insert(r);
            } catch (Exception ignored) {
                // 次数不足时仅记录, 不阻断
            }
        }
        return R.ok(b);
    }

    /**
     * 签到(管理员扫码/手动)
     */
    @Transactional
    public R<Booking> checkIn(Long bookingId) {
        Booking b = bookingMapper.selectById(bookingId);
        if (b == null) throw BizException.notFound("预约不存在");
        BookingStatus current = BookingStatus.valueOf(b.getStatus());
        if (current != BookingStatus.BOOKED) {
            throw BizException.conflict("该预约不可签到, 状态: " + b.getStatus());
        }
        CourseSchedule schedule = scheduleMapper.selectById(b.getScheduleId());
        if (schedule == null) throw BizException.notFound("排课不存在");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earliest = schedule.getStartTime().minusMinutes(schedule.getCheckinBefore());
        LocalDateTime latest = schedule.getEndTime();
        if (now.isBefore(earliest)) throw BizException.conflict("未到签到时间");
        if (now.isAfter(latest)) throw BizException.conflict("已过签到时间");

        current.checkTransit(BookingStatus.CHECKED_IN);
        b.setStatus(BookingStatus.CHECKED_IN.name());
        b.setCheckedInAt(now);
        bookingMapper.updateById(b);
        return R.ok(b);
    }

    /**
     * 核销(课程完成时定时任务 / 管理员手动)
     */
    @Transactional
    public R<Booking> complete(Long bookingId) {
        Booking b = bookingMapper.selectById(bookingId);
        if (b == null) throw BizException.notFound("预约不存在");
        BookingStatus current = BookingStatus.valueOf(b.getStatus());
        current.checkTransit(BookingStatus.COMPLETED);
        b.setStatus(BookingStatus.COMPLETED.name());
        b.setCompletedAt(LocalDateTime.now());
        bookingMapper.updateById(b);
        return R.ok(b);
    }

    /**
     * 爽约标记(定时任务, 课程开始 10 分钟未签到)
     */
    @Transactional
    public int markNoShow() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        List<Booking> list = bookingMapper.selectList(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getStatus, BookingStatus.BOOKED.name()));
        int n = 0;
        for (Booking b : list) {
            CourseSchedule s = scheduleMapper.selectById(b.getScheduleId());
            if (s != null && s.getStartTime().isBefore(cutoff)) {
                BookingStatus.BOOKED.checkTransit(BookingStatus.NO_SHOW);
                b.setStatus(BookingStatus.NO_SHOW.name());
                b.setIsPenalty(1);
                bookingMapper.updateById(b);
                // 释放容量
                scheduleMapper.update(null,
                        new LambdaUpdateWrapper<CourseSchedule>()
                                .eq(CourseSchedule::getId, s.getId())
                                .set(CourseSchedule::getBookedCount, Math.max(0, s.getBookedCount() - 1))
                                .set(CourseSchedule::getVersion, s.getVersion() + 1));
                n++;
            }
        }
        return n;
    }

    public R<Map<String, Object>> page(BookingQuery q) {
        Page<Booking> page = new Page<>(q.getPageNum(), q.getPageSize());
        LambdaQueryWrapper<Booking> w = new LambdaQueryWrapper<Booking>()
                .orderByDesc(Booking::getId);
        if (q.getMemberId() != null) w.eq(Booking::getMemberId, q.getMemberId());
        if (q.getScheduleId() != null) w.eq(Booking::getScheduleId, q.getScheduleId());
        if (q.getStatus() != null && !q.getStatus().isBlank()) w.eq(Booking::getStatus, q.getStatus());
        if (q.getKeyword() != null && !q.getKeyword().isBlank()) {
            w.and(ww -> ww.like(Booking::getMemberName, q.getKeyword())
                    .or().like(Booking::getBookingNo, q.getKeyword()));
        }
        var result = bookingMapper.selectPage(page, w);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return R.ok(data);
    }

    public R<Map<String, Object>> myBookings(String status) {
        Long memberId = SecurityContext.getUserId();
        Asserts.hasLogin();
        LambdaQueryWrapper<Booking> w = new LambdaQueryWrapper<Booking>()
                .eq(Booking::getMemberId, memberId)
                .orderByDesc(Booking::getId);
        if (status != null && !status.isBlank()) w.eq(Booking::getStatus, status);
        List<Booking> list = bookingMapper.selectList(w);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        return R.ok(data);
    }

    public R<Map<String, Object>> bySchedule(Long scheduleId) {
        List<Booking> list = bookingMapper.selectList(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getScheduleId, scheduleId)
                .orderByDesc(Booking::getId));
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        return R.ok(data);
    }
}
