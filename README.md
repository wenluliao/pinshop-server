# PinShop Server - FlashGroupBuy Backend

> High-performance flash sale and group buying platform powered by **Java 25** + **Spring Boot 3.4** + **GraalVM Native Image**

## Project Overview

PinShop Server is a cutting-edge e-commerce backend system designed for high-concurrency scenarios (100K+ QPS). It leverages the latest Java 25 Virtual Threads technology and GraalVM Native Image compilation to achieve:

- **Ultra-low latency**: <50ms response time
- **Minimal memory footprint**: <100MB RSS in Native mode
- **Massive concurrency**: Virtual threads handle 100K+ concurrent requests
- **Cloud-native**: Designed for containerized deployment

## Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Runtime | OpenJDK 25 | (with Virtual Threads) |
| Framework | Spring Boot | 3.4+ |
| Database | MySQL | 8.0 |
| Cache | Redis | 7.2 |
| ORM | MyBatis-Flex | 1.9.7 |
| Message Queue | RocketMQ | 5.x |
| Local Cache | Caffeine | Latest |
| Compilation | GraalVM Native | 24.1.1 |

## Project Structure

```
pinshop-server/
├── doc/                          # Documentation
│   ├── spec.md                   # Technical specifications
│   └── project-v1.md             # Architecture design
├── src/
│   ├── main/
│   │   java/com/flashbuy/
│   │   │   ├── api/              # REST Controllers
│   │   │   │   ├── SeckillController.java
│   │   │   │   ├── GroupBuyController.java
│   │   │   │   └── AdminController.java
│   │   │   ├── application/      # Business Services
│   │   │   │   ├── seckill/      # Flash sale engine
│   │   │   │   ├── groupbuy/     # Group buy logic
│   │   │   │   └── admin/        # Admin dashboard
│   │   │   ├── domain/           # Domain Models (DDD)
│   │   │   │   ├── user/         # User domain
│   │   │   │   ├── item/         # Product/SKU domain
│   │   │   │   ├── trade/        # Order domain
│   │   │   │   └── marketing/    # Marketing domain
│   │   │   ├── infrastructure/   # Infrastructure
│   │   │   │   ├── config/       # Configuration
│   │   │   │   ├── cache/        # Redis/Cache implementations
│   │   │   │   └── mq/           # Message queue
│   │   │   ├── common/           # Common utilities
│   │   │   └── FlashBuyApplication.java
│   │   └── resources/
│   │       ├── lua/              # Redis Lua scripts
│   │       ├── application.yml   # Configuration
│   │       └── schema.sql        # Database schema
└── pom.xml
```

## Core Features

### 1. Flash Sale Engine

- **3-level defense mechanism**:
  - Level 1: Caffeine local cache (blocks 90% requests when stock empty)
  - Level 2: Redis Lua script (atomic stock deduction)
  - Level 3: RocketMQ async order creation (prevents DB pressure)

- **Key APIs**:
  - `POST /api/v1/seckill/execute` - Execute flash sale
  - `GET /api/v1/seckill/status/{orderId}` - Query order status

### 2. Group Buy Logic

- **Social viral mechanics**:
  - Initiate group (become group leader)
  - Join existing group
  - Auto-expiry with refund
  - Group completion notification

- **Key APIs**:
  - `POST /api/v1/groupbuy/initiate` - Initiate new group
  - `POST /api/v1/groupbuy/join` - Join existing group

### 3. Admin Dashboard

- **Parallel aggregation** using Java 25 `StructuredTaskScope`:
  - Today's GMV
  - New user count
  - Low stock alerts
  - Pending shipments

- **Key API**:
  - `GET /api/v1/admin/dashboard` - Get dashboard data

## Getting Started

### Prerequisites

- JDK 25 (with `--enable-preview` support)
- Maven 3.9+
- MySQL 8.0
- Redis 7.2
- RocketMQ 5.x

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd pinshop-server
   ```

2. **Initialize database**
   ```bash
   mysql -u root -p < src/main/resources/schema.sql
   ```

3. **Configure application**
   ```bash
   # Edit src/main/resources/application.yml
   # Configure database, Redis, RocketMQ connection details
   ```

4. **Build the project**
   ```bash
   # Regular JVM mode
   mvn clean package

   # Native Image mode (GraalVM required)
   mvn -Pnative native:compile
   ```

5. **Run the application**
   ```bash
   # JVM mode
   java --enable-preview -jar target/pinshop-server-1.0.0.jar

   # Native mode
   ./target/pinshop-server
   ```

## Configuration

### Virtual Threads

Virtual threads are enabled by default in `application.yml`:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

### Redis Lua Scripts

Lua scripts are pre-loaded during startup for optimal performance:

```java
// Located at: src/main/resources/lua/deduct_stock.lua
// Executes atomic stock deduction with user limit validation
```

### RocketMQ

Message queue configuration:

```yaml
rocketmq:
  name-server: localhost:9876
  producer:
    group: seckill-producer-group
```

## Performance Tips

1. **Warm-up**: Preload stock to Redis before events start
   ```java
   seckillService.warmUpStock(skuId, stock);
   ```

2. **Monitoring**: Use Caffeine stats to monitor cache efficiency
   ```java
   cacheManager.getCache("items").getStatistics();
   ```

3. **Native Image**: Compile to native for production deployment
   ```bash
   mvn -Pnative native:compile
   ```

## Database Schema

See [schema.sql](src/main/resources/schema.sql) for complete database structure.

Key tables:
- `user` - User information
- `product_spu` - Product SPU
- `product_sku` - Product SKU with inventory
- `flash_item` - Flash sale items
- `group_session` - Group buy sessions
- `trade_order` - Trade orders

## API Documentation

### Flash Sale

```bash
# Execute flash sale
curl -X POST http://localhost:8080/api/v1/seckill/execute \
  -H "Content-Type: application/json" \
  -d '{"eventId":1,"skuId":1,"count":1,"userId":1}'
```

### Group Buy

```bash
# Initiate group
curl -X POST http://localhost:8080/api/v1/groupbuy/initiate?ruleId=1&userId=1&skuId=1

# Join group
curl -X POST http://localhost:8080/api/v1/groupbuy/join?sessionId=1&userId=2&skuId=1
```

### Admin Dashboard

```bash
# Get dashboard data
curl http://localhost:8080/api/v1/admin/dashboard
```

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

MIT License

## Author

FlashBuy Team

---

**Built with passion for high-performance e-commerce systems**
