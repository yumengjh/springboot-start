# 运行时服务配置

运行时配置只管理后端服务治理，不管理业务开关。数据库、JWT 密钥、CORS 来源、可信代理和初始管理员密码都属于启动配置，修改后必须重启。

## 已实现接口

- `GET /api/v1/system/runtime-config`：读取当前配置。
- `PUT /api/v1/system/runtime-config/{key}`：更新配置，请求体为 `{"value":"..."}`。

当前接口要求已登录。RBAC 权限控制、修改审计记录和操作日志将在 security 模块中继续接入。

## 内置键

- `security.rate-limit.enabled`
- `security.rate-limit.capacity`
- `security.rate-limit.window-seconds`
- `security.brute-force.enabled`
- `security.brute-force.failure-threshold`
- `security.brute-force.lock-seconds`
- `security.audit.enabled`

服务启动时会为缺失项写入默认值，并将配置缓存为内存快照。更新成功后会立即刷新快照；限流和防爆破策略后续直接读取该快照。

当前仅支持布尔值和有范围限制的整数值。Redis 分布式计数、IP 黑白名单、真正限流、防爆破执行和审计事件属于下一阶段。
