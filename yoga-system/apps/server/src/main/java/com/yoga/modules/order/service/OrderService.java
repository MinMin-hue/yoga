package com.yoga.modules.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yoga.common.BizException;
import com.yoga.common.R;
import com.yoga.common.SecurityContext;
import com.yoga.config.SystemConfig;
import com.yoga.modules.card.entity.CardType;
import com.yoga.modules.card.entity.MemberCard;
import com.yoga.modules.card.mapper.CardTypeMapper;
import com.yoga.modules.card.service.MemberCardService;
import com.yoga.modules.consumption.entity.ConsumptionRecord;
import com.yoga.modules.consumption.mapper.ConsumptionRecordMapper;
import com.yoga.modules.course.entity.CourseType;
import com.yoga.modules.course.mapper.CourseTypeMapper;
import com.yoga.modules.member.entity.Member;
import com.yoga.modules.member.mapper.MemberMapper;
import com.yoga.modules.order.dto.OrderCreateDTO;
import com.yoga.modules.order.dto.OrderPayDTO;
import com.yoga.modules.order.dto.OrderQuery;
import com.yoga.modules.order.entity.OrderInfo;
import com.yoga.modules.order.enums.OrderStatus;
import com.yoga.modules.order.mapper.OrderInfoMapper;
import com.yoga.utils.BizNoGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderInfoMapper orderInfoMapper;
    private final CardTypeMapper cardTypeMapper;
    private final CourseTypeMapper courseTypeMapper;
    private final MemberCardService memberCardService;
    private final MemberMapper memberMapper;
    private final ConsumptionRecordMapper consumptionRecordMapper;
    private final SystemConfig systemConfig;

    /**
     * 创建订单(H5 会员下单)
     */
    @Transactional
    public R<OrderInfo> create(OrderCreateDTO dto) {
        Long memberId = SecurityContext.getUserId();
        if (memberId == null) throw BizException.unauthorized("请先登录");

        OrderInfo o = new OrderInfo();
        o.setOrderNo(BizNoGenerator.orderNo());
        o.setMemberId(memberId);
        Member m = memberMapper.selectById(memberId);
        o.setMemberName(m != null ? m.getNickname() : null);
        o.setOrderType(dto.getOrderType());
        o.setStatus(OrderStatus.PENDING.name());
        o.setRemark(dto.getRemark());

        switch (dto.getOrderType()) {
            case "PURCHASE_CARD" -> {
                if (dto.getCardTypeId() == null) throw BizException.badRequest("请选择卡类型");
                CardType t = cardTypeMapper.selectById(dto.getCardTypeId());
                if (t == null) throw BizException.notFound("卡类型不存在");
                if (t.getStatus() != 1) throw BizException.conflict("该卡已停售");
                o.setCardTypeId(t.getId());
                o.setCardTypeName(t.getName());
                o.setAmount(dto.getAmount() != null ? dto.getAmount() : t.getPrice());
            }
            case "SINGLE_COURSE" -> {
                if (dto.getCourseTypeId() == null) throw BizException.badRequest("请选择课程");
                CourseType t = courseTypeMapper.selectById(dto.getCourseTypeId());
                if (t == null) throw BizException.notFound("课程不存在");
                o.setCourseTypeId(t.getId());
                o.setCourseTypeName(t.getName());
                o.setAmount(dto.getAmount() != null ? dto.getAmount() : BigDecimal.ZERO);
            }
            case "RECHARGE" -> {
                if (dto.getAmount() == null || dto.getAmount().signum() <= 0) {
                    throw BizException.badRequest("请填写充值金额");
                }
                o.setAmount(dto.getAmount());
            }
            default -> throw BizException.badRequest("未知订单类型");
        }

        int expireMinutes = Integer.parseInt(systemConfig.getOrDefault("order.expire_minutes", "30"));
        o.setExpireAt(LocalDateTime.now().plusMinutes(expireMinutes));
        orderInfoMapper.insert(o);
        return R.ok(o);
    }

    /**
     * 线下支付确认(管理员操作)
     */
    @Transactional
    public R<OrderInfo> confirmOfflinePay(OrderPayDTO dto) {
        OrderInfo o = orderInfoMapper.selectById(dto.getOrderId());
        if (o == null) throw BizException.notFound("订单不存在");
        OrderStatus.PENDING.checkTransit(OrderStatus.PAID);
        o.setStatus(OrderStatus.PAID.name());
        o.setPayMethod("OFFLINE");
        o.setPayTime(LocalDateTime.now());
        o.setPaidBy(SecurityContext.getUserId());
        if (dto.getRemark() != null) o.setRemark(dto.getRemark());
        orderInfoMapper.updateById(o);

        // 业务联动
        switch (o.getOrderType()) {
            case "PURCHASE_CARD" -> {
                MemberCard card = memberCardService.createCard(o.getMemberId(), o.getCardTypeId(), o.getId());
                // 记录流水
                ConsumptionRecord r = new ConsumptionRecord();
                r.setMemberId(o.getMemberId());
                r.setType("PURCHASE");
                r.setAmount(o.getAmount());
                r.setTimesDelta(0);
                r.setCardId(card.getId());
                r.setCardNo(card.getCardNo());
                r.setOrderId(o.getId());
                r.setRemark("购买会员卡: " + o.getCardTypeName());
                consumptionRecordMapper.insert(r);
            }
            case "RECHARGE" -> {
                ConsumptionRecord r = new ConsumptionRecord();
                r.setMemberId(o.getMemberId());
                r.setType("RECHARGE");
                r.setAmount(o.getAmount());
                r.setTimesDelta(0);
                r.setOrderId(o.getId());
                r.setRemark("会员充值");
                consumptionRecordMapper.insert(r);
            }
            case "SINGLE_COURSE" -> {
                ConsumptionRecord r = new ConsumptionRecord();
                r.setMemberId(o.getMemberId());
                r.setType("PURCHASE");
                r.setAmount(o.getAmount());
                r.setTimesDelta(0);
                r.setOrderId(o.getId());
                r.setRemark("单课购买: " + o.getCourseTypeName());
                consumptionRecordMapper.insert(r);
            }
        }
        return R.ok(o);
    }

    /**
     * 取消订单(超时或用户手动)
     */
    @Transactional
    public R<OrderInfo> cancel(Long orderId) {
        OrderInfo o = orderInfoMapper.selectById(orderId);
        if (o == null) throw BizException.notFound("订单不存在");
        OrderStatus.PENDING.checkTransit(OrderStatus.CANCELLED);
        o.setStatus(OrderStatus.CANCELLED.name());
        orderInfoMapper.updateById(o);
        return R.ok(o);
    }

    /**
     * 退款
     */
    @Transactional
    public R<OrderInfo> refund(Long orderId, String reason) {
        OrderInfo o = orderInfoMapper.selectById(orderId);
        if (o == null) throw BizException.notFound("订单不存在");
        OrderStatus.PAID.checkTransit(OrderStatus.REFUNDED);
        o.setStatus(OrderStatus.REFUNDED.name());
        o.setRefundTime(LocalDateTime.now());
        o.setRefundReason(reason);
        orderInfoMapper.updateById(o);
        return R.ok(o);
    }

    /**
     * 定时任务: 扫描超时订单
     */
    @Transactional
    public int scanExpiredOrders() {
        List<OrderInfo> list = orderInfoMapper.selectList(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getStatus, OrderStatus.PENDING.name())
                .lt(OrderInfo::getExpireAt, LocalDateTime.now()));
        for (OrderInfo o : list) {
            o.setStatus(OrderStatus.CANCELLED.name());
            orderInfoMapper.updateById(o);
        }
        return list.size();
    }

    public R<Map<String, Object>> page(OrderQuery q) {
        Page<OrderInfo> page = new Page<>(q.getPageNum(), q.getPageSize());
        LambdaQueryWrapper<OrderInfo> w = new LambdaQueryWrapper<OrderInfo>()
                .orderByDesc(OrderInfo::getId);
        if (q.getOrderNo() != null && !q.getOrderNo().isBlank()) w.like(OrderInfo::getOrderNo, q.getOrderNo());
        if (q.getMemberId() != null) w.eq(OrderInfo::getMemberId, q.getMemberId());
        if (q.getStatus() != null && !q.getStatus().isBlank()) w.eq(OrderInfo::getStatus, q.getStatus());
        if (q.getOrderType() != null && !q.getOrderType().isBlank()) w.eq(OrderInfo::getOrderType, q.getOrderType());
        if (q.getStartDate() != null) w.ge(OrderInfo::getCreatedAt, q.getStartDate().atStartOfDay());
        if (q.getEndDate() != null) w.lt(OrderInfo::getCreatedAt, q.getEndDate().plusDays(1).atStartOfDay());
        var result = orderInfoMapper.selectPage(page, w);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return R.ok(data);
    }

    public R<OrderInfo> detail(Long id) {
        OrderInfo o = orderInfoMapper.selectById(id);
        if (o == null) throw BizException.notFound("订单不存在");
        return R.ok(o);
    }
}
