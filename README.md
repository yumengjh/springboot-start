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

- 注册、登录、RSA JWT、Refresh Token 轮换、当前设备登出、本人会话列表与退出全部设备
- 用户查看本人资料、修改显示名、修改密码
- 数据库 RBAC：初始角色/权限、角色与权限目录 CRUD、用户角色分配、角色权限分配
- 运行时服务治理：动态 CORS、接口禁用、通用与接口级限流、CIDR IP 白黑名单、带有效期的 IP 规则、登录防爆破、请求日志开关、注册开关及审计查询
- 统一错误响应、Trace ID、SQLite/PostgreSQL Liquibase 迁移
- Docker、Compose、GitHub Actions 基础文件

详细接口与边界见：

- [全量 API 参考手册](docs/API_REFERENCE.md)
- [认证与会话](docs/api/authentication.md)
- [RBAC 权限](docs/api/rbac.md)
- [运行时服务配置](docs/operations/runtime-settings.md)
- [公告示例模块](docs/api/announcements.md)
- [项目目标与进度](docs/PROJECT_STATUS.md)
- [部署说明](docs/operations/deployment.md)

## 接口测试页

根目录的 `api-test.html` 是浏览器测试页。先启动服务，再双击打开它；页面默认填写 `admin`（密码不预填），登录后会加载用户管理、角色/权限目录 CRUD、角色权限分组、本人会话、运行时配置、CORS、接口/IP 策略、审计查询和公告示例。可选“保存登录状态”只会把 Refresh Token 保存在当前浏览器的 localStorage；退出登录或“退出全部设备”都会清除它。共享电脑不要启用此选项。

本地从 HTML 文件发起的跨域预检支持 `GET`、`POST`、`PUT`、`PATCH`、`DELETE` 和 `OPTIONS`；因此用户状态更新、角色分配和角色权限更新都可以直接在测试页执行。生产环境应将通配来源改为明确的受信任来源。

## 主要路由

| 模块 | 路由 |
| --- | --- |
| 认证 | `/api/v1/auth/register`、`login`、`refresh`、`logout` |
| 当前用户 | `/api/v1/users/me` |
| RBAC | `/api/v1/rbac/**` |
| 运行时配置 | `/api/v1/system/runtime-config` |
| 系统审计 | `/api/v1/system/audit-events` |
| 公告示例 | `/api/v1/announcements` |
| 健康检查 | `/actuator/health` |
| OpenAPI | `/v3/api-docs`、`/swagger-ui/index.html` |

## API 响应约定

`/api/v1/**` 的成功响应（`204 No Content` 除外）默认统一为：

```json
{
  "data": {},
  "traceId": "请求追踪 ID",
  "timestamp": 1783814400000
}
```

错误响应使用 `code`、`message`、`traceId`、`violations`、`timestamp`。所有对外时间字段使用 Unix 毫秒时间戳，避免 ISO 字符串格式不一致。认证、授权、限流和 Controller 内异常共用这一错误结构；客户端排查时应携带 `traceId`。

默认情况下，所有业务接口都需要 Access Token。要开放一个外部接口，在 Controller 方法或类上标记 `@PublicApi`；它只跳过身份认证，仍然会经过 Trace ID、IP 访问控制、接口禁用和限流。公开内容需要最简响应时可使用 `@PublicApi(minimalResponse = true)`，例如公告公开列表；详情见 [公告示例模块](docs/api/announcements.md)。

请求 DTO 使用 Bean Validation，并启用严格 JSON 反序列化：未知字段会返回 `400 UNKNOWN_FIELD`，不会被静默忽略。路径中的用户 ID、角色和权限编码也会做格式校验。

## 构建与测试

```bash
./mvnw test
./mvnw verify
```

本机没有 Docker 时，PostgreSQL/Testcontainers 测试需要交给 GitHub CI；SQLite 测试可直接运行。

## 当前重要限制

- `local` Profile 可临时生成 JWT 密钥方便开发；`postgres` Profile 必须配置一对 RSA 私钥/公钥（`APP_JWT_*_PEM` 或 `APP_JWT_*_PATH`）。用户状态、密码、角色或权限变化会递增 `token_version`，旧 Access Token 会立即被拒绝。
- IP 白黑名单、限流和防爆破计数仍是单实例能力；多实例部署时应替换为 Redis 等共享实现。仅来自 `APP_SECURITY_TRUSTED_PROXIES` 的代理才会读取 `X-Forwarded-For`。
- 不包含 MySQL、SQL Server、Redis、邮件验证、OAuth、MFA 和前端。
