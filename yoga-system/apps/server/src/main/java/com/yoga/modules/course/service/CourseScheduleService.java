package com.yoga.modules.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yoga.common.BizException;
import com.yoga.common.R;
import com.yoga.modules.auth.entity.AdminUser;
import com.yoga.modules.auth.mapper.AdminUserMapper;
import com.yoga.modules.course.dto.CourseScheduleUpsertDTO;
import com.yoga.modules.course.entity.CourseSchedule;
import com.yoga.modules.course.entity.CourseType;
import com.yoga.modules.course.entity.Room;
import com.yoga.modules.course.mapper.CourseScheduleMapper;
import com.yoga.modules.course.mapper.CourseTypeMapper;
import com.yoga.modules.course.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseScheduleService {

    private final CourseScheduleMapper scheduleMapper;
    private final CourseTypeMapper courseTypeMapper;
    private final RoomMapper roomMapper;
    private final AdminUserMapper adminUserMapper;

    public R<List<CourseType>> listCourseType(Integer status) {
        return R.ok(courseTypeMapper.selectList(
                new LambdaQueryWrapper<CourseType>()
                        .eq(status != null, CourseType::getStatus, status)
                        .orderByAsc(CourseType::getSort)));
    }

    public R<CourseType> upsertCourseType(CourseType t) {
        if (t.getId() == null) {
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            courseTypeMapper.insert(t);
        } else {
            t.setUpdatedAt(LocalDateTime.now());
            courseTypeMapper.updateById(t);
        }
        return R.ok(t);
    }

    public R<Void> deleteCourseType(Long id) {
        long used = scheduleMapper.selectCount(new LambdaQueryWrapper<CourseSchedule>()
                .eq(CourseSchedule::getCourseTypeId, id));
        if (used > 0) throw BizException.conflict("该课程类型已被排课引用, 不能删除");
        courseTypeMapper.deleteById(id);
        return R.ok();
    }

    public R<List<Room>> listRoom() {
        return R.ok(roomMapper.selectList(
                new LambdaQueryWrapper<Room>().orderByAsc(Room::getSort)));
    }

    public R<Room> upsertRoom(Room r) {
        if (r.getId() == null) {
            r.setCreatedAt(LocalDateTime.now());
            roomMapper.insert(r);
        } else {
            roomMapper.updateById(r);
        }
        return R.ok(r);
    }

    public R<Void> deleteRoom(Long id) {
        roomMapper.deleteById(id);
        return R.ok();
    }

    public R<List<AdminUser>> listCoaches() {
        return R.ok(adminUserMapper.selectList(
                new LambdaQueryWrapper<AdminUser>()
                        .eq(AdminUser::getRole, "COACH")
                        .eq(AdminUser::getStatus, 1)));
    }

    /**
     * 排课: 支持单次或周期
     */
    @Transactional
    public R<List<CourseSchedule>> schedule(CourseScheduleUpsertDTO dto) {
        if (dto.getId() != null) {
            // 编辑单条
            CourseSchedule exist = scheduleMapper.selectById(dto.getId());
            if (exist == null) throw BizException.notFound("排课不存在");
            CourseType ct = courseTypeMapper.selectById(dto.getCourseTypeId());
            if (ct == null) throw BizException.notFound("课程类型不存在");
            AdminUser coach = adminUserMapper.selectById(dto.getCoachId());
            if (coach == null) throw BizException.notFound("教练不存在");
            checkConflict(dto, exist.getId());
            fillAndSave(exist, dto, ct, coach);
            scheduleMapper.updateById(exist);
            return R.ok(List.of(exist));
        }
        // 新建
        CourseType ct = courseTypeMapper.selectById(dto.getCourseTypeId());
        if (ct == null) throw BizException.notFound("课程类型不存在");
        AdminUser coach = adminUserMapper.selectById(dto.getCoachId());
        if (coach == null) throw BizException.notFound("教练不存在");

        List<CourseSchedule> result = new ArrayList<>();
        List<LocalDateTime> startTimes = expandStartTimes(dto);
        for (LocalDateTime st : startTimes) {
            CourseScheduleUpsertDTO sub = clone(dto);
            sub.setStartTime(st);
            sub.setEndTime(st.plusMinutes(
                    ChronoUnit.MINUTES.between(dto.getStartTime(), dto.getEndTime())));
            checkConflict(sub, null);
            CourseSchedule s = new CourseSchedule();
            fillAndSave(s, sub, ct, coach);
            scheduleMapper.insert(s);
            result.add(s);
        }
        return R.ok(result);
    }

    private List<LocalDateTime> expandStartTimes(CourseScheduleUpsertDTO dto) {
        if (dto.getRepeatType() == null || "ONCE".equals(dto.getRepeatType()) || dto.getRepeatCount() == null) {
            return List.of(dto.getStartTime());
        }
        List<LocalDateTime> list = new ArrayList<>();
        LocalDateTime base = dto.getStartTime();
        long minutes = ChronoUnit.MINUTES.between(dto.getStartTime(), dto.getEndTime());
        if ("DAILY".equals(dto.getRepeatType())) {
            for (int i = 0; i < dto.getRepeatCount(); i++) {
                list.add(base.plusDays(i));
            }
        } else if ("WEEKLY".equals(dto.getRepeatType())) {
            // 以周为周期, weekDays 为空时按 base 的星期几
            int baseDow = base.getDayOfWeek().getValue();
            for (int i = 0; i < dto.getRepeatCount(); i++) {
                LocalDate weekStart = base.toLocalDate().plusWeeks(i);
                if (dto.getWeekDays() == null || dto.getWeekDays().isEmpty()) {
                    list.add(weekStart.atTime(base.toLocalTime()));
                } else {
                    for (int dow : dto.getWeekDays()) {
                        if (dow == baseDow) {
                            list.add(weekStart.atTime(base.toLocalTime()));
                        }
                    }
                }
            }
        }
        return list;
    }

    private void checkConflict(CourseScheduleUpsertDTO dto, Long excludeId) {
        // 同一教练同时间段不能排课
        Long coachConflict = scheduleMapper.selectCount(new LambdaQueryWrapper<CourseSchedule>()
                .eq(CourseSchedule::getCoachId, dto.getCoachId())
                .lt(CourseSchedule::getStartTime, dto.getEndTime())
                .gt(CourseSchedule::getEndTime, dto.getStartTime())
                .ne(excludeId != null, CourseSchedule::getId, excludeId));
        if (coachConflict > 0) throw BizException.conflict("该教练时间段已有排课");

        // 同一教室同时间段不能排课
        if (dto.getRoomId() != null) {
            Long roomConflict = scheduleMapper.selectCount(new LambdaQueryWrapper<CourseSchedule>()
                    .eq(CourseSchedule::getRoomId, dto.getRoomId())
                    .lt(CourseSchedule::getStartTime, dto.getEndTime())
                    .gt(CourseSchedule::getEndTime, dto.getStartTime())
                    .ne(excludeId != null, CourseSchedule::getId, excludeId));
            if (roomConflict > 0) throw BizException.conflict("该教室时间段已有排课");
        }
    }

    private void fillAndSave(CourseSchedule s, CourseScheduleUpsertDTO dto,
                             CourseType ct, AdminUser coach) {
        BeanUtils.copyProperties(dto, s, "id", "repeatType", "repeatCount", "weekDays");
        s.setCourseTypeName(ct.getName());
        s.setCoachName(coach.getRealName() != null ? coach.getRealName() : coach.getUsername());
        if (dto.getRoomId() != null) {
            Room r = roomMapper.selectById(dto.getRoomId());
            if (r != null) s.setRoomName(r.getName());
        }
        s.setBookedCount(0);
        s.setStatus("SCHEDULED");
        s.setVersion(0);
        s.setCancelDeadline(dto.getStartTime().minusHours(2));
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
    }

    private CourseScheduleUpsertDTO clone(CourseScheduleUpsertDTO src) {
        CourseScheduleUpsertDTO c = new CourseScheduleUpsertDTO();
        BeanUtils.copyProperties(src, c);
        return c;
    }

    /**
     * 排课列表(按日期范围)
     */
    public R<Map<String, Object>> schedulePage(LocalDate startDate, LocalDate endDate,
                                               Long courseTypeId, Long coachId,
                                               Long pageNum, Long pageSize) {
        Page<CourseSchedule> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CourseSchedule> w = new LambdaQueryWrapper<CourseSchedule>()
                .orderByAsc(CourseSchedule::getStartTime);
        if (startDate != null) w.ge(CourseSchedule::getStartTime, startDate.atStartOfDay());
        if (endDate != null) w.lt(CourseSchedule::getStartTime, endDate.plusDays(1).atStartOfDay());
        if (courseTypeId != null) w.eq(CourseSchedule::getCourseTypeId, courseTypeId);
        if (coachId != null) w.eq(CourseSchedule::getCoachId, coachId);
        var result = scheduleMapper.selectPage(page, w);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return R.ok(data);
    }

    public R<Void> cancelSchedule(Long id) {
        CourseSchedule s = scheduleMapper.selectById(id);
        if (s == null) throw BizException.notFound("排课不存在");
        if (!"SCHEDULED".equals(s.getStatus())) throw BizException.conflict("该排课不可取消");
        s.setStatus("CANCELLED");
        s.setUpdatedAt(LocalDateTime.now());
        scheduleMapper.updateById(s);
        return R.ok();
    }

    public R<Void> deleteSchedule(Long id) {
        CourseSchedule s = scheduleMapper.selectById(id);
        if (s == null) throw BizException.notFound("排课不存在");
        if (s.getBookedCount() > 0) throw BizException.conflict("已有会员预约, 不能删除");
        scheduleMapper.deleteById(id);
        return R.ok();
    }

    /**
     * H5 排课查询
     */
    public R<List<CourseSchedule>> h5List(LocalDate date, Long courseTypeId) {
        LambdaQueryWrapper<CourseSchedule> w = new LambdaQueryWrapper<CourseSchedule>()
                .eq(CourseSchedule::getStatus, "SCHEDULED")
                .ge(CourseSchedule::getStartTime, date.atStartOfDay())
                .lt(CourseSchedule::getStartTime, date.plusDays(1).atStartOfDay())
                .orderByAsc(CourseSchedule::getStartTime);
        if (courseTypeId != null) w.eq(CourseSchedule::getCourseTypeId, courseTypeId);
        return R.ok(scheduleMapper.selectList(w));
    }
}
