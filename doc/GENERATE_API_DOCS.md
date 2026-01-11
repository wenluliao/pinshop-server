# 生成API文档指南

## 方法一：使用Maven生成（推荐）

### 1. 安装Maven（如果未安装）

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install maven

# macOS
brew install maven

# 验证安装
mvn -version
```

### 2. 生成API文档

```bash
# 进入项目目录
cd /home/lwl/project/pinshop-server

# 编译并生成HTML文档
mvn clean compile

# 生成OpenAPI 3.0 JSON文档
mvn smart-doc:openapi

# 生成Postman Collection
mvn smart-doc:postman
```

### 3. 查看生成的文档

文档生成位置：
- **HTML文档**: `target/smart-doc/html/index.html`
- **OpenAPI JSON**: `target/smart-doc/openapi.json`
- **Postman Collection**: `target/postman/postman.json`

打开HTML文档：
```bash
# Linux
xdg-open target/smart-doc/html/index.html

# macOS
open target/smart-doc/html/index.html
```

---

## 方法二：使用Docker生成（无需安装Maven）

```bash
# 使用Docker运行Maven
docker run --rm -v "$(pwd)":/app -w /app \
  maven:3.9-eclipse-temurin-17 \
  mvn clean compile smart-doc:openapi smart-doc:postman
```

---

## 方法三：在线预览（临时方案）

在本地启动项目后，访问：
- **API列表**: http://localhost:8080/api/v1/product/flash-list
- **Postman测试**: 导入 `doc/API_EXAMPLES.md` 中的示例

---

## Smart-Doc配置说明

配置文件位置：`src/main/resources/smart-doc.json`

主要配置项：
```json
{
  "serverUrl": "http://localhost:8080",      // API服务器地址
  "packageFilters": "com.flashbuy.api.*",    // 扫描的包路径
  "allInOne": true,                         // 合并为一个文档
  "postmanEnv": {                           // Postman环境配置
    "name": "PinShop开发环境",
    "host": "localhost",
    "port": 8080
  }
}
```

---

## 常见问题

### 1. 找不到smart-doc-maven-plugin
**解决方案**: 确保pom.xml中已正确配置插件（已配置）

### 2. 编译失败
**解决方案**: 检查Java版本，需要JDK 25
```bash
java -version  # 应该显示 25.x.x
```

### 3. 文档内容不完整
**解决方案**: 确保Controller和DTO有完整的Javadoc注释（已完成）

---

## 下次更新文档

当代码更新后，只需重新运行：
```bash
mvn clean compile smart-doc:openapi smart-doc:postman
```

文档会自动更新！
