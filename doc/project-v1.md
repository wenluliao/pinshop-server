这是一个一份经过深度整合、查漏补缺的**全栈开发架构文档 (Master Design Document)**。

这份文档将**技术架构（Java 25 + Native）**与**全链路业务（C端秒杀拼团 + B端进销存管理）**完美融合，并详细定义了**数据库 Schema**。你可以直接将此文档作为 Prompt 发送给 Claude Code、Cursor 或 Windsurf 进行项目初始化。

---

# 📘 FlashGroupBuy 全栈开发架构文档 (Project V1.5)

## 1. 项目愿景与技术标准
*   **项目定位**: 基于微信小程序的高并发团购与秒杀电商平台，具备完整的供应链管理能力。
*   **技术哲学**: Cloud-Native, Memory-First, High-Concurrency.
*   **核心版本**: Java 25 (Preview) + Spring Boot 3.4+ + GraalVM Native Image.

### 1.1 技术栈详细清单
| 层级 | 技术选型 | 关键配置/说明 |
| :--- | :--- | :--- |
| **Runtime** | **OpenJDK 25** | 启用 `--enable-preview`，全站使用虚拟线程 (Virtual Threads)。 |
| **Framework** | **Spring Boot 3.4** | WebMvc (Tomcat/Undertow), Native Image Support. |
| **Data Access** | **MyBatis-Flex** | 高性能 ORM，支持 AOT 编译，比 MP 更轻量。 |
| **Database** | **MySQL 8.0** | InnoDB 引擎，严格的事务控制。 |
| **Cache** | **Redis 7.2** | 核心抗压层 (Lua Scripting, Stream/List). |
| **Messaging** | **RabbitMQ / RocketMQ** | 异步削峰，解耦交易与履约。 |
| **Utils** | **Jackson** | 使用 Java `Record` 作为 DTO，减少反射开销。 |

---

## 2. 数据库设计 (Database Schema)

> **设计原则**:
> 1.  **扩展性**: 核心表预留 `extra_json` 字段。
> 2.  **精度**: 金额字段统一使用 `DECIMAL(10, 2)`。
> 3.  **性能**: 关键查询字段强制建索引 (`idx_`).
> 4.  **审计**: 库存变动必须有流水。

### 2.1 用户与基础域 (User & Base)

```sql
-- 用户表 (C端)
CREATE TABLE `user` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `openid` varchar(64) NOT NULL COMMENT '微信OpenID',
  `nickname` varchar(64),
  `avatar_url` varchar(255),
  `phone` varchar(20),
  `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime(3),
  UNIQUE KEY `uk_openid` (`openid`)
);

-- 用户地址簿
CREATE TABLE `user_address` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `receiver_name` varchar(32) NOT NULL,
  `receiver_phone` varchar(20) NOT NULL,
  `province` varchar(32),
  `city` varchar(32),
  `district` varchar(32),
  `detail_addr` varchar(255),
  `is_default` tinyint DEFAULT 0,
  INDEX `idx_user` (`user_id`)
);
```

### 2.2 商品与库存域 (Product & Inventory)

```sql
-- 商品规格 (SPU)
CREATE TABLE `product_spu` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `category_id` int COMMENT '分类ID',
  `main_image` varchar(255),
  `detail_images` json COMMENT '详情图列表',
  `status` tinyint DEFAULT 0 COMMENT '0:草稿 1:上架 2:下架',
  `create_time` datetime(3),
  INDEX `idx_status` (`status`)
);

-- 商品库存单元 (SKU)
CREATE TABLE `product_sku` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `spu_id` bigint NOT NULL,
  `specs` json COMMENT '规格KV, 如 {"color":"红", "size":"L"}',
  `market_price` decimal(10,2) COMMENT '划线价',
  `sale_price` decimal(10,2) COMMENT '日常售价',
  `stock` int NOT NULL DEFAULT 0 COMMENT '当前可用库存',
  INDEX `idx_spu` (`spu_id`)
);

-- [关键] 库存流水账 (财务审计用)
CREATE TABLE `stock_ledger` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `sku_id` bigint NOT NULL,
  `biz_type` varchar(32) NOT NULL COMMENT 'ORDER_OUT(销售), STOCK_IN(入库), REFUND_IN(退货)',
  `biz_id` varchar(64) NOT NULL COMMENT '关联单号',
  `change_num` int NOT NULL COMMENT '变动数量, +/-',
  `balance_snapshot` int NOT NULL COMMENT '变动后余额快照',
  `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_sku_time` (`sku_id`, `create_time`)
);
```

### 2.3 营销域 (Marketing - Flash & Group)

```sql
-- 秒杀场次配置
CREATE TABLE `flash_event` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `title` varchar(64) COMMENT '如: 双11晚8点场',
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `status` tinyint DEFAULT 0 COMMENT '0:未开始 1:进行中 2:已结束'
);

