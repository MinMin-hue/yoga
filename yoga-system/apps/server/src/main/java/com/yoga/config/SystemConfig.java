package com.yoga.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yoga.modules.system.entity.SystemConfigDB;
import com.yoga.modules.system.mapper.SystemConfigMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置缓存(简单实现, 启动时加载到内存)
 */
@Component
@RequiredArgsConstructor
public class SystemConfig {

    private final SystemConfigMapper systemConfigMapper;
    private final Map<String, String> cache = new HashMap<>();

    @PostConstruct
    public void init() {
        reload();
    }

    public void reload() {
        cache.clear();
        List<SystemConfigDB> all = systemConfigMapper.selectList(
                new LambdaQueryWrapper<SystemConfigDB>());
        for (SystemConfigDB c : all) {
            cache.put(c.getConfigKey(), c.getConfigValue());
        }
    }

    public String getOrDefault(String key, String def) {
        return cache.getOrDefault(key, def);
    }

    public String get(String key) {
        return cache.get(key);
    }

    public Map<String, String> all() {
        return new HashMap<>(cache);
    }
}
