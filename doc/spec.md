技术规范文档
这是一个非常前沿且高性能的技术选型。你提到的“Spring Boot”应该是指基于 **Spring Framework 6** 的 **Spring Boot 3.4+** 版本。结合 **Java 25**（Loom 项目完全成熟版）和 **GraalVM Native Image**（参考文章中的核心，用于极低内存占用和瞬时启动），我们可以构建一个**云原生、毫秒级响应**的秒杀系统。

---

# 🚀 团购秒杀小程序后端开发架构文档 (Java 25 + Spring Boot 3 + Native)

## 1. 项目概述与设计哲学
*   **项目名称**: FlashGroupBuy-Backend
*   **核心目标**: 高并发（支持 10万+ QPS）、低延迟（<50ms）、极低内存占用（Native Image 模式下 <100MB）。
*   **设计原则**:
    *   **Everything is Virtual**: 全面采用 Java 25 虚拟线程，摒弃传统线程池调优。
    *   **Memory First**: 针对 GraalVM Native Image 优化，减少反射，使用编译时处理。
    *   **Async Core**: 核心交易链路全异步化（Redis + MQ）。
    *   **DDD (领域驱动)**: 分离 `Command` (写) 和 `Query` (读) 职责。

---

## 2. 技术栈详细规范 (Tech Stack)

| 组件 | 版本/选型 | 说明 |
| :--- | :--- | :--- |
| **JDK** | **OpenJDK 25** | 启用 `--enable-preview` (视情况)，核心利用 Virtual Threads 和 Scoped Values。 |
| **Framework** | **Spring Boot 3.4+** | 基于 Spring Framework 6.2，原生支持虚拟线程。 |
| **AOT Compilation** | **GraalVM CE/EE** | 构建 Native Image，实现瞬时启动和极致内存压缩。 |
| **Database** | MySQL 8.0 | 存储持久化数据（订单、用户）。 |
| **Cache** | Redis 7.2 | 核心抗压层（Lua 脚本扣减库存、缓存热点数据）。 |
| **Local Cache** | Caffeine | 进程内一级缓存，减少 Redis 网络开销。 |
| **Message Queue** | RocketMQ 5.x | 削峰填谷，事务消息保证最终一致性。 |
| **ORM** | MyBatis-Flex | 相比 MP 更轻量，对 Native Image 支持更好，性能更高。 |
| **JSON** | Jackson / Fastjson2 | 需配置 Native 反射元数据。 |

---

## 3. 核心架构设计 (Architecture)

### 3.1 线程模型 (Java 25 Virtual Threads)
*   **配置**:
    在 `application.yml` 中开启虚拟线程：
    ```yaml
    spring:
      threads:
        virtual:
          enabled: true
    ```
*   **容器**: 使用内嵌 Tomcat 或 Undertow，不再设置 `max-threads=200`，依靠虚拟线程调度器处理海量并发连接。
*   **并发策略**: 遇到 I/O (DB, Redis, HTTP) 时，虚拟线程自动挂起 (Unmount)，不阻塞系统线程 (Carrier Thread)。

### 3.2 读写分离 (CQRS)
*   **读服务 (Query)**:
    *   商品列表、详情：直接走 `Caffeine (Local)` -> `Redis` -> `MySQL` 三级缓存。
    *   使用 `Spring Cache` 注解简化逻辑。
*   **写服务 (Command)**:
    *   秒杀、拼团：**不直接操作 DB**。
    *   流程：`Request` -> `Redis (Lua Pre-check)` -> `MQ` -> `Async Consumer` -> `MySQL`。

### 3.3 内存优化 (GraalVM Native)
*   避免使用动态代理过重的库。
*   所有 DTO/VO 使用 Java `record` 类型（减少类头开销，不可变）。
*   在 `pom.xml` 配置 `native-maven-plugin`。

---

## 4. 关键模块详细设计

### 4.1 领域模型 (DDD)
请按以下包结构生成代码：
```text
com.flashbuy
├── api           // Controller (Web Layer)
├── application   // Service (Use Cases)
├── domain        // Entity, Aggregate, Repository Interface
│   ├── item      // 商品域
│   ├── trade     // 交易域 (订单/秒杀)
│   └── user      // 用户域
├── infrastructure// Persistence, MQ impl, Cache impl
└── common        // Result, Exception, Utils
```

### 4.2 秒杀/拼团核心流程 (The "Seckill" Engine)

**接口**: `POST /api/v1/trade/seckill`

**逻辑流程 (AI 请严格执行此逻辑)**:
1.  **前置风控**: 校验 `UserContext`，检查 IP 限流 (RateLimiter)。
2.  **本地内存标记**: 检查 `LocalCache.get("stock_empty_" + goodsId)`，若为 true 直接返回“已抢光”（阻挡 90% 流量）。
3.  **Redis 原子扣减 (Lua Script)**:
    *   Key: `seckill_stock:{skuId}`
    *   执行 `DECR`。
    *   **判定**:
        *   若返回值 < 0: 恢复库存 (`INCR`), 设置本地内存标记 `stock_empty`, 返回失败。
        *   若返回值 >= 0: 进入第 4 步。
