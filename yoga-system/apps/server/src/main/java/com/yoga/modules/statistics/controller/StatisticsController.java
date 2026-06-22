package com.yoga.modules.statistics.controller;

import com.yoga.common.R;
import com.yoga.modules.statistics.dto.StatisticsDTO;
import com.yoga.modules.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService service;

    /**
     * 首页看板概览
     */
    @GetMapping("/overview")
    public R<StatisticsDTO> overview() {
        return service.overview();
    }

    /**
     * 多维度区间统计
     * @param days 近 N 天 (默认 7)
     */
    @GetMapping("/range")
    public R<StatisticsDTO> range(@RequestParam(defaultValue = "7") int days) {
        return service.range(days);
    }
}
