# Yoga 瑜伽馆会员管理系统

> 完整的瑜伽馆会员管理业务系统: **用户端 H5 / 微信小程序** + **管理后台 PC** + **后端 API**。

## 一、技术栈

| 端 | 技术 |
|---|---|
| 用户端 | uni-app (Vue 3 + TS + Pinia) → H5 / 微信小程序 |
| 管理后台 | Vue 3 + TypeScript + Element Plus + Vite + ECharts |
| 后端 | Spring Boot 3 + Java 17 + MyBatis-Plus + Spring Security + JWT |
| 数据库 | MySQL 8 |
| 缓存 | Redis 7 |
| 部署 | Docker Compose |

## 二、业务模块

1. **会员卡管理** — 卡类型(时间卡 / 次卡 / 混合卡)、购买、激活、查询、退款
2. **课程管理** — 课程类型、排课(单次/每日/每周)、教练绑定、教室管理
3. **预约管理** — 会员预约、取消、签到、核销、爽约、违约扣次
4. **订单与支付** — 购卡/充值/单课订单(线下支付 + 管理员手动确认)
5. **会员管理** — 会员档案、消费记录、会员卡查询
6. **数据统计** — 营业/课程/会员/教练 4 类统计 + ECharts 可视化
7. **系统设置** — 预约规则、签到规则、违约扣次、订单超时

## 三、目录结构

```
yoga-system/
├── apps/
│   ├── server/         # Spring Boot 后端  (端口 8080)
│   ├── admin/          # Vue3 管理后台    (端口 5173 → 8081)
│   └── h5/             # uni-app 用户端   (端口 5174 → 8082)
├── db/
│   ├── schema.sql      # 数据库 Schema
│   └── seed.sql        # 种子数据
├── docs/               # 设计与接口文档
└── docker-compose.yml
```

## 四、快速启动

### 方案 A: Docker Compose 一键启动(推荐)

```bash
cd yoga-system

# 1) 启动 MySQL 和 Redis
docker compose up -d mysql redis

# 2) 启动后端(开发模式)
cd apps/server
./mvnw spring-boot:run
# 或: mvn spring-boot:run

# 3) 启动管理后台
cd apps/admin
pnpm install        # 或 npm install
pnpm dev

# 4) 启动用户端 H5
cd apps/h5
pnpm install
pnpm dev:h5
```

### 方案 B: 完全 Docker

```bash
# 1. 构建后端镜像
cd apps/server && mvn package -DskipTests

# 2. 构建前端
cd ../admin && pnpm install && pnpm build
cd ../h5 && pnpm install && pnpm build:h5

# 3. 启动
cd ../..
docker compose up -d
```

## 五、访问地址

| 端 | URL | 账号 |
|---|---|---|
| 管理后台 | http://localhost:8081 | `admin / admin123` |
| 用户端 H5 | http://localhost:8082 | 手机号登录(首次自动注册) |
| 后端 API | http://localhost:8080/api | - |

## 六、核心数据库表

| 表 | 说明 |
|---|---|
| `admin_user` | 管理员/教练账号 |
| `member` | 会员 |
| `card_type` | 会员卡类型 |
| `member_card` | 会员卡实例 |
| `course_type` | 课程类型 |
| `course_schedule` | 排课 |
| `room` | 教室 |
| `booking` | 预约记录 |
| `order_info` | 订单 |
| `consumption_record` | 消费流水 |
| `system_config` | 系统配置 |

## 七、核心业务流

### 购卡流程
```
H5 用户下单 (PURCHASE_CARD) → 订单 PENDING
         ↓
管理员后台「确认收款」→ 订单 PAID + 自动创建会员卡(PENDING)
         ↓
管理员/用户「激活会员卡」→ 会员卡 ACTIVE
         ↓
用户可使用会员卡预约课程
```

### 预约流程
```
H5 选择课程 → 选择会员卡 → 提交预约
   ↓ 事务
① 校验课程可预约 + 时间未截止
② 校验会员卡状态/有效期/类型/次数
③ 乐观锁扣减课程容量
④ 扣减会员卡次数
⑤ 写入预约(BOOKED) + 流水
```

### 签到/核销流程
```
管理员扫码/手动签到 → 课程开始前 N 分钟内可签到
   ↓
课程结束定时任务(可手动) → 批量核销 → COMPLETED
```

## 八、状态机

| 对象 | 状态 |
|---|---|
| 会员卡 | PENDING → ACTIVE → (EXPIRED \| NO_REMAIN) → REFUNDED |
| 预约 | BOOKED → (CANCELLED \| CHECKED_IN → COMPLETED \| NO_SHOW) |
| 订单 | PENDING → (PAID → REFUNDED \| CANCELLED) |

状态流转在 `enums` 包内集中校验, 禁止跨级流转。

## 九、待办(后续迭代)

- [ ] 接入微信/支付宝支付(已预留 `payMethod` 字段)
- [ ] 短信/微信模板通知(已预留 `notification.enabled` 开关)
- [ ] 多门店支持(单店起步, 后续扩展)
- [ ] 课程评价
- [ ] 数据导出 Excel

## 十、文档

- [接口文档](docs/api.md)
- [数据库 Schema](../db/schema.sql)