4.  **发送 MQ 消息**:
    *   构建 `TradeMessage` (Record 类型)。
    *   包含：`userId`, `skuId`, `price`, `timestamp`。
    *   发送至 `seckill_order_topic`。
5.  **极速响应**: 立即返回前端 `{"status": "QUEUING", "orderId": null}`。前端开启轮询模式。

**异步消费者 (Consumer)**:
1.  监听 `seckill_order_topic`。
2.  **数据库落库**:
    *   开启事务。
    *   扣减 MySQL 库存 (乐观锁: `UPDATE stock SET num=num-1 WHERE id=? AND num>0`)。
    *   创建订单记录。
    *   创建秒杀记录（防止单人重复买）。
3.  **缓存回写**: 写入订单状态到 Redis 供前端轮询查询。

### 4.3 数据库设计 (Schema)

使用 MySQL 8.0，表引擎 InnoDB。

```sql
-- 秒杀商品表
CREATE TABLE `flash_item` (
  `id` bigint PRIMARY KEY,
  `sku_id` bigint NOT NULL,
  `flash_price` decimal(10,2) NOT NULL,
  `stock_count` int NOT NULL,
  `start_time` datetime(3) NOT NULL,
  `end_time` datetime(3) NOT NULL,
  INDEX `idx_time` (`start_time`, `end_time`)
);

-- 订单表 (分库分表键: user_id)
CREATE TABLE `trade_order` (
  `id` bigint PRIMARY KEY,
  `user_id` bigint NOT NULL,
  `status` tinyint DEFAULT 0 COMMENT '0:未付 1:已付 -1:取消',
  `total_amount` decimal(10,2),
  `create_time` datetime(3) DEFAULT CURRENT_TIMESTAMP
);
```

---

## 5. 高性能代码规范 (Coding Standards for AI)

### 5.1 Java 25 特性应用
*   **虚拟线程**:
    *   **不要**创建自定义线程池 (`ExecutorService`).
    *   使用 `Executors.newVirtualThreadPerTaskExecutor()` 如果必须手动异步。
    *   示例:
        ```java
        // 推荐：在 Service 中并发获取数据
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var userTask = scope.fork(() -> userClient.getUser(uid));
            var itemTask = scope.fork(() -> itemRepo.findById(itemId));
            
            scope.join().throwIfFailed();
            // 处理结果
        }
        ```
*   **DTO 定义**: 必须使用 `record`。
    ```java
    public record SeckillRequest(Long skuId, Integer count) {}
    ```

### 5.2 日志规范 (Logging)
为了极致性能，日志 I/O 不能阻塞业务。
*   使用 **Slf4j** + **Logback Async Appender**。
*   **禁止** 字符串拼接日志，必须使用占位符。
    *   ❌ `log.info("User " + uid + " bought " + skuId);`
    *   ✅ `log.info("User {} bought {}", uid, skuId);`

### 5.3 异常处理
*   定义全局 `GlobalExceptionHandler`。
*   业务异常不打印堆栈信息（重写 `fillInStackTrace`），减少 CPU 消耗。

---

## 6. 开发环境与 Prompt 示例

### 6.1 给 Claude 的初始 Prompt
> "你是一个精通 Java 25、Spring Boot 3 和高并发架构的资深后端架构师。请根据以下《FlashGroupBuy 后端架构文档》为我生成代码。
> 要求：
> 1. 所有 I/O 操作利用 Spring Boot 虚拟线程特性。
> 2. 使用 Record 代替 Lombok @Data。
> 3. 核心秒杀逻辑需包含 Redis Lua 脚本。
> 4. 代码需考虑 GraalVM Native Image 兼容性（减少反射）。"

### 6.2 关键类代码示例 (Lua Script)

```java
@Component
public class StockLuaScript {
    // 预加载脚本，避免每次请求编译
    private final DefaultRedisScript<Long> script;

    public StockLuaScript() {
        script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/deduct_stock.lua"));
        script.setResultType(Long.class);
    }

    public Long deduct(String key, int count) {
        // RedisTemplate 调用
    }
}
```

```lua
-- src/main/resources/lua/deduct_stock.lua
local key = KEYS[1]
local count = tonumber(ARGV[1])
local current = tonumber(redis.call('get', key) or "0")

if current >= count then
    redis.call('decrby', key, count)
    return 1
else
    return -1
end
```

---

## 7. 部署与压测预期
*   **构建**: `mvn -Pnative native:compile`
*   **运行**: `./target/flash-buy-backend` (无 JVM 启动参数，操作系统直接调度)
*   **内存预期**: 启动后驻留内存 (RSS) 约 50MB - 100MB。
*   **性能预期**: 单实例 (4C8G) 可承载 5000+ QPS (纯计算+缓存)，数据库写入瓶颈由 MQ 缓冲。
