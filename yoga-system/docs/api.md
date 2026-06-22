# Yoga API 接口文档

> Base URL: `http://localhost:8080/api`
> 鉴权: 除 `/auth/**`, `/h5/auth/**` 外, 需在 Header 携带 `Authorization: Bearer <token>`

## 一、统一响应

```json
{
  "code": 0,
  "message": "success",
  "data": { ... }
}
```

- `code = 0` 成功
- `code = 401` 未登录
- `code = 400` 参数错误
- `code = 409` 业务冲突
- `code = 500` 系统异常

## 二、认证模块

### 2.1 管理员/教练登录
- `POST /auth/admin/login`
- Body: `{ "username": "admin", "password": "admin123" }`
- 响应: `{ token, profile: { id, username, realName, role } }`

### 2.2 会员登录(H5)
- `POST /auth/member/login`
- Body: `{ "phone": "13800000000" }`
- 响应: `{ token, profile: { id, phone, nickname } }`
- 说明: 简化版, 首次登录自动注册, 无短信验证码

## 三、会员管理

### 3.1 管理后台
| 接口 | 方法 | 说明 |
|---|---|---|
| `/admin/member/page` | POST | 分页查询 `{ keyword, status, pageNum, pageSize }` |
| `/admin/member/{id}` | GET | 详情 |
| `/admin/member/create` | POST | 新增 |
| `/admin/member/update` | POST | 编辑 |
| `/admin/member/delete/{id}` | POST | 删除 |

### 3.2 H5 会员端
| 接口 | 方法 | 说明 |
|---|---|---|
| `/h5/member/profile` | GET | 个人资料 |
| `/h5/member/cards` | GET | 我的会员卡 |
| `/h5/member/records` | GET | 消费记录 |

## 四、会员卡管理

### 4.1 卡类型
| 接口 | 方法 | 说明 |
|---|---|---|
| `/admin/card-type/list` | GET | 列表 |
| `/admin/card-type/page` | POST | 分页 |
| `/admin/card-type/create` | POST | 新增 |
| `/admin/card-type/update` | POST | 编辑 |
| `/admin/card-type/delete/{id}` | POST | 删除 |
| `/h5/card-type/list` | GET | H5 列表(仅启用) |

#### CardTypeUpsertDTO
```json
{
  "name": "月卡",
  "cardKind": "TIME",  // TIME / TIMES / MIXED
  "price": 599,
  "validDays": 30,      // 时间卡必填
  "totalTimes": 10,     // 次卡必填
  "applicableTypes": [1, 2],  // 可约课程类型, 空=全部
  "description": "...",
  "status": 1
}
```

### 4.2 会员卡实例
| 接口 | 方法 | 说明 |
|---|---|---|
| `/admin/member-card/{id}` | GET | 详情 |
| `/admin/member-card/list-by-member/{memberId}` | GET | 某会员的所有卡 |
| `/admin/member-card/activate` | POST | 激活 `{ cardId }` |
| `/admin/member-card/refund` | POST | 退款 `{ cardId, reason? }` |

## 五、课程与排课

| 接口 | 方法 | 说明 |
|---|---|---|
| `/admin/course-type/list` | GET | 课程类型列表 |
| `/admin/course-type/upsert` | POST | 新增/编辑 |
| `/admin/course-type/delete/{id}` | POST | 删除 |
| `/admin/room/list` | GET | 教室列表 |
| `/admin/room/upsert` | POST | 新增/编辑教室 |
| `/admin/room/delete/{id}` | POST | 删除 |
| `/admin/coach/list` | GET | 教练列表 |
| `/admin/schedule/page` | POST | 排课分页(支持日期/课程/教练过滤) |
| `/admin/schedule/upsert` | POST | 新增/编辑排课 |
| `/admin/schedule/cancel/{id}` | POST | 取消 |
| `/admin/schedule/delete/{id}` | POST | 删除 |
| `/h5/course-type/list` | GET | H5 课程类型 |
| `/h5/schedule/list?date=2026-06-19&courseTypeId=1` | GET | H5 排课 |

#### 排课 upsert
```json
{
  "courseTypeId": 1,
  "coachId": 2,
  "roomId": 1,
  "startTime": "2026-06-19T10:00:00",
  "endTime": "2026-06-19T11:00:00",
  "capacity": 20,
  "repeatType": "WEEKLY",  // ONCE / DAILY / WEEKLY
  "repeatCount": 4,
  "weekDays": [1, 3, 5]
}
```

## 六、预约管理

| 接口 | 方法 | 说明 |
|---|---|---|
| `/h5/booking/create` | POST | 创建预约 `{ scheduleId, cardId }` |
| `/h5/booking/cancel/{id}` | POST | 取消 |
| `/h5/booking/my` | GET | 我的预约 `?status=BOOKED` |
| `/admin/booking/page` | POST | 分页 |
| `/admin/booking/by-schedule/{id}` | GET | 某排课的所有预约 |
| `/admin/booking/check-in/{id}` | POST | 签到 |
| `/admin/booking/complete/{id}` | POST | 核销 |
| `/admin/booking/cancel/{id}` | POST | 管理员取消 |

#### 预约状态
- `BOOKED` 已预约
- `CHECKED_IN` 已签到
- `COMPLETED` 已核销
- `CANCELLED` 已取消
- `NO_SHOW` 爽约(自动标记)

## 七、订单管理

| 接口 | 方法 | 说明 |
|---|---|---|
| `/h5/order/create` | POST | 创建订单 `{ orderType, cardTypeId?, amount? }` |
| `/h5/order/cancel/{id}` | POST | 取消 |
| `/admin/order/page` | POST | 分页查询 |
| `/admin/order/{id}` | GET | 详情 |
| `/admin/order/confirm-pay` | POST | 线下支付确认 `{ orderId, payMethod, remark? }` |
| `/admin/order/cancel/{id}` | POST | 取消 |
| `/admin/order/refund/{id}` | POST | 退款 `?reason=...` |

#### 订单类型
- `PURCHASE_CARD` 购卡
- `RECHARGE` 充值
- `SINGLE_COURSE` 单课

## 八、统计

| 接口 | 方法 | 说明 |
|---|---|---|
| `/statistics/revenue?range=month` | GET | 营业统计 |
| `/statistics/course?range=month` | GET | 课程上座率 |
| `/statistics/member?range=month` | GET | 会员统计 |
| `/statistics/coach?range=month` | GET | 教练课时 |

`range` 取值: `day` / `week` / `month` / `year`

## 九、系统设置

| 接口 | 方法 | 说明 |
|---|---|---|
| `/system/config/all` | GET | 全部配置(简化版) |
| `/system/config/detail` | GET | 全部配置(详细) |
| `/system/config/update` | POST | 批量更新 `{ key: value }` |

#### 配置项
| Key | 默认 | 说明 |
|---|---|---|
| `booking.stop_minutes` | 15 | 课程开始前停止预约的分钟数 |
| `booking.cancel_minutes` | 120 | 免费取消窗口(分钟) |
| `booking.no_show_penalty` | 1 | 违约扣除次数 |
| `booking.late_checkin_minutes` | 10 | 迟到可签到(分钟) |
| `order.expire_minutes` | 30 | 订单支付超时 |
| `notification.enabled` | false | 是否启用通知 |
