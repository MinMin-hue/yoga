package com.yoga.modules.card.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yoga.common.BizException;
import com.yoga.common.R;
import com.yoga.modules.card.dto.CardTypeUpsertDTO;
import com.yoga.modules.card.entity.CardType;
import com.yoga.modules.card.enums.CardStatus;
import com.yoga.modules.card.mapper.CardTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardTypeService {

    private final CardTypeMapper cardTypeMapper;

    public R<List<CardType>> listAll(Integer status) {
        LambdaQueryWrapper<CardType> wrapper = new LambdaQueryWrapper<CardType>()
                .orderByAsc(CardType::getSort).orderByDesc(CardType::getId);
        if (status != null) wrapper.eq(CardType::getStatus, status);
        return R.ok(cardTypeMapper.selectList(wrapper));
    }

    public R<Map<String, Object>> page(Long pageNum, Long pageSize) {
        Page<CardType> page = new Page<>(pageNum, pageSize);
        Page<CardType> result = cardTypeMapper.selectPage(page,
                new LambdaQueryWrapper<CardType>().orderByAsc(CardType::getSort));
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return R.ok(data);
    }

    public R<CardType> create(CardTypeUpsertDTO dto) {
        validate(dto);
        CardType t = new CardType();
        BeanUtils.copyProperties(dto, t);
        t.setId(null);
        t.setApplicableTypes(joinIds(dto.getApplicableTypes()));
        cardTypeMapper.insert(t);
        return R.ok(t);
    }

    public R<CardType> update(CardTypeUpsertDTO dto) {
        if (dto.getId() == null) throw BizException.badRequest("id 必填");
        CardType t = cardTypeMapper.selectById(dto.getId());
        if (t == null) throw BizException.notFound("卡类型不存在");
        validate(dto);
        BeanUtils.copyProperties(dto, t);
        t.setApplicableTypes(joinIds(dto.getApplicableTypes()));
        cardTypeMapper.updateById(t);
        return R.ok(t);
    }

    public R<Void> delete(Long id) {
        cardTypeMapper.deleteById(id);
        return R.ok();
    }

    private void validate(CardTypeUpsertDTO dto) {
        if (!List.of("TIME", "TIMES", "MIXED").contains(dto.getCardKind())) {
            throw BizException.badRequest("卡类型必须为 TIME/TIMES/MIXED");
        }
        if ("TIME".equals(dto.getCardKind()) && (dto.getValidDays() == null || dto.getValidDays() <= 0)) {
            throw BizException.badRequest("时间卡必须设置有效天数");
        }
        if ("TIMES".equals(dto.getCardKind()) && (dto.getTotalTimes() == null || dto.getTotalTimes() <= 0)) {
            throw BizException.badRequest("次卡必须设置总次数");
        }
        if ("MIXED".equals(dto.getCardKind())) {
            if (dto.getValidDays() == null || dto.getValidDays() <= 0
                    || dto.getTotalTimes() == null || dto.getTotalTimes() <= 0) {
                throw BizException.badRequest("混合卡必须同时设置有效天数和总次数");
            }
        }
    }

    private String joinIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return null;
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    public static List<Long> splitIds(String s) {
        if (s == null || s.isBlank()) return List.of();
        return java.util.Arrays.stream(s.split(",")).map(Long::valueOf).toList();
    }
}
