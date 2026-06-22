package com.yoga.modules.system.job;

import com.yoga.modules.booking.service.BookingService;
import com.yoga.modules.card.service.MemberCardService;
import com.yoga.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 系统定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemJobs {

    private final MemberCardService memberCardService;
    private final BookingService bookingService;
    private final OrderService orderService;

    /** 每 5 分钟扫描一次过期卡/用尽次数卡 */
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void scanCards() {
        int n = memberCardService.scanExpiredCards();
        if (n > 0) log.info("扫描过期/用尽卡: {} 张", n);
    }

    /** 每 5 分钟扫描一次超时未签到的预约 -> 标记爽约 */
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void scanNoShow() {
        int n = bookingService.markNoShow();
        if (n > 0) log.info("标记爽约: {} 条", n);
    }

    /** 每 1 分钟扫描一次超时未支付订单 -> 自动取消 */
    @Scheduled(fixedDelay = 60 * 1000)
    public void scanOrders() {
        int n = orderService.scanExpiredOrders();
        if (n > 0) log.info("取消超时订单: {} 条", n);
    }
}