-- 秒杀商品关联 (独立库存)
CREATE TABLE `flash_item` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `event_id` bigint NOT NULL,
  `sku_id` bigint NOT NULL,
  `flash_price` decimal(10,2) NOT NULL,
  `flash_stock` int NOT NULL COMMENT '分配给秒杀的独立库存',
  `lock_stock` int DEFAULT 0 COMMENT '已锁定的库存',
  `limit_per_user` int DEFAULT 1,
  UNIQUE KEY `uk_event_sku` (`event_id`, `sku_id`)
);

-- 拼团活动规则
CREATE TABLE `group_rule` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `sku_id` bigint NOT NULL,
  `group_price` decimal(10,2) NOT NULL,
  `member_count` int NOT NULL DEFAULT 2 COMMENT '成团人数',
  `duration_hours` int DEFAULT 24 COMMENT '有效期',
  `status` tinyint DEFAULT 1
);

-- 拼团会话 (实际发生的团)
CREATE TABLE `group_session` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `rule_id` bigint NOT NULL,
  `initiator_id` bigint NOT NULL COMMENT '团长ID',
  `status` tinyint DEFAULT 0 COMMENT '0:拼团中 1:成功 2:失败',
  `current_count` int DEFAULT 1,
  `expire_time` datetime NOT NULL,
  INDEX `idx_status` (`status`)
);
```

### 2.4 交易与履约域 (Trade & Fulfillment)

```sql
-- 订单主表
CREATE TABLE `trade_order` (
  `id` bigint PRIMARY KEY COMMENT '分布式ID (Snowflake)',
  `user_id` bigint NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `pay_amount` decimal(10,2) NOT NULL,
  `status` tinyint DEFAULT 10 COMMENT '10:待付 20:待发(已付/拼团成) 30:已发 40:完成 50:已取消 60:售后中',
  `order_type` varchar(16) COMMENT 'NORMAL, FLASH, GROUP',
  `marketing_id` bigint COMMENT '关联的秒杀ID或拼团SessionID',
  `receiver_info` json NOT NULL COMMENT '收货人快照 {name, phone, address}',
  `create_time` datetime(3),
  `pay_time` datetime(3),
  `extra_json` json COMMENT '扩展信息',
  INDEX `idx_user` (`user_id`),
  INDEX `idx_create_time` (`create_time`)
);

-- 订单明细
CREATE TABLE `order_item` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `sku_id` bigint NOT NULL,
  `sku_name` varchar(128),
  `price` decimal(10,2) COMMENT '购买时单价',
  `quantity` int NOT NULL
);

-- 物流发货单
CREATE TABLE `delivery_order` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `logistics_company` varchar(64) COMMENT '顺丰/圆通',
  `tracking_no` varchar(64) COMMENT '运单号',
  `ship_time` datetime DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_order` (`order_id`)
);
```

---

## 3. 核心业务流程与代码逻辑

### 3.1 秒杀引擎 (Flash Sale Engine)

**设计目标**: 扛住 10W+ QPS，数据库零压力。

1.  **预热阶段 (Job)**:
    *   活动开始前，将 `flash_stock` 加载至 Redis: `SET flash:stock:{skuId} 100`。
    *   本地缓存 (Caffeine) 标记 `is_stock_empty: false`。

2.  **秒杀请求处理 (API)**:
    *   **Level 1 (Local Memory)**: 检查 Caffeine `is_stock_empty`，若为 true 直接返回 "秒杀结束"。
    *   **Level 2 (Redis Atomicity)**: 执行 Lua 脚本。
        ```lua
        -- keys[1]: flash:stock:{skuId}
        -- keys[2]: flash:user:{event_id}:{skuId} (用于限购去重)
        local stock = tonumber(redis.call('get', KEYS[1]))
        if stock <= 0 then return -1 end
        if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then return -2 end -- 已买过
        redis.call('decr', KEYS[1])
        redis.call('sadd', KEYS[2], ARGV[1])
        return 1
        ```
    *   **Level 3 (Async MQ)**: Lua 返回 1 后，发送消息 `FlashOrderMessage(userId, skuId)` 到 MQ，立即返回前端 `{"status": "QUEUING"}`。

