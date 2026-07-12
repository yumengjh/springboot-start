# API 契约与横切层收口设计

## 目标

让 Spring Boot 模板拥有清晰、可追踪的请求链路：Filter 处理原始 HTTP 横切问题，Security 处理认证与授权，Controller 只表达路由契约，Service 处理业务，Advice 处理 MVC 异常，ResponseBodyAdvice 统一成功响应。

## 响应契约

成功且有响应体的 `/api/v1/**` 接口统一为：

```json
{
  "data": {},
  "traceId": "...",
  "timestamp": "..."
}
```

`204 No Content` 保持空响应体。错误继续使用既有 `ApiError`，包含 `code`、`message`、`traceId`、`violations` 和 `timestamp`。Actuator、OpenAPI、Swagger 不参与成功包装。

## 横切职责

- `TraceIdFilter`：生成或接收合法 traceId，写入响应头和 MDC。
- `RateLimitFilter`：仅对实际 API 请求执行，跳过 CORS `OPTIONS`；命中时通过统一错误写入器返回 429。
- `SecurityApiErrorHandler`：认证失败 401 与权限拒绝 403 使用统一错误写入器。
- `ApiExceptionHandler`：处理 Controller/MVC 范围的预期错误；未知异常记录含 traceId 的服务端堆栈，客户端仅接收安全的 500 响应。
- `ApiResponseAdvice`：等价于 NestJS 的全局 interceptor，包装 API 成功响应。

## HTTP 语义

- 注册继续返回 `201 Created`，但删除指向不存在资源的 `Location` 头。
- 角色与权限关联的 PUT/DELETE、登出和改密继续返回 204。
- 校验、JSON 格式、路径参数类型、缺少参数、405、415 和 500 都映射为稳定的错误码与合理状态码。

## 代码组织

- `modules/*/api` 只放 Controller 与 DTO。
- `RateLimitFilter` 移至 `modules/security/web`。
- 删除误入源码的 `.DS_Store`，并通过 `.gitignore` 防止再次提交。

## 验收

所有成功 API（204 除外）包含统一 data/traceId/timestamp；所有应用层错误包含标准错误字段；安全与限流错误也满足相同错误结构；OPTIONS 不计入限流；注册响应不再提供不存在的 Location。
