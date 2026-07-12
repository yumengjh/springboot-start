# API 契约与横切层收口 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 统一 `/api/v1/**` 的成功和错误契约，并明确 Filter、Security、Advice 与业务模块的边界。

**Architecture:** `ApiResponseAdvice` 仅包装 API 成功实体；`ApiErrorWriter` 被 MVC Advice、Security Handler 和限流 Filter 共享，保证错误结构一致。Controller 保持业务 DTO 返回类型，横切层负责协议一致性。

**Tech Stack:** Java 21、Spring Boot 4.1、Spring MVC、Spring Security、JUnit 5、MockMvc。

## Global Constraints

- 仅包装 `/api/v1/**` 的有响应体成功请求；204 维持 HTTP 空响应。
- 错误响应不得泄露异常内部信息，必须携带 traceId。
- 不新增业务功能，不改变现有 RBAC 权限模型。
- 代码变更需有失败后转绿的回归测试，并更新 README/API 文档。

---

### Task 1: 统一成功响应契约

**Files:**
- Create: `src/main/java/com/yumg/starter/common/api/ApiResponse.java`
- Create: `src/main/java/com/yumg/starter/common/api/ApiResponseAdvice.java`
- Create: `src/test/java/com/yumg/starter/common/api/ApiResponseAdviceTest.java`

- [ ] 写失败测试，断言 `GET /api/v1/...` 的 JSON 有 `data`、`traceId`、`timestamp`，且原 DTO 在 `data` 中。
- [ ] 运行 `./mvnw -Dtest=ApiResponseAdviceTest test`，确认缺少 advice 时失败。
- [ ] 添加 `record ApiResponse<T>(T data, String traceId, Instant timestamp)` 与 `ResponseBodyAdvice<Object>`，排除 `ApiError`、`ResponseEntity` 的空 body、Actuator 和 OpenAPI。
- [ ] 重跑同一测试并确认通过。

### Task 2: 收口错误写入与 MVC 错误映射

**Files:**
- Create: `src/main/java/com/yumg/starter/common/api/ApiErrorWriter.java`
- Modify: `src/main/java/com/yumg/starter/common/api/SecurityApiErrorHandler.java`
- Modify: `src/main/java/com/yumg/starter/common/api/ApiExceptionHandler.java`
- Modify: `src/main/java/com/yumg/starter/common/api/ApiErrorCode.java`
- Create: `src/test/java/com/yumg/starter/common/api/ApiExceptionHandlerContractTest.java`

- [ ] 写失败测试，断言限流、安全拒绝、路径类型错误和未知异常返回统一 `ApiError` 字段与正确状态。
- [ ] 运行指定测试，确认现有限流响应缺少 traceId/时间戳或类型错误映射为 500。
- [ ] 让 `ApiErrorWriter.write(HttpServletResponse, ApiErrorCode)` 负责序列化；Security Handler 与限流 Filter 调用它；Advice 显式处理类型转换、缺参数和不支持方法。
- [ ] 未知异常使用 `log.error` 记录完整堆栈和 traceId，客户端仅接收 `INTERNAL_ERROR`。
- [ ] 重跑契约测试并确认通过。

### Task 3: 整理 Filter 和 HTTP 语义

**Files:**
- Move: `src/main/java/com/yumg/starter/modules/security/api/RateLimitFilter.java` to `src/main/java/com/yumg/starter/modules/security/web/RateLimitFilter.java`
- Modify: `src/main/java/com/yumg/starter/config/SecurityConfiguration.java`
- Modify: `src/main/java/com/yumg/starter/modules/auth/api/AuthController.java`
- Modify: `src/test/java/com/yumg/starter/config/SecurityConfigurationCorsTest.java`
- Create: `src/test/java/com/yumg/starter/modules/security/web/RateLimitFilterTest.java`

- [ ] 写失败测试，断言 OPTIONS 不被限流；注册 201 不返回不存在的 `/api/v1/users/{id}` Location。
- [ ] 运行指定测试，确认当前行为不满足断言。
- [ ] Filter 移入 `security.web`，跳过 OPTIONS，并使用统一错误写入器；配置更新 import。
- [ ] 注册保留 201 和响应体，删除硬编码 Location。
- [ ] 重跑测试并确认通过。

### Task 4: 清理与文档

**Files:**
- Modify: `README.md`
- Modify: `docs/api/authentication.md`
- Modify: `docs/api/rbac.md`
- Modify: `.gitignore`
- Delete: `src/main/java/com/yumg/starter/.DS_Store`
- Delete: `src/main/java/com/yumg/starter/modules/.DS_Store`
- Delete: `src/main/java/com/yumg/starter/modules/auth/.DS_Store`

- [ ] 记录成功、错误和 204 响应契约；记录注册不再返回 Location。
- [ ] 忽略并删除已跟踪的 `.DS_Store`。
- [ ] 运行 `./mvnw test`、`git diff --check` 和 HTTP smoke test；提交本地变更但不推送。
