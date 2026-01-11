这是一个非常好的切入点。在 Java 开发中，传统的 Swagger/SpringDoc 往往需要在 Controller 上加大量的注解（如 `@Operation`, `@Schema`），这被称为**“代码侵入”**，不仅让代码变得臃肿，还会增加 GraalVM Native Image 的反射配置负担。

针对你的**高性能、Java 25、无侵入**需求，我强烈推荐使用 **Smart-Doc**。

### 为什么选择 Smart-Doc？
1.  **零注解侵入**：它直接读取 Java 源代码中的标准注释（Javadoc）和类型定义（包括 Java Records）来生成文档。
2.  **支持 Native Image**：因为它是在编译期（Maven 插件阶段）分析源码生成文档，不会带入运行时依赖，不影响最终二进制文件的大小和性能。
3.  **产物丰富**：可以直接生成 OpenAPI 3.0 (JSON), Postman Collection, Markdown, 甚至直接推送到 YApi/Torna 等接口管理平台。

---

以下是更新后的 V1.6 版本文档，重点补充了**前后端交互契约**和**API 治理方案**。

# 📘 FlashGroupBuy 全栈架构文档 (Project V1.6) - API 治理与协作篇

## 1. 核心变更：API 治理架构 (API Governance)

为了实现前后端高效并行开发，且保持后端代码的“纯洁性”，我们采用 **Smart-Doc + YApi/Postman** 的组合方案。

### 1.1 技术选型
*   **文档生成**: **Smart-Doc Maven Plugin**
    *   *原理*: 源码分析 (Source Code Analysis)。
    *   *优势*: 完全利用 Javadoc，无需 `@Swagger` 注解。
*   **接口管理**: **YApi** (推荐) 或 **Postman**。
    *   后端构建时自动推送文档到平台，前端在平台查看并使用 Mock 数据。
*   **交互协议**: RESTful over HTTP/2。
    *   **数据格式**: JSON (Jackson).
    *   **命名风格**: 驼峰命名法 (camelCase) —— *Java Record 默认风格，减少转换开销*。

### 1.2 Maven 配置 (pom.xml)
请在 backend 项目中添加此插件配置，告诉 AI 这是生成文档的标准方式。

```xml
<plugin>
    <groupId>com.github.shalousun</groupId>
    <artifactId>smart-doc-maven-plugin</artifactId>
    <version>3.0.3</version>
    <configuration>
        <!-- 指定生成文档的配置文件 -->
        <configFile>./src/main/resources/smart-doc.json</configFile>
        <!-- 排除不需要生成文档的依赖库 -->
        <excludes>
            <exclude>org.springframework.boot:spring-boot-starter-web</exclude>
        </excludes>
    </configuration>
</plugin>
```

---

## 2. 无侵入编码规范 (Coding Standard)

为了让 Smart-Doc 生成完美的文档，后端代码（Controller 和 DTO）只需要写标准的注释。

### 2.1 Controller 写法示例
注意：完全没有 Swagger 注解，只有标准的 Javadoc。

```java
/**
 * 交易中心-秒杀服务
 * @author Claude
 */
@RestController
@RequestMapping("/api/v1/trade")
public class SeckillController {

    /**
     * 执行秒杀下单
     * 
     * <p>
     * 核心高并发接口。前端需轮询查询结果。
     * </p>
     *
     * @param request 秒杀请求参数
     * @return 包含订单ID或排队状态
     * @apiNote 1. 必须先登录; 2. 接口含防刷限流;
     */
    @PostMapping("/seckill")
    public Result<SeckillResponse> doSeckill(@RequestBody SeckillRequest request) {
        // 业务逻辑...
        return Result.success(new SeckillResponse("TD123456", "QUEUING"));
    }
}
```

### 2.2 DTO (Java Record) 写法示例
Smart-Doc 完美支持 Java Record 的字段注释提取。

```java
/**
 * 秒杀请求对象
 * @param skuId 商品SKU编号 (必填)
 * @param quantity 购买数量 (单次限购1件)
 * @param addressId 收货地址ID
 */
public record SeckillRequest(
    Long skuId, 
    Integer quantity, 
    Long addressId
) {}
```

---

## 3. 前后端交互契约 (API Contract)

前端开发人员（或 AI）可以直接依据以下标准进行 UI 逻辑开发，无需等待后端部署。

### 3.1 统一响应结构 (Response Wrapper)
后端无论成功失败，HTTP 状态码建议统一为 200 (除了 401/403)，业务状态码通过 body 判断。

