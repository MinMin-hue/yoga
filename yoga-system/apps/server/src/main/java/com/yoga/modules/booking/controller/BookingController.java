package com.yoga.modules.booking.controller;

import com.yoga.common.R;
import com.yoga.modules.booking.dto.BookingCreateDTO;
import com.yoga.modules.booking.dto.BookingQuery;
import com.yoga.modules.booking.entity.Booking;
import com.yoga.modules.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // ==================== H5 会员端 ====================
    @PostMapping("/h5/booking/create")
    public R<Booking> create(@RequestBody @Valid BookingCreateDTO dto) {
        return bookingService.create(dto);
    }

    @PostMapping("/h5/booking/cancel/{id}")
    public R<Booking> cancel(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return bookingService.cancel(id, reason);
    }

    @GetMapping("/h5/booking/my")
    public R<Map<String, Object>> myBookings(@RequestParam(required = false) String status) {
        return bookingService.myBookings(status);
    }

    // ==================== 管理后台 ====================
    @PostMapping("/admin/booking/page")
    public R<Map<String, Object>> page(@RequestBody BookingQuery q) {
        return bookingService.page(q);
    }

    @GetMapping("/admin/booking/by-schedule/{scheduleId}")
    public R<Map<String, Object>> bySchedule(@PathVariable Long scheduleId) {
        return bookingService.bySchedule(scheduleId);
    }

    @PostMapping("/admin/booking/check-in/{id}")
    public R<Booking> checkIn(@PathVariable Long id) {
        return bookingService.checkIn(id);
    }

    @PostMapping("/admin/booking/complete/{id}")
    public R<Booking> complete(@PathVariable Long id) {
        return bookingService.complete(id);
    }

    @PostMapping("/admin/booking/cancel/{id}")
    public R<Booking> adminCancel(@PathVariable Long id, @RequestParam(required = false) String reason) {
        // 管理员取消 - 不走违约
        return bookingService.cancel(id, reason);
    }
}
