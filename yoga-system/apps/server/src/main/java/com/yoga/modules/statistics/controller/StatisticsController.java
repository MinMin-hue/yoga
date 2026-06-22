package com.yoga.modules.statistics.controller;

import com.yoga.common.R;
import com.yoga.modules.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService service;

    @GetMapping("/revenue")
    public R<Map<String, Object>> revenue(@RequestParam(defaultValue = "month") String range) {
        return service.revenue(range);
    }

    @GetMapping("/course")
    public R<List<Map<String, Object>>> course(@RequestParam(defaultValue = "month") String range) {
        return service.course(range);
    }

    @GetMapping("/member")
    public R<Map<String, Object>> member(@RequestParam(defaultValue = "month") String range) {
        return service.member(range);
    }

    @GetMapping("/coach")
    public R<List<Map<String, Object>>> coach(@RequestParam(defaultValue = "month") String range) {
        return service.coach(range);
    }
}
