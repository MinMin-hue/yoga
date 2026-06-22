package com.yoga.modules.system.controller;

import com.yoga.common.R;
import com.yoga.modules.system.entity.SystemConfigDB;
import com.yoga.modules.system.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService service;

    @GetMapping("/all")
    public R<Map<String, String>> all() {
        return service.all();
    }

    @PostMapping("/update")
    public R<Void> update(@RequestBody Map<String, String> params) {
        return service.batchUpdate(params);
    }

    @GetMapping("/detail")
    public R<List<SystemConfigDB>> detail() {
        return service.allDetailed();
    }
}
