# PinShop Server 配置说明

## 🔐 敏感信息配置

本项目已将敏感配置信息从代码仓库中分离，确保账号密码安全。

## 配置方式（3选1）

### 方式1: 使用配置文件（推荐本地开发）

1. 复制配置模板：
```bash
cp src/main/resources/application-example.yml src/main/resources/application-local.yml
```

2. 编辑 `application-local.yml`，填入真实配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://your-host:3306/pinshop?...
    username: your_username
    password: your_password

  data:
    redis:
      host: your_redis_host
      password: your_redis_password
```

3. 启动应用：
```bash
java -jar target/pinshop-server-1.0.0.jar
```

### 方式2: 使用环境变量（推荐生产环境）

```bash
export DB_URL=jdbc:mysql://your-host:3306/pinshop?...
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
export REDIS_HOST=your_redis_host
export REDIS_PASSWORD=your_redis_password
export SPRING_PROFILE=prod

java -jar target/pinshop-server-1.0.0.jar
```

或者在启动时直接指定：
```bash
DB_URL=jdbc:mysql://... \
DB_USERNAME=your_username \
DB_PASSWORD=your_password \
REDIS_HOST=your_redis_host \
REDIS_PASSWORD=your_password \
java -jar target/pinshop-server-1.0.0.jar
```

### 方式3: Docker环境变量（推荐容器部署）

```bash
docker run -d \
  -e DB_URL=jdbc:mysql://... \
  -e DB_USERNAME=your_username \
  -e DB_PASSWORD=your_password \
  -e REDIS_HOST=your_redis_host \
  -e REDIS_PASSWORD=your_redis_password \
  -p 8080:8080 \
  pinshop-server:latest
```

## 配置优先级

Spring Boot 会按以下优先级加载配置：

1. **环境变量**（最高优先级）
2. `application-local.yml`（需存在）
3. `application.yml`（默认值）

## 环境变量列表

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DB_URL` | 数据库连接URL | jdbc:mysql://localhost:3306/pinshop |
| `DB_USERNAME` | 数据库用户名 | root |
| `DB_PASSWORD` | 数据库密码 | (空) |
| `REDIS_HOST` | Redis主机 | localhost |
| `REDIS_PORT` | Redis端口 | 6379 |
| `REDIS_PASSWORD` | Redis密码 | (空) |
| `SPRING_PROFILE` | Spring Profile | local |

## 安全注意事项

⚠️ **重要**：
- `application-local.yml` 已在 `.gitignore` 中，不会被提交到Git
- 生产环境配置 `application-prod.yml` 也在 `.gitignore` 中
- 切勿将包含真实密码的配置文件提交到代码仓库
- 生产环境建议使用环境变量或密钥管理服务（如AWS Secrets Manager）

## 清除Git历史中的敏感信息

如果你的Git历史中已经包含过敏感信息，可以使用以下命令清除：

```bash
# 备份当前分支
git branch backup-branch

# 使用 git filter-repo 清除敏感文件（需先安装 git-filter-repo）
pip install git-filter-repo
git filter-repo --path src/main/resources/application-local.yml --invert-paths

# 强制推送（谨慎使用）
git push origin main --force
```

或者使用 BFG Repo-Cleaner：
```bash
# 下载 BFG: https://rtyley.github.io/bfg-repo-cleaner/
java -jar bfg.jar --delete-files application-local.yml
git reflog expire --expire=now --all
git gc --prune=now --aggressive
git push origin main --force
```

## Docker Compose 示例

创建 `.env` 文件（也在 .gitignore 中）：

```env
DB_URL=jdbc:mysql://db:3306/pinshop?...
DB_USERNAME=your_username
DB_PASSWORD=your_password
REDIS_HOST=redis
REDIS_PASSWORD=your_redis_password
```

然后在 `docker-compose.yml` 中使用：

```yaml
version: '3.8'
services:
  app:
    image: pinshop-server:latest
    env_file:
      - .env
    ports:
      - "8080:8080"
```

## 验证配置

启动应用后，检查日志确认配置加载：

```bash
tail -f logs/application.log | grep -E "Database|Redis"
```

或使用健康检查接口：

```bash
curl http://localhost:8080/actuator/health
```
