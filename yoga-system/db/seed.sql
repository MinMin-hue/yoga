-- ============================================================
--  种子数据
-- ============================================================
USE `yoga`;

-- 管理员/教练账号
-- admin / admin123   (BCrypt: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)
-- coach / coach123   (BCrypt: $2a$10$D2H3vS1Z0Ke7/8qB8xwO..tTfd6p/2y5UjkfCkAa2SU7qYNsxD2DG)
INSERT INTO `admin_user` (`username`, `password`, `real_name`, `role`, `phone`, `status`) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '超级管理员', 'ADMIN', '13800000000', 1),
('coach_wang', '$2a$10$D2H3vS1Z0Ke7/8qB8xwO..tTfd6p/2y5UjkfCkAa2SU7qYNsxD2DG', '王老师', 'COACH', '13800000001', 1),
('coach_li',  '$2a$10$D2H3vS1Z0Ke7/8qB8xwO..tTfd6p/2y5UjkfCkAa2SU7qYNsxD2DG', '李老师', 'COACH', '13800000002', 1);

-- 教室
INSERT INTO `room` (`name`, `capacity`, `sort`) VALUES
('1号教室', 20, 1),
('2号教室', 15, 2),
('VIP教室',  5, 3);

-- 课程类型
INSERT INTO `course_type` (`name`, `category`, `times_cost`, `description`, `sort`) VALUES
('哈他瑜伽(团课)',    'GROUP',   1, '基础体式练习',         1),
('流瑜伽(进阶团课)',  'GROUP',   2, '进阶体式与串联',       2),
('空中瑜伽(特色课)',  'SPECIAL', 3, '吊床辅助',             3),
('一对一私教',        'PRIVATE', 1, '个性化定制',           4);

-- 卡类型
INSERT INTO `card_type` (`name`, `card_kind`, `price`, `valid_days`, `total_times`, `description`, `sort`) VALUES
('月卡(30天)',     'TIME',   599.00,  30,  NULL, '自激活起 30 天有效, 不限次数',     1),
('季卡(90天)',     'TIME',  1599.00,  90,  NULL, '自激活起 90 天有效, 不限次数',     2),
('年卡(365天)',    'TIME',  4999.00, 365,  NULL, '自激活起 365 天有效, 不限次数',    3),
('10 次卡',         'TIMES',  999.00, 180,  10,   '180 天内使用 10 次',               4),
('20 次卡',         'TIMES', 1799.00, 365,  20,   '365 天内使用 20 次',               5),
('混合卡(60天/15次)','MIXED', 1999.00, 60,  15,   '60 天或 15 次, 以先到为准',        6);

-- 会员示例
INSERT INTO `member` (`phone`, `nickname`, `gender`, `status`) VALUES
('13900000001', '张小瑜', 2, 1),
('13900000002', '李静雅', 2, 1),
('13900000003', '王力',   1, 1);

-- 系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('booking.cancel_minutes',      '120',   '课程开始前多少分钟内可免费取消(分钟)'),
('booking.stop_minutes',        '15',    '课程开始前多少分钟停止预约(分钟)'),
('booking.no_show_penalty',     '1',     '违约扣除次数'),
('booking.late_checkin_minutes','10',    '迟到多少分钟内可签到(分钟)'),
('order.expire_minutes',        '30',    '订单支付超时时间(分钟)'),
('notification.enabled',        'false', '是否启用通知(短信/微信)');
