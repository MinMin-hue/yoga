-- ============================================================
--  Yoga 瑜伽馆会员管理系统  数据库 Schema
--  MySQL 8.0 / utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS `yoga` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `yoga`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 管理员 / 教练账号
-- ============================================================
DROP TABLE IF EXISTS `admin_user`;
CREATE TABLE `admin_user` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `username`      VARCHAR(50)  NOT NULL COMMENT '登录名',
  `password`      VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码',
  `real_name`     VARCHAR(50)           DEFAULT NULL COMMENT '真实姓名',
  `role`          VARCHAR(20)  NOT NULL DEFAULT 'ADMIN' COMMENT '角色: ADMIN/COACH',
  `phone`         VARCHAR(20)           DEFAULT NULL,
  `avatar`        VARCHAR(255)          DEFAULT NULL,
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB COMMENT = '管理员/教练账号';

-- ============================================================
-- 2. 会员
-- ============================================================
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `phone`         VARCHAR(20)  NOT NULL COMMENT '手机号(账号)',
  `nickname`      VARCHAR(50)           DEFAULT NULL,
  `avatar`        VARCHAR(255)          DEFAULT NULL,
  `gender`        TINYINT               DEFAULT 0 COMMENT '0未知 1男 2女',
  `birthday`      DATE                  DEFAULT NULL,
  `openid`        VARCHAR(64)           DEFAULT NULL COMMENT '微信 openid',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 0停用',
  `remark`        VARCHAR(500)          DEFAULT NULL,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_openid` (`openid`)
) ENGINE = InnoDB COMMENT = '会员';

-- ============================================================
-- 3. 教室
-- ============================================================
DROP TABLE IF EXISTS `room`;
CREATE TABLE `room` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `name`          VARCHAR(50)  NOT NULL COMMENT '教室名称: 1号教室',
  `capacity`      INT          NOT NULL DEFAULT 20 COMMENT '容纳人数',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `sort`          INT          NOT NULL DEFAULT 0,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT = '教室';

-- ============================================================
-- 4. 课程类型
-- ============================================================
DROP TABLE IF EXISTS `course_type`;
CREATE TABLE `course_type` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `name`          VARCHAR(50)  NOT NULL COMMENT '课程名: 团课(普通)/私教课',
  `category`      VARCHAR(20)  NOT NULL DEFAULT 'GROUP' COMMENT 'GROUP/SPECIAL/PRIVATE',
  `times_cost`    INT          NOT NULL DEFAULT 1 COMMENT '单次消耗次数',
  `cover`         VARCHAR(255)          DEFAULT NULL,
  `description`   VARCHAR(500)          DEFAULT NULL,
  `status`        TINYINT      NOT NULL DEFAULT 1,
  `sort`          INT          NOT NULL DEFAULT 0,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT = '课程类型';

-- ============================================================
-- 5. 会员卡类型
-- ============================================================
DROP TABLE IF EXISTS `card_type`;
CREATE TABLE `card_type` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT,
  `name`              VARCHAR(50)  NOT NULL COMMENT '卡名: 月卡/季卡/10次卡',
  `card_kind`         VARCHAR(20)  NOT NULL COMMENT 'TIME=时间卡 TIMES=次卡 MIXED=混合卡',
  `price`             DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '售价(元)',
  `valid_days`        INT                   DEFAULT NULL COMMENT '有效天数, 0/空=不限',
  `total_times`       INT                   DEFAULT NULL COMMENT '总次数, 0/空=不限',
  `applicable_types`  VARCHAR(255)          DEFAULT NULL COMMENT '可约课程类型ID集合,逗号分隔,空=全部',
  `description`       VARCHAR(500)          DEFAULT NULL,
  `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `sort`              INT          NOT NULL DEFAULT 0,
  `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT = '会员卡类型';

-- ============================================================
-- 6. 会员卡实例
-- ============================================================
DROP TABLE IF EXISTS `member_card`;
CREATE TABLE `member_card` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `card_no`         VARCHAR(32)   NOT NULL COMMENT '卡号',
  `member_id`       BIGINT        NOT NULL,
  `card_type_id`    BIGINT        NOT NULL,
  `card_type_name`  VARCHAR(50)   NOT NULL COMMENT '冗余防卡类型改名',
  `price`           DECIMAL(10,2) NOT NULL DEFAULT 0,
  `card_kind`       VARCHAR(20)   NOT NULL,
  `valid_days`      INT                    DEFAULT NULL,
  `total_times`     INT                    DEFAULT NULL,
  `remain_times`    INT                    DEFAULT NULL,
  `valid_from`      DATETIME              DEFAULT NULL COMMENT '激活开始时间',
  `valid_to`        DATETIME              DEFAULT NULL COMMENT '到期时间',
  `status`          VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/ACTIVE/EXPIRED/NO_REMAIN/REFUNDED',
  `activated_at`    DATETIME              DEFAULT NULL,
  `refunded_at`     DATETIME              DEFAULT NULL,
  `order_id`        BIGINT                 DEFAULT NULL,
  `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_card_no` (`card_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_status` (`status`)
) ENGINE = InnoDB COMMENT = '会员卡实例';

-- ============================================================
-- 7. 排课(课程实例)
-- ============================================================
DROP TABLE IF EXISTS `course_schedule`;
CREATE TABLE `course_schedule` (
  `id`                BIGINT     NOT NULL AUTO_INCREMENT,
  `course_type_id`    BIGINT     NOT NULL,
  `course_type_name`  VARCHAR(50) NOT NULL,
  `coach_id`          BIGINT     NOT NULL,
  `coach_name`        VARCHAR(50) NOT NULL,
  `room_id`           BIGINT              DEFAULT NULL,
  `room_name`         VARCHAR(50)         DEFAULT NULL,
  `start_time`        DATETIME   NOT NULL,
  `end_time`          DATETIME   NOT NULL,
  `capacity`          INT        NOT NULL DEFAULT 20,
  `booked_count`      INT        NOT NULL DEFAULT 0,
  `cancel_deadline`   DATETIME            DEFAULT NULL COMMENT '可取消截止',
  `checkin_before`    INT        NOT NULL DEFAULT 15 COMMENT '可签到提前分钟',
  `status`            VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' COMMENT 'SCHEDULED/ONGOING/FINISHED/CANCELLED',
  `remark`            VARCHAR(255)        DEFAULT NULL,
  `version`           INT        NOT NULL DEFAULT 0 COMMENT '乐观锁',
  `created_at`        DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`        DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_coach_id` (`coach_id`),
  KEY `idx_course_type_id` (`course_type_id`)
) ENGINE = InnoDB COMMENT = '排课';

-- ============================================================
-- 8. 预约记录
-- ============================================================
DROP TABLE IF EXISTS `booking`;
CREATE TABLE `booking` (
  `id`              BIGINT     NOT NULL AUTO_INCREMENT,
  `booking_no`      VARCHAR(32) NOT NULL,
  `member_id`       BIGINT     NOT NULL,
  `member_name`     VARCHAR(50)         DEFAULT NULL,
  `schedule_id`     BIGINT     NOT NULL,
  `card_id`         BIGINT     NOT NULL,
  `card_no`         VARCHAR(32)         DEFAULT NULL,
  `cost_times`      INT        NOT NULL DEFAULT 1,
  `status`          VARCHAR(20) NOT NULL DEFAULT 'BOOKED' COMMENT 'BOOKED/CANCELLED/CHECKED_IN/COMPLETED/NO_SHOW',
  `booked_at`       DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cancelled_at`    DATETIME            DEFAULT NULL,
  `cancel_reason`   VARCHAR(255)        DEFAULT NULL,
  `checked_in_at`   DATETIME            DEFAULT NULL,
  `completed_at`    DATETIME            DEFAULT NULL,
  `is_penalty`      TINYINT    NOT NULL DEFAULT 0 COMMENT '是否违约 1是',
  `created_at`      DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_booking_no` (`booking_no`),
  UNIQUE KEY `uk_member_schedule` (`member_id`, `schedule_id`),
  KEY `idx_schedule_id` (`schedule_id`),
  KEY `idx_status` (`status`)
) ENGINE = InnoDB COMMENT = '预约记录';

-- ============================================================
-- 9. 订单
-- ============================================================
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `order_no`        VARCHAR(32)   NOT NULL,
  `member_id`       BIGINT        NOT NULL,
  `member_name`     VARCHAR(50)            DEFAULT NULL,
  `order_type`      VARCHAR(20)   NOT NULL COMMENT 'PURCHASE_CARD/RECHARGE/SINGLE_COURSE',
  `card_type_id`    BIGINT                 DEFAULT NULL,
  `card_type_name`  VARCHAR(50)            DEFAULT NULL,
  `course_type_id`  BIGINT                 DEFAULT NULL,
  `course_type_name` VARCHAR(50)           DEFAULT NULL,
  `amount`          DECIMAL(10,2) NOT NULL DEFAULT 0,
  `pay_method`      VARCHAR(20)            DEFAULT NULL COMMENT 'OFFLINE/WECHAT/ALIPAY',
  `status`          VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PAID/CANCELLED/REFUNDED',
  `pay_time`        DATETIME               DEFAULT NULL,
  `paid_by`         BIGINT                 DEFAULT NULL COMMENT '管理员确认人',
  `refund_time`     DATETIME               DEFAULT NULL,
  `refund_reason`   VARCHAR(255)           DEFAULT NULL,
  `remark`          VARCHAR(255)           DEFAULT NULL,
  `expire_at`       DATETIME               DEFAULT NULL COMMENT '支付超时时间',
  `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_status` (`status`)
) ENGINE = InnoDB COMMENT = '订单';

-- ============================================================
-- 10. 会员消费/流水
-- ============================================================
DROP TABLE IF EXISTS `consumption_record`;
CREATE TABLE `consumption_record` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `member_id`     BIGINT        NOT NULL,
  `type`          VARCHAR(20)   NOT NULL COMMENT 'PURCHASE/RECHARGE/CHECKIN/REFUND/PENALTY',
  `amount`        DECIMAL(10,2) NOT NULL DEFAULT 0,
  `times_delta`   INT           NOT NULL DEFAULT 0 COMMENT '次数变化 +增加 -减少',
  `card_id`       BIGINT                 DEFAULT NULL,
  `card_no`       VARCHAR(32)            DEFAULT NULL,
  `booking_id`    BIGINT                 DEFAULT NULL,
  `order_id`      BIGINT                 DEFAULT NULL,
  `remark`        VARCHAR(255)           DEFAULT NULL,
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_type` (`type`)
) ENGINE = InnoDB COMMENT = '会员消费流水';

-- ============================================================
-- 11. 系统配置
-- ============================================================
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `config_key`    VARCHAR(50)  NOT NULL,
  `config_value`  VARCHAR(500) NOT NULL,
  `description`   VARCHAR(255)          DEFAULT NULL,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE = InnoDB COMMENT = '系统配置';

SET FOREIGN_KEY_CHECKS = 1;
