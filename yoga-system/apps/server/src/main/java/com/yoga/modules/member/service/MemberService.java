package com.yoga.modules.member.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yoga.common.BizException;
import com.yoga.common.R;
import com.yoga.common.SecurityContext;
import com.yoga.modules.card.entity.MemberCard;
import com.yoga.modules.card.mapper.MemberCardMapper;
import com.yoga.modules.consumption.entity.ConsumptionRecord;
import com.yoga.modules.consumption.mapper.ConsumptionRecordMapper;
import com.yoga.modules.member.dto.MemberQuery;
import com.yoga.modules.member.dto.MemberUpsertDTO;
import com.yoga.modules.member.entity.Member;
import com.yoga.modules.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final MemberCardMapper memberCardMapper;
    private final ConsumptionRecordMapper consumptionRecordMapper;

    public R<Map<String, Object>> page(MemberQuery query) {
        Page<Member> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<Member>()
                .orderByDesc(Member::getId);
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            wrapper.and(w -> w.like(Member::getPhone, query.getKeyword())
                    .or().like(Member::getNickname, query.getKeyword()));
        }
        if (query.getStatus() != null) {
            wrapper.eq(Member::getStatus, query.getStatus());
        }
        Page<Member> result = memberMapper.selectPage(page, wrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("pageNum", result.getCurrent());
        data.put("pageSize", result.getSize());
        return R.ok(data);
    }

    public R<Member> detail(Long id) {
        Member m = memberMapper.selectById(id);
        if (m == null) throw BizException.notFound("会员不存在");
        return R.ok(m);
    }

    public R<Member> create(MemberUpsertDTO dto) {
        Long exist = memberMapper.selectCount(new LambdaQueryWrapper<Member>()
                .eq(Member::getPhone, dto.getPhone()));
        if (exist > 0) throw BizException.conflict("手机号已存在");
        Member m = new Member();
        BeanUtils.copyProperties(dto, m);
        m.setId(null);
        memberMapper.insert(m);
        return R.ok(m);
    }

    public R<Member> update(MemberUpsertDTO dto) {
        if (dto.getId() == null) throw BizException.badRequest("id 必填");
        Member m = memberMapper.selectById(dto.getId());
        if (m == null) throw BizException.notFound("会员不存在");
        BeanUtils.copyProperties(dto, m);
        memberMapper.updateById(m);
        return R.ok(m);
    }

    public R<Void> delete(Long id) {
        long cardCount = memberCardMapper.selectCount(new LambdaQueryWrapper<MemberCard>()
                .eq(MemberCard::getMemberId, id)
                .in(MemberCard::getStatus, List.of("PENDING", "ACTIVE")));
        if (cardCount > 0) throw BizException.conflict("该会员存在未结算的会员卡, 不能删除");
        memberMapper.deleteById(id);
        return R.ok();
    }

    public R<Map<String, Object>> myCards() {
        Long memberId = SecurityContext.getUserId();
        List<MemberCard> cards = memberCardMapper.selectList(new LambdaQueryWrapper<MemberCard>()
                .eq(MemberCard::getMemberId, memberId)
                .orderByDesc(MemberCard::getId));
        Map<String, Object> data = new HashMap<>();
        data.put("cards", cards);
        return R.ok(data);
    }

    public R<Map<String, Object>> myRecords() {
        Long memberId = SecurityContext.getUserId();
        List<ConsumptionRecord> records = consumptionRecordMapper.selectList(
                new LambdaQueryWrapper<ConsumptionRecord>()
                        .eq(ConsumptionRecord::getMemberId, memberId)
                        .orderByDesc(ConsumptionRecord::getId));
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        return R.ok(data);
    }

    public R<Member> myProfile() {
        Long memberId = SecurityContext.getUserId();
        Member m = memberMapper.selectById(memberId);
        if (m == null) throw BizException.unauthorized("请重新登录");
        return R.ok(m);
    }
}
