package com.yoga.utils;

import com.yoga.common.SecurityContext;

/**
 * 通用断言
 */
public class Asserts {

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new com.yoga.common.BizException(404, message);
        }
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new com.yoga.common.BizException(400, message);
        }
    }

    public static void hasLogin() {
        if (SecurityContext.getUserId() == null) {
            throw new com.yoga.common.BizException(401, "请先登录");
        }
    }
}
