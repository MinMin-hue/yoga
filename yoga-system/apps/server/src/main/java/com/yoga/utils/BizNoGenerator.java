package com.yoga.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 业务单据编号生成器
 */
public class BizNoGenerator {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static String orderNo() {
        return "OD" + LocalDateTime.now().format(DTF) + RandomUtil.randomNumbers(4);
    }

    public static String bookingNo() {
        return "BK" + LocalDateTime.now().format(DTF) + RandomUtil.randomNumbers(4);
    }

    public static String cardNo() {
        return "MC" + RandomUtil.randomNumbers(10);
    }

    public static String uuid() {
        return IdUtil.fastSimpleUUID();
    }
}
