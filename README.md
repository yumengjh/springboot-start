# Spring Boot 后端启动模板

一个面向真实 REST API 项目的 Java 21 / Spring Boot 4.1 模板。它采用模块化单体结构，提供 SQLite 本地开发、PostgreSQL 部署、Liquibase、JWT、Refresh Token、RBAC、运行时服务配置、容器化与 CI 基础。

这不是完整 SaaS，也不包含前端；目标是以后新项目可以直接从这里开始。

## 环境要求

- JDK 21
- `./mvnw` 可自动下载 Maven；Windows 可使用 `mvnw.cmd`
- Docker 仅在 PostgreSQL、镜像构建和 Testcontainers 测试时需要

## SQLite 本地启动

默认使用 `./data/app.db`：

```bash
cp .env.example .env
# 编辑 .env，按需填写 APP_BOOTSTRAP_ADMIN_USERNAME 和 APP_BOOTSTRAP_ADMIN_PASSWORD
./mvnw spring-boot:run
```

服务默认监听 <http://localhost:8080>。检查健康状态：

```bash
curl http://localhost:8080/actuator/health
```

`.env` 会被 Spring Boot 直接加载，适合保存启动期配置；密码不会写入源码、示例文件或日志：

```bash
APP_BOOTSTRAP_ADMIN_USERNAME=admin
APP_BOOTSTRAP_ADMIN_PASSWORD=请替换为强密码
```

若用户名不存在，服务会创建该用户并授予 `SUPER_ADMIN`。该初始化是幂等的。

本地管理员已被锁定或忘记密码时，可临时设置
`APP_BOOTSTRAP_ADMIN_RESET_PASSWORD=true` 后重启服务；它会重置 `.env` 中指定账号的密码并解除锁定。恢复成功后应立即改回 `false`。

## 已实现功能

- 注册、登录、RSA JWT、Refresh Token 轮换与当前设备登出
- 用户查看本人资料、修改显示名、修改密码
- 数据库 RBAC：初始角色、权限、用户角色分配、角色权限分配
- 运行时服务配置基础：通用 API 限流、登录防爆破和审计开关的持久化内存快照
- 统一错误响应、Trace ID、SQLite/PostgreSQL Liquibase 迁移
- Docker、Compose、GitHub Actions 基础文件

详细接口与边界见：

- [认证与会话](docs/api/authentication.md)
- [RBAC 权限](docs/api/rbac.md)
- [运行时服务配置](docs/operations/runtime-settings.md)
- [项目目标与进度](docs/PROJECT_STATUS.md)
- [部署说明](docs/operations/deployment.md)

## 接口测试页

根目录的 `api-test.html` 是浏览器测试页。先启动服务，再双击打开它，可测试认证、当前用户资料、RBAC、管理员用户状态和运行时配置。受权限保护的操作需要先用超级管理员账号登录。

## 主要路由

| 模块 | 路由 |
| --- | --- |
| 认证 | `/api/v1/auth/register`、`login`、`refresh`、`logout` |
| 当前用户 | `/api/v1/users/me` |
| RBAC | `/api/v1/rbac/**` |
| 运行时配置 | `/api/v1/system/runtime-config` |
| 健康检查 | `/actuator/health` |
| OpenAPI | `/v3/api-docs`、`/swagger-ui/index.html` |

## 构建与测试

```bash
./mvnw test
./mvnw verify
```

本机没有 Docker 时，PostgreSQL/Testcontainers 测试需要交给 GitHub CI；SQLite 测试可直接运行。

## 当前重要限制

- JWT 密钥当前每次启动自动生成，因此服务重启会让旧 Access Token 失效；生产外部密钥配置待完成。
- 审计事件、IP 策略、真正限流与防爆破尚在实现。
- 不包含 MySQL、SQL Server、Redis、邮件验证、OAuth、MFA 和前端。
