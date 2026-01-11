# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Run Commands

### Prerequisites
- **JDK 25** with `--enable-preview` flag support
- **Maven 3.9+**
- **MySQL 8.0**, **Redis 7.2**, **RocketMQ 5.x** (for full functionality)

### Building the Project

```bash
# Standard JVM build
mvn clean package

# Skip tests during build
mvn clean package -DskipTests

# Native Image build (requires GraalVM)
mvn -Pnative native:compile
```

### Running the Application

```bash
# JVM mode (always use --enable-preview)
java --enable-preview -jar target/pinshop-server-1.0.0.jar

# Native mode (compiled with GraalVM)
./target/pinshop-server
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=SeckillServiceTest

# Run with Java 25 preview features
mvn test -DargLine="--enable-preview"
```

### Database Setup

```bash
# Initialize database schema
mysql -u root -p < src/main/resources/schema.sql
```

## Architecture Overview

### Core Design Philosophy

This is a **high-concurrency flash sale platform** targeting **100K+ QPS** with **<50ms latency**. The architecture prioritizes:
- **Everything is Virtual**: Java 25 virtual threads throughout
- **Memory First**: Optimized for GraalVM Native Image compilation
- **Async Core**: Write operations decoupled via MQ
- **DDD Structure**: Domain-driven design with clear boundaries

### The Three-Level Defense (Seckill Engine)

The flash sale engine (`SeckillService`) implements a critical performance pattern:

```
Request → LocalCache (Caffeine) → Redis Lua (Atomic) → MQ (Async) → Response
   ↓            ↓                      ↓                    ↓
Level 1     Level 2               Level 3             Immediate
90% block   Atomic deduction     Order creation      return
```

**Key Flow**: `SeckillService.execute()`:
1. Check `LocalStockCache.isEmpty()` - returns immediately if stock empty (blocks ~90% of requests)
2. Execute Redis Lua script for atomic stock deduction (`StockLuaScript`)
3. Send MQ message (`TradeOrderMessage`) for async order creation
4. Return `SeckillResponse.queuing()` immediately (frontend polls for status)

**Lua Script**: `src/main/resources/lua/deduct_stock.lua`
- Atomic stock check and deduction
- User limit validation (prevents duplicate purchases)
- Returns: remaining stock (>0), -1 (insufficient), -2 (already bought)

### Parallel Aggregation Pattern

Use `StructuredTaskScope` for parallel I/O operations (see `AdminDashboardService`):

```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var task1 = scope.fork(() -> repository.query1());
    var task2 = scope.fork(() -> repository.query2());
    scope.join().throwIfFailed();
    // Process results
}
```

This leverages Java 25 virtual threads for concurrent database/redis calls without blocking carrier threads.

### Package Structure (DDD)

```
com.flashbuy/
├── api/                    # REST Controllers (thin layer)
├── application/            # Business services / use cases
│   ├── seckill/           # Flash sale logic
│   ├── groupbuy/          # Group buying logic
│   └── admin/             # Dashboard aggregation
├── domain/                # Domain models (no framework deps)
│   ├── user/             # User, UserAddress entities
│   ├── item/             # ProductSpu, ProductSku, FlashItem
│   ├── trade/            # TradeOrder
│   └── marketing/        # GroupRule, GroupSession
│   └── [domain]/mapper/  # MyBatis-Flex mapper interfaces
└── infrastructure/       # Technical implementations
    ├── config/          # Spring configuration
    ├── cache/           # Redis, Caffeine implementations
    └── mq/              # RocketMQ producers/consumers
```

**Important**: Domain entities should have zero framework dependencies. Use plain Java classes with getters/setters (not Lombok). DTOs must use `record` types for Native Image optimization.

### Data Access Pattern (MyBatis-Flex)

MyBatis-Flex uses `QueryWrapper` for query construction:

```java
// Correct pattern
mapper.selectOneByQuery(
    QueryWrapper.create()
        .where(Entity::getField).eq(value)
        .and(Entity::getOther).eq(otherValue)
        .limit(1)
);

// NOT supported: Entity::getField.eq(value).and(...)
```

All repositories wrap mappers and provide domain-specific query methods.

### Group Buy State Machine

