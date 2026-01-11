# PinShop Server API 文档示例

> 本文档提供前端开发人员参考的Mock数据，基于 V1.6 API 规范

---

## 基础信息

- **Base URL**: `http://localhost:8080`
- **认证方式**: Bearer Token (Header: `Authorization: Bearer {token}`)
- **数据格式**: JSON
- **命名风格**: camelCase (驼峰命名)

---

## 统一响应结构

所有接口返回格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1709823456789
}
```

### 状态码说明

| Code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 业务异常 |
| 401 | 未登录或Token失效 |
| 403 | 无权限访问 |
| 500 | 系统错误 |
| 4001 | 库存不足 |
| 4002 | 未登录 |
| 4003 | 重复购买 |

---

## 接口列表

### 1. 获取秒杀商品列表

**接口**: `GET /api/v1/product/flash-list`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| timeSlot | String | 否 | 时间段（如"08:00"、"12:00"、"20:00"） |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "skuId": 1001,
      "title": "iPhone 15 Pro Max 256GB",
      "imgUrl": "https://cdn.pinshop.com/products/1001.jpg",
      "flashPrice": 5999.00,
      "originalPrice": 7999.00,
      "stockPercent": 85,
      "status": 1
    },
    {
      "skuId": 1002,
      "title": "Tesla Model Y 玩具车 1:18",
      "imgUrl": "https://cdn.pinshop.com/products/1002.jpg",
      "flashPrice": 99.00,
      "originalPrice": 199.00,
      "stockPercent": 12,
      "status": 1
    }
  ],
  "timestamp": 1709823456789
}
```

### 2. 执行秒杀下单

**接口**: `POST /api/v1/trade/seckill`

**请求Body**:

```json
{
  "eventId": 1,
  "skuId": 1001,
  "count": 1,
  "userId": 10001
}
```

**响应示例 - 排队中**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": 0,
    "message": "Order processing, please wait",
    "orderId": null,
    "queueId": "Q-1709823456789",
    "timestamp": 1709823456789
  },
  "timestamp": 1709823456789
}
```

**响应示例 - 成功**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": 1,
    "message": "Order created successfully",
    "orderId": 12345,
    "queueId": null,
    "timestamp": 1709823456789
  },
  "timestamp": 1709823456789
}
```

**响应示例 - 失败**:

```json
{
  "code": 4001,
  "message": "Insufficient stock",
  "data": {
    "status": -1,
    "message": "Insufficient stock",
    "orderId": null,
    "queueId": null,
    "timestamp": 1709823456789
  },
  "timestamp": 1709823456789
}
```

### 3. 查询秒杀结果

**接口**: `GET /api/v1/trade/result/{queueId}`

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| queueId | String | 排队凭证ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": 0,
    "message": "QUEUING",
    "orderId": null,
    "queueId": "Q-1709823456789",
    "timestamp": 1709823457000
  },
  "timestamp": 1709823457000
}
```

### 4. 获取正在拼的团

**接口**: `GET /api/v1/group/{skuId}/sessions`

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| skuId | Long | 商品SKU ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "sessionId": 5001,
      "userAvatar": "https://api.dicebear.com/7.x/avataaars/svg?seed=10001",
      "userName": "用户1",
      "missingNum": 1,
      "timeLeft": 3600000
    },
    {
      "sessionId": 5002,
      "userAvatar": "https://api.dicebear.com/7.x/avataaars/svg?seed=10002",
      "userName": "用户2",
      "missingNum": 2,
      "timeLeft": 7200000
    }
  ],
  "timestamp": 1709823456789
}
```

### 5. 发起拼团

**接口**: `POST /api/v1/group/initiate`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ruleId | Long | 是 | 拼团规则ID |
| userId | Long | 是 | 用户ID |
| skuId | Long | 是 | 商品SKU ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": 5001,
  "timestamp": 1709823456789
}
```

### 6. 参与拼团

**接口**: `POST /api/v1/group/join`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sessionId | Long | 是 | 拼团会话ID |
| userId | Long | 是 | 用户ID |
| skuId | Long | 是 | 商品SKU ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1709823456789
}
```

---

## 前端开发建议

### 1. 轮询策略

秒杀下单后，前端应使用轮询查询结果：

```javascript
async function pollSeckillResult(queueId, maxRetries = 10) {
  for (let i = 0; i < maxRetries; i++) {
    const response = await fetch(`/api/v1/trade/result/${queueId}`);
    const result = await response.json();

    if (result.data.status === 'SUCCESS') {
      return result.data;
    } else if (result.data.status === 'FAIL') {
      throw new Error(result.data.message);
    }

    // Wait 500ms - 1000ms before next poll
    await new Promise(resolve => setTimeout(resolve, 500));
  }

  throw new Error('Timeout waiting for seckill result');
}
```

### 2. 倒计时显示

拼团剩余时间（毫秒）需转换为倒计时组件：

```javascript
function formatTimeLeft(timeLeft) {
  const hours = Math.floor(timeLeft / 3600000);
  const minutes = Math.floor((timeLeft % 3600000) / 60000);
  const seconds = Math.floor((timeLeft % 60000) / 1000);

  return `${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
}
```

### 3. 错误处理

统一错误处理：

```javascript
function handleApiError(error) {
  if (error.code === 401) {
    // 跳转登录页
    window.location.href = '/login';
  } else if (error.code === 4001) {
    // 库存不足
    showToast('商品已抢光');
  } else {
    // 其他错误
    showToast(error.message || '系统错误，请稍后重试');
  }
}
```

---

## Postman Collection

完整的Postman Collection可在项目根目录生成：

```bash
mvn smart-doc:postman
```

生成的文件：`target/postman/postman.json`
