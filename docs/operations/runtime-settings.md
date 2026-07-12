# 运行时服务配置

运行时配置只管理后端服务治理，不管理业务开关。数据库、JWT 密钥、CORS 来源、可信代理和初始管理员密码都属于启动配置，修改后必须重启。

## 已实现接口

- `GET /api/v1/system/runtime-config`：读取当前配置。
- `PUT /api/v1/system/runtime-config/{key}`：更新配置，请求体为 `{"value":"..."}`。

读取接口需要 `system:config:read`，更新接口需要 `system:config:write`。修改审计记录和操作日志将在 security 模块中继续接入。

## 内置键

- `security.rate-limit.enabled`
- `security.rate-limit.capacity`
- `security.rate-limit.window-seconds`
- `security.brute-force.enabled`
- `security.brute-force.failure-threshold`
- `security.brute-force.lock-seconds`
- `security.audit.enabled`

服务启动时会为缺失项写入默认值，并将配置缓存为内存快照。更新成功后会立即刷新快照；通用 API 限流已直接读取该快照，并按 IP 与路径在单实例内计数，超过阈值返回 `429 RATE_LIMITED`。

当前仅支持布尔值和有范围限制的整数值。登录防爆破已实现：在 15 分钟观察窗口内达到失败阈值会锁定账户，锁定时长由 `security.brute-force.lock-seconds` 控制；锁定会撤销该用户 Refresh Token，到期后下次登录自动恢复。IP 黑白名单、审计事件和 Redis 分布式计数仍待实现。
