package com.yoga.modules.card.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yoga.common.BizException;
import com.yoga.common.R;
import com.yoga.modules.card.dto.CardActivateDTO;
import com.yoga.modules.card.dto.CardRefundDTO;
import com.yoga.modules.card.entity.CardType;
import com.yoga.modules.card.entity.MemberCard;
import com.yoga.modules.card.enums.CardStatus;
import com.yoga.modules.card.mapper.CardTypeMapper;
import com.yoga.modules.card.mapper.MemberCardMapper;
import com.yoga.modules.consumption.entity.ConsumptionRecord;
import com.yoga.modules.consumption.mapper.ConsumptionRecordMapper;
import com.yoga.utils.BizNoGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberCardService {

    private final MemberCardMapper memberCardMapper;
    private final CardTypeMapper cardTypeMapper;
    private final ConsumptionRecordMapper consumptionRecordMapper;

    /**
     * 创建会员卡(由订单支付成功回调触发, 或后台手动添加)
     */
    @Transactional
    public MemberCard createCard(Long memberId, Long cardTypeId, Long orderId) {
        CardType type = cardTypeMapper.selectById(cardTypeId);
        if (type == null) throw BizException.notFound("卡类型不存在");
        MemberCard card = new MemberCard();
        card.setCardNo(BizNoGenerator.cardNo());
        card.setMemberId(memberId);
        card.setCardTypeId(cardTypeId);
        card.setCardTypeName(type.getName());
        card.setPrice(type.getPrice());
        card.setCardKind(type.getCardKind());
        card.setValidDays(type.getValidDays());
        card.setTotalTimes(type.getTotalTimes());
        card.setRemainTimes(type.getTotalTimes());
        card.setStatus(CardStatus.PENDING.name());
        card.setOrderId(orderId);
        memberCardMapper.insert(card);
        return card;
    }

    /**
     * 激活会员卡(待激活 -> 正常)
     */
    @Transactional
    public R<MemberCard> activate(CardActivateDTO dto) {
        MemberCard card = memberCardMapper.selectById(dto.getCardId());
        if (card == null) throw BizException.notFound("会员卡不存在");
        CardStatus status = CardStatus.valueOf(card.getStatus());
        status.checkTransit(CardStatus.ACTIVE);

        LocalDateTime now = LocalDateTime.now();
        card.setStatus(CardStatus.ACTIVE.name());
        card.setActivatedAt(now);
        if (card.getValidDays() != null && card.getValidDays() > 0) {
            card.setValidFrom(now);
            card.setValidTo(now.plusDays(card.getValidDays()));
        }
        memberCardMapper.updateById(card);
        return R.ok(card);
    }

    /**
     * 退款处理
     */
    @Transactional
    public R<MemberCard> refund(CardRefundDTO dto) {
        MemberCard card = memberCardMapper.selectById(dto.getCardId());
        if (card == null) throw BizException.notFound("会员卡不存在");
        CardStatus current = CardStatus.valueOf(card.getStatus());
        if (current == CardStatus.REFUNDED) throw BizException.conflict("该卡已退款");
        if (current == CardStatus.EXPIRED) throw BizException.conflict("已过期卡不可退款");

        current.checkTransit(CardStatus.REFUNDED);
        card.setStatus(CardStatus.REFUNDED.name());
        card.setRefundedAt(LocalDateTime.now());
        memberCardMapper.updateById(card);

        // 记录流水
        ConsumptionRecord r = new ConsumptionRecord();
        r.setMemberId(card.getMemberId());
        r.setType("REFUND");
        r.setAmount(card.getPrice());
        r.setTimesDelta(0);
        r.setCardId(card.getId());
        r.setCardNo(card.getCardNo());
        r.setOrderId(card.getOrderId());
        r.setRemark(dto.getReason() != null ? dto.getReason() : "会员卡退款");
        consumptionRecordMapper.insert(r);
        return R.ok(card);
    }

    /**
     * 扫描过期/次数用尽的卡(定时任务调用)
     */
    @Transactional
    public int scanExpiredCards() {
        LocalDateTime now = LocalDateTime.now();
        // 过期
        List<MemberCard> expiredList = memberCardMapper.selectList(new LambdaQueryWrapper<MemberCard>()
                .eq(MemberCard::getStatus, CardStatus.ACTIVE.name())
                .isNotNull(MemberCard::getValidTo)
                .lt(MemberCard::getValidTo, now));
        for (MemberCard c : expiredList) {
            CardStatus.ACTIVE.checkTransit(CardStatus.EXPIRED);
            c.setStatus(CardStatus.EXPIRED.name());
            memberCardMapper.updateById(c);
        }
        // 次数用尽
        List<MemberCard> noRemainList = memberCardMapper.selectList(new LambdaQueryWrapper<MemberCard>()
                .eq(MemberCard::getStatus, CardStatus.ACTIVE.name())
                .le(MemberCard::getRemainTimes, 0));
        for (MemberCard c : noRemainList) {
            CardStatus.ACTIVE.checkTransit(CardStatus.NO_REMAIN);
            c.setStatus(CardStatus.NO_REMAIN.name());
            memberCardMapper.updateById(c);
        }
        return expiredList.size() + noRemainList.size();
    }

    /**
     * 校验并扣减次数(预约时调用)
     */
    @Transactional
    public MemberCard deductTimes(Long cardId, int cost) {
        MemberCard card = memberCardMapper.selectById(cardId);
        if (card == null) throw BizException.notFound("会员卡不存在");
        if (!CardStatus.ACTIVE.name().equals(card.getStatus())) {
            throw BizException.conflict("会员卡不可用, 当前状态: " + card.getStatus());
        }
        if (card.getValidTo() != null && card.getValidTo().isBefore(LocalDateTime.now())) {
            throw BizException.conflict("会员卡已过期");
        }
        if (card.getRemainTimes() != null) {
            if (card.getRemainTimes() < cost) {
                throw BizException.conflict("会员卡剩余次数不足");
            }
            card.setRemainTimes(card.getRemainTimes() - cost);
            if (card.getRemainTimes() == 0) {
                card.setStatus(CardStatus.NO_REMAIN.name());
            }
        }
        memberCardMapper.updateById(card);
        return card;
    }

    /**
     * 释放次数(取消预约时回滚, 仅在扣减后)
     */
    @Transactional
    public void releaseTimes(Long cardId, int cost) {
        MemberCard card = memberCardMapper.selectById(cardId);
        if (card == null || card.getRemainTimes() == null) return;
        int newRemain = card.getRemainTimes() + cost;
        if (card.getTotalTimes() != null) {
            newRemain = Math.min(newRemain, card.getTotalTimes());
        }
        card.setRemainTimes(newRemain);
        // 释放后如果是从 NO_REMAIN 状态, 重新激活
        if (CardStatus.NO_REMAIN.name().equals(card.getStatus()) && newRemain > 0
                && (card.getValidTo() == null || card.getValidTo().isAfter(LocalDateTime.now()))) {
            card.setStatus(CardStatus.ACTIVE.name());
        }
        memberCardMapper.updateById(card);
    }

    public R<MemberCard> detail(Long id) {
        MemberCard c = memberCardMapper.selectById(id);
        if (c == null) throw BizException.notFound("会员卡不存在");
        return R.ok(c);
    }

    public R<List<MemberCard>> listByMember(Long memberId) {
        return R.ok(memberCardMapper.selectList(new LambdaQueryWrapper<MemberCard>()
                .eq(MemberCard::getMemberId, memberId)
                .orderByDesc(MemberCard::getId)));
    }
}