```json
{
  "code": 200,          // 200:成功, 500:系统错误, 4001:库存不足, 4002:未登录
  "message": "success", // 错误提示文案
  "data": { ... },      // 业务数据
  "timestamp": 1709823456789
}
```

### 3.2 认证鉴权 (Auth)
*   **Header**: 所有非公开接口，前端必须在 Header 中携带 Token。
    *   Key: `Authorization`
    *   Value: `Bearer eyJhbGciOiJIUzI1Ni...` (JWT Token)
*   **失效处理**: 前端拦截器检测到 `code: 401` 时，自动跳转登录页或静默刷新 Token。

---

## 4. 核心接口定义 (V1.6 Updated)

以下是生成的接口文档摘要，前端可据此 Mock 数据。

### 4.1 首页与商品
**GET /api/v1/product/flash-list** (获取秒杀商品流)
*   **Query**: `timeSlot` (08:00)
*   **Response**:
    ```json
    {
      "code": 200,
      "data": [
        {
          "skuId": 1001,
          "title": "iPhone 15 Pro",
          "imgUrl": "https://cdn.../1.jpg",
          "flashPrice": 5999.00,
          "originalPrice": 7999.00,
          "stockPercent": 85,  // 剩余库存百分比，用于前端进度条
          "status": 1          // 1:抢购中 2:已抢光
        }
      ]
    }
    ```

### 4.2 交易核心 (秒杀)
**POST /api/v1/trade/seckill** (秒杀下单)
*   **Body**: `{ "skuId": 1001, "count": 1 }`
*   **Response**:
    ```json
    {
      "code": 200,
      "data": {
        "orderId": null,
        "queueId": "Q-998877", // 排队凭证
        "status": "QUEUING"    // QUEUING, SUCCESS, FAIL
      }
    }
    ```

**GET /api/v1/trade/result/{queueId}** (轮询结果)
*   **Response (处理中)**: `{ "code": 200, "data": { "status": "QUEUING" } }`
*   **Response (成功)**: 
    ```json
    { 
      "code": 200, 
      "data": { 
        "status": "SUCCESS", 
        "orderId": "TD20240101...", 
        "payAmount": 5999.00 
      } 
    }
    ```

### 4.3 拼团核心
**GET /api/v1/group/{skuId}/sessions** (获取当前正在拼的团)
*   **Response**:
    ```json
    {
      "code": 200,
      "data": [
        {
          "sessionId": 5001,
          "userAvatar": "https://...",
          "userName": "张***",
          "missingNum": 1,        // 还差1人
          "timeLeft": 3600000     // 剩余毫秒数
        }
      ]
    }
    ```

---

## 5. 前端技术栈推荐 (Frontend Stack)

既然后端用了最先进的 Java 25，前端建议也使用现代化、轻量化的技术栈，兼顾开发效率和性能。

### 5.1 小程序端 (C端)
*   **框架**: **Uni-app (Vue 3 + Vite 版本)**
    *   *理由*: 跨端能力强（可编译为微信小程序、H5、App），Vue 3 的 Composition API 更利于逻辑复用。
*   **UI 库**: **Uview Plus** 或 **Wot Design Uni**。
    *   *理由*: 针对电商场景组件齐全（倒计时、SKU 选择器、侧滑购物车）。
*   **状态管理**: **Pinia** (比 Vuex 更轻量)。

### 5.2 管理后台 (B端)
*   **框架**: **React 18** 或 **Vue 3 Admin**.
*   **UI 库**: **Arco Design (ByteDance)** 或 **Ant Design 5**.
*   **数据请求**: **TanStack Query (React Query)**。
    *   *理由*: 自动管理服务端状态、缓存、轮询，完美契合后台的报表和实时数据需求。

---

## 6. 给 AI 的指令 (Prompt Update)

如果你要让 Claude/Cursor 生成代码，请追加这段关于 API 的指令：

> "在编写 Controller 和 DTO 时，请**不要**使用 `@Swagger` 或 `@Operation` 等注解。
> 请严格遵守 **Javadoc 标准注释**规范，详细描述接口的功能、参数和返回值，因为我们将使用 **Smart-Doc** 插件来自动生成文档。
> 所有 HTTP 响应必须包装在 `Result<T>` 泛型类中。时间字段请统一使用时间戳（Long）或 ISO-8601 字符串，以便前端处理。"

这样，你就拥有了一个**文档即代码**、**前后端解耦**且**极致轻量**的 V1.6 版本架构。前端可以立刻拿着这份文档（或生成的 Postman JSON）去写 UI，后端则专注于高性能实现。