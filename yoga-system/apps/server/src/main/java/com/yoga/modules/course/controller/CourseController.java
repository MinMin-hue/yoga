package com.yoga.modules.course.controller;

import com.yoga.common.R;
import com.yoga.modules.auth.entity.AdminUser;
import com.yoga.modules.course.dto.CourseScheduleUpsertDTO;
import com.yoga.modules.course.entity.CourseSchedule;
import com.yoga.modules.course.entity.CourseType;
import com.yoga.modules.course.entity.Room;
import com.yoga.modules.course.service.CourseScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CourseController {

    private final CourseScheduleService courseScheduleService;

    // ==================== 课程类型(后台) ====================
    @GetMapping("/admin/course-type/list")
    public R<List<CourseType>> listType(@RequestParam(required = false) Integer status) {
        return courseScheduleService.listCourseType(status);
    }

    @PostMapping("/admin/course-type/upsert")
    public R<CourseType> upsertType(@RequestBody CourseType t) {
        return courseScheduleService.upsertCourseType(t);
    }

    @PostMapping("/admin/course-type/delete/{id}")
    public R<?> deleteType(@PathVariable Long id) {
        return courseScheduleService.deleteCourseType(id);
    }

    // ==================== 教室(后台) ====================
    @GetMapping("/admin/room/list")
    public R<List<Room>> listRoom() {
        return courseScheduleService.listRoom();
    }

    @PostMapping("/admin/room/upsert")
    public R<Room> upsertRoom(@RequestBody Room r) {
        return courseScheduleService.upsertRoom(r);
    }

    @PostMapping("/admin/room/delete/{id}")
    public R<?> deleteRoom(@PathVariable Long id) {
        return courseScheduleService.deleteRoom(id);
    }

    // ==================== 教练(后台) ====================
    @GetMapping("/admin/coach/list")
    public R<List<AdminUser>> listCoaches() {
        return courseScheduleService.listCoaches();
    }

    // ==================== 排课(后台) ====================
    @PostMapping("/admin/schedule/page")
    public R<Map<String, Object>> schedulePage(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long courseTypeId,
            @RequestParam(required = false) Long coachId,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "50") Long pageSize) {
        return courseScheduleService.schedulePage(startDate, endDate, courseTypeId, coachId, pageNum, pageSize);
    }

    @PostMapping("/admin/schedule/upsert")
    public R<List<CourseSchedule>> upsertSchedule(@RequestBody @Valid CourseScheduleUpsertDTO dto) {
        return courseScheduleService.schedule(dto);
    }

    @PostMapping("/admin/schedule/cancel/{id}")
    public R<?> cancelSchedule(@PathVariable Long id) {
        return courseScheduleService.cancelSchedule(id);
    }

    @PostMapping("/admin/schedule/delete/{id}")
    public R<?> deleteSchedule(@PathVariable Long id) {
        return courseScheduleService.deleteSchedule(id);
    }

    // ==================== H5 用户端 ====================
    @GetMapping("/h5/course-type/list")
    public R<List<CourseType>> h5CourseType() {
        return courseScheduleService.listCourseType(1);
    }

    @GetMapping("/h5/schedule/list")
    public R<List<CourseSchedule>> h5Schedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long courseTypeId) {
        return courseScheduleService.h5List(date, courseTypeId);
    }
}
