# 运行时服务配置

运行时配置只管理后端服务治理，不管理业务开关。数据库连接、JWT 密钥、可信代理边界和初始管理员密码属于启动配置，修改后必须重启。CORS 来源和允许方法属于本模块的热更新策略。

## 已实现接口

- `GET /api/v1/system/runtime-config`：读取当前配置。
- `PUT /api/v1/system/runtime-config/{key}`：更新配置，请求体为 `{"value":"..."}`。
- `GET /api/v1/system/audit-events?page=0&size=20`：分页读取系统审计事件。

读取接口需要 `system:config:read`，更新接口需要 `system:config:write`；审计查询需要 `system:audit:read`。运行时配置更新会立即记入审计事件。

## 内置键

- `security.rate-limit.enabled`
- `security.rate-limit.capacity`
- `security.rate-limit.window-seconds`
- `security.endpoint.rate-limit.patterns`
- `security.endpoint.rate-limit.capacity`
- `security.endpoint.rate-limit.window-seconds`
- `security.brute-force.enabled`
- `security.brute-force.failure-threshold`
- `security.brute-force.lock-seconds`
- `security.audit.enabled`
- `security.cors.allowed-origins`
- `security.cors.allowed-methods`
- `security.endpoint.disabled-patterns`
- `security.ip.allow-list`
- `security.ip.deny-list`

服务启动时会为缺失项写入默认值，并将配置缓存为内存快照。更新成功后立即刷新快照；通用 API 限流按 IP 与路径在单实例内计数，超过阈值返回 `429 RATE_LIMITED`。可通过接口级模式配置更严格、独立的容量与窗口，例如只限制 `/api/v1/auth/login`。

字符串配置用于 CORS、Ant 路径模式和 IP 策略。IP 白名单非空时仅匹配的来源可访问；黑名单优先级更高。两者支持逗号分隔的 IPv4、IPv6 或 CIDR，例如 `192.168.10.0/24`。运行时配置接口不会被接口禁用策略或 IP 策略锁死。

登录防爆破已实现：在观察窗口内达到失败阈值会锁定账户，锁定时长由 `security.brute-force.lock-seconds` 控制；锁定会撤销该用户 Refresh Token，到期后下次登录自动恢复。限流计数和防爆破状态目前是单实例内存状态；多实例部署时应以 Redis 等共享存储替换，不能把当前实现当作分布式限流。

为避免唯一管理入口被误锁，`SUPER_ADMIN` 不会被自动防爆破策略锁定；普通管理员和用户仍受该策略约束。
