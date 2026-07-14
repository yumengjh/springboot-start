# 第一版安全与运维闭环 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完成第一版模板尚缺的 JWT 失效闭环、审计、可信客户端 IP、运行时 IP 规则、系统信息和 OpenAPI，并用本地 SQLite 验证。

**Architecture:** 认证配置只负责密钥装配与 JWT 验证；身份与 RBAC 变更通过数据库中的用户令牌版本统一撤销旧 JWT。安全过滤器只依赖一个真实客户端 IP 解析器；运行时 IP 规则拥有独立的持久化模型与 API。审计服务接收结构化命令，统一写入脱敏事件。

**Tech Stack:** Java 21、Spring Boot 4.1、Spring Security Resource Server、Spring Data JPA、Liquibase、springdoc-openapi、JUnit 5、MockMvc。

## Global Constraints

- 不修改业务模块公开公告契约。
- Access Token 仍为 15 分钟 JWT，Refresh Token 仅保存 SHA-256 哈希。
- `.env` 和环境变量保存启动期密钥；运行时设置不得保存数据库凭证或私钥。
- 公开接口只跳过认证，继续经过 IP、接口禁用和限流过滤器。
- 新行为必须先有失败测试，再写生产代码；每段完成后执行对应测试。

### Task 1: 文档基线与 JWT 密钥/令牌版本

**Files:** `docs/PROJECT_STATUS.md`、`README.md`、`docs/api/authentication.md`、`JwtConfiguration.java`、`SecurityConfiguration.java`、JWT 验证测试。

- [x] 写失败测试：用户的 JWT `token_version` 与数据库不一致时返回 401；配置 PEM 公钥/私钥后可稳定重启验签。
- [x] 实现环境变量或文件加载 RSA 密钥；仅 `local` 在显式允许时回退到临时密钥。
- [x] 实现数据库支持的 JWT token-version 验证器，并保留标准签名/过期校验。
- [x] 更新认证/部署文档、`.env.example` 与测试。

### Task 2: 完整安全审计

**Files:** `AuditEvent.java`、Liquibase 追加变更集、`AuditService.java`、auth/users/rbac/runtime-config/announcement 服务、审计 DTO/测试。

- [x] 写失败测试：登录成功/失败、登出、会话复用、用户状态、RBAC 分配均生成包含 actor、result 的审计事件。
- [x] 扩展事件表与 DTO，保存 actor、结果和 JSON 元数据；敏感字段只记录脱敏说明，不存密码或 Token。
- [x] 将全部管理与认证安全动作接入统一审计服务。
- [x] 验证审计分页 API 和数据库迁移。

### Task 3: 可信代理与运行时 IP 规则

**Files:** `ClientIpResolver.java`、配置属性、`IpAccessFilter.java`、IP 规则实体/仓库/API/服务、Liquibase、测试和运行时文档。

- [x] 写失败测试：不可信请求伪造 `X-Forwarded-For` 不生效；可信代理链取第一个非代理客户端；过期 deny 规则不阻断。
- [x] 实现 CIDR 可信代理解析器，过滤器和限流统一使用解析后的客户端 IP。
- [x] 为 IP 规则增加 CRUD、有效期、备注和管理员权限；配置字符串白黑名单保留为兼容的全局策略。
- [x] 为规则变动写审计，并验证 IP 拒绝/放行路径。

### Task 4: 系统信息、Actuator 与 OpenAPI

**Files:** 系统信息 controller/DTO、`application*.yml`、OpenAPI 配置、各 controller 注解、README/API 文档、测试。

- [x] 写失败测试：匿名可读的系统版本信息不泄露密钥；OpenAPI JSON 包含 Bearer scheme 与公开公告接口的无认证标记。
- [x] 实现 `/api/v1/system/info`，读取 build/git 元数据，缺失时返回 `null`。
- [x] 限制 Actuator 暴露面为 health/info/metrics，并记录生产暴露边界。
- [x] 为 API 添加概要、权限、响应和错误注释，更新 Markdown 契约。

### Task 5: 全量验证与状态收口

**Files:** `docs/PROJECT_STATUS.md`、README、根目录测试页（仅在接口有变化时）。

- [x] 执行 `./mvnw test` 与 `./mvnw verify`。
- [x] 启动 SQLite Profile，使用 curl 验证健康检查、登录、公开公告、鉴权管理、token-version 失效、运行时 IP 规则。
- [x] 检查 `git diff --check`，逐项复核本计划与状态文档，记录 Docker/Testcontainers 的环境限制。