3.  **异步削峰 (Consumer)**:
    *   消费 MQ 消息 -> 扣减 DB 库存 (乐观锁) -> 创建订单 (`status=10`) -> 写入 Redis 订单状态供前端轮询。

### 3.2 拼团状态机 (Group Buy Logic)

**设计目标**: 社交裂变与库存安全。

*   **开团**: 用户支付成功 -> 创建 `trade_order` (`status=20` 待成团/待发货) -> 创建 `group_session` (`status=0`, `count=1`)。
*   **参团**: 校验 `group_session` 是否满员/过期 -> 用户支付 -> `trade_order` -> `group_session.count++`。
*   **成团判定**:
    *   若 `count == target`: 更新 `group_session` 为成功 -> 触发 MQ 发送“拼团成功通知” -> 推送订单给仓库。
*   **失败判定 (Job)**:
    *   每分钟扫描 `group_session` where `status=0 AND expire_time < NOW()`。
    *   更新为失败 -> 触发自动退款流程 -> 释放库存。

### 3.3 报表统计 (Admin Dashboard)

**设计目标**: 利用 Java 25 虚拟线程并行计算，实现毫秒级报表渲染。

```java
// 伪代码：利用 StructuredTaskScope 并行聚合
public DashboardData getAdminDashboard() {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        var taskGmv = scope.fork(() -> orderRepo.sumTodayGMV());
        var taskUser = scope.fork(() -> userRepo.countNewUsers());
        var taskStock = scope.fork(() -> stockLedger.getLowStockSkus());
        var taskLogistics = scope.fork(() -> deliveryRepo.countPendingShipments());
        
        scope.join(); // 虚拟线程非阻塞等待
        
        return new DashboardData(
            taskGmv.get(), 
            taskUser.get(), 
            taskStock.get(),
            taskLogistics.get()
        );
    }
}
```

---

## 4. 后台管理与物流对接

### 4.1 物流接口抽象 (Adapter Pattern)
为了支持 V1 的手动发货和 V2 的自动对接，定义标准接口：

```java
public interface LogisticsProvider {
    // 创建运单 (V1返回空，V2调用快递API)
    String createWaybill(OrderDTO order);
    
    // 查询轨迹
    List<TraceInfo> queryTrack(String company, String no);
}
```

### 4.2 库存审计
*   **强制逻辑**: 任何对 `product_sku.stock` 的修改，必须在同一个事务中插入 `stock_ledger`。
*   **对账**: 每日定时任务对比 `sku.stock` 与 `sum(ledger.change_num)` 是否一致。

---

## 5. 项目结构与 Prompt 建议

### 5.1 Maven/Gradle 模块划分
```text
flash-buy-backend
├── flash-api-app       // 小程序API入口 (Netty/Tomcat)
├── flash-admin-api     // 管理后台入口
├── flash-core          // 核心业务逻辑 (Domain Service)
├── flash-infra         // 基础设施 (DB, Redis, MQ Impl)
└── flash-common        // 全局对象 (Result, Utils, Exception)
```

### 5.2 给 AI (Claude/Cursor) 的终极 Prompt

你可以复制以下内容开始开发：

> "你是一个精通 **Java 25 (Virtual Threads)**, **Spring Boot 3.4**, **GraalVM Native Image** 的首席架构师。
> 
> 请根据《FlashGroupBuy 全栈开发架构文档 V1》构建项目。
> 
> **主要任务**:
> 1.  **数据层**: 按照文档中的 Schema 使用 MyBatis-Flex 生成 Entity 和 Mapper。确保 `stock_ledger` 的记录逻辑在 Service 层是强制的。
> 2.  **核心交易**: 实现 `SeckillService`，必须包含 Redis Lua 脚本扣减库存和 MQ 异步下单的完整逻辑。
> 3.  **拼团逻辑**: 实现 `GroupBuyService`，包含开团、参团、定时任务检查拼团失败（虚拟线程处理）。
> 4.  **管理后台**: 实现 `AdminDashboardService`，使用 `StructuredTaskScope` 并行聚合销售数据。
> 5.  **规范**: 使用 Java `Record` 作为所有 DTO。日志使用占位符。代码需对 Native Image 友好（减少不必要的反射）。"

这份文档现在涵盖了从底层数据库到顶层业务逻辑的所有细节，兼顾了高性能（Java 25/Redis）和业务完整性（库存审计/报表/物流）。