package com.yoga.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yoga.common.R;
import com.yoga.config.SystemConfig;
import com.yoga.modules.system.entity.SystemConfigDB;
import com.yoga.modules.system.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigMapper mapper;
    private final SystemConfig cache;

    public R<Map<String, String>> all() {
        return R.ok(cache.all());
    }

    public R<Void> batchUpdate(Map<String, String> params) {
        if (params == null || params.isEmpty()) return R.ok();
        for (Map.Entry<String, String> e : params.entrySet()) {
            SystemConfigDB exist = mapper.selectOne(new LambdaQueryWrapper<SystemConfigDB>()
                    .eq(SystemConfigDB::getConfigKey, e.getKey()));
            if (exist == null) {
                SystemConfigDB c = new SystemConfigDB();
                c.setConfigKey(e.getKey());
                c.setConfigValue(e.getValue());
                mapper.insert(c);
            } else {
                exist.setConfigValue(e.getValue());
                mapper.updateById(exist);
            }
        }
        cache.reload();
        return R.ok();
    }

    public R<List<SystemConfigDB>> allDetailed() {
        return R.ok(mapper.selectList(
                new LambdaQueryWrapper<SystemConfigDB>().orderByAsc(SystemConfigDB::getId)));
    }
}