`GroupBuyService` manages group sessions with these states:
- **0 (In Progress)**: Group active, waiting for members
- **1 (Success)**: Target member count reached
- **2 (Failed)**: Expired without reaching target

Key job: `GroupExpireJob` runs every minute (virtual thread) to expire incomplete groups.

## Critical Implementation Rules

### 1. DTOs Must Be Records
All request/response DTOs use Java `record` for Native Image compatibility:

```java
public record SeckillRequest(Long eventId, Long skuId, Integer count, Long userId) {}
```

### 2. Exception Performance Optimization
`BusinessException` overrides `fillInStackTrace()` to return `this`:

```java
@Override
public Throwable fillInStackTrace() {
    return this; // Skip expensive stack trace generation
}
```

### 3. Logging Must Use Placeholders
```java
// CORRECT
log.info("User {} bought sku {}", userId, skuId);

// WRONG - string allocates under pressure
log.info("User " + userId + " bought " + skuId);
```

### 4. Virtual Thread Usage
- **DO NOT** create custom thread pools
- Use `Thread.ofVirtual().start(...)` for one-off async tasks
- Use `StructuredTaskScope` for concurrent I/O aggregation
- Virtual threads auto-unmount on I/O (DB, Redis, HTTP)

### 5. Redis Lua Script Loading
Lua scripts are pre-loaded in `StockLuaScript` constructor via `ClassPathResource`. Do not load scripts per-request.

### 6. Configuration Requirements
- Virtual threads enabled: `spring.threads.virtual.enabled=true`
- MySQL timezone: `Asia/Shanghai`
- MyBatis-Flex: `mapUnderscoreToCamelCase=true`

## Common Patterns

### Adding a New API Endpoint
1. Create record DTO in `application/[domain]/`
2. Add method to Service in `application/[domain]/`
3. Create controller method in `api/`
4. Keep controllers thin - only HTTP handling

### Adding Database Tables
1. Update `src/main/resources/schema.sql`
2. Create entity in `domain/[domain]/entity/`
3. Create mapper interface in `domain/[domain]/mapper/`
4. Create repository in `domain/[domain]/repository/`
5. Use `@Table` annotation with table name

### Extending the Seckill Engine
- Lua script modifications go in `src/main/resources/lua/deduct_stock.lua`
- Stock warm-up: call `seckillService.warmUpStock(skuId, stock)` before events
- Order polling: implement Redis cache write in `SeckillOrderConsumer`

## Testing Strategy

The manual test in `src/test/java/com/flashbuy/ManualTest.java` validates core Record DTOs:

```bash
javac -source 25 --enable-preview -d /tmp/test-classes \
  src/main/java/com/flashbuy/application/seckill/*.java \
  src/main/java/com/flashbuy/common/*.java \
  src/test/java/com/flashbuy/ManualTest.java

java --enable-preview -cp /tmp/test-classes com.flashbuy.test.ManualTest
```

This test verifies Record immutability, factory methods, and business exception optimization.

## Performance Considerations

1. **Seckill path must complete in <50ms** - Local cache + Redis Lua are critical
2. **Never block virtual threads** - All I/O operations should be non-blocking
3. **Minimize reflection** - Use records and explicit code for Native Image
4. **MQ for write spikes** - Seckill orders processed asynchronously
5. **Caffeine as L1 cache** - Check local cache before Redis/DB calls

## Troubleshooting

### Build Errors
- If `--enable-preview` issues: Ensure Maven compiler plugin includes `<arg>--enable-preview</arg>`
- If MyBatis-Flex query errors: Use `QueryWrapper.create()` pattern, not method reference chaining

### Runtime Issues
- Virtual threads not working: Check `spring.threads.virtual.enabled=true` in application.yml
- Redis script failures: Verify Lua script in `src/main/resources/lua/deduct_stock.lua`
- MQ connection errors: RocketMQ must be running on `localhost:9876`

### Performance Issues
- High seckill latency: Check if `LocalStockCache` is being utilized (should block ~90% requests)
- Dashboard slow: Verify `StructuredTaskScope` is used for parallel aggregation
- Memory leaks: Ensure Caffeine cache has `expireAfterWrite` configured
