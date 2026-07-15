# Findings

- `AuthenticationService.refresh` 在默认事务中调用 `TokenService.rotate`；后者重用令牌时抛出 `ApiException`，会回滚家族撤销与审计。
- `compose.yaml` 现已显式转发 PEM/文件路径两种 JWT 密钥变量；Compose 不会猜测或挂载宿主机密钥路径，文件路径方案需部署方显式提供只读 secret volume。
- 请求结构化日志、`security.request-log.enabled`、防爆破观察窗口和 identity 注册开关已通过运行时设置实现。
- Maven 的 `git-commit-id-maven-plugin` 由 Spring Boot 4.1 的依赖管理提供，构建会生成受控的 `git.properties`；OpenAPI 已补通用错误组件。
- 角色/权限 CRUD、会话列表和本人退出全部设备已实现，状态文档不再将其列为非目标。
- `springboot-admin` 是独立 Vue 3/Element Plus 项目，当前认证仍指向模板假接口；其 Vite 开发端口为 8848，动态菜单接口可在本次保持假实现。
- 后端已有登录、注册、Token 轮换和退出能力，但 Refresh Token 当前出现在 JSON 响应与请求体；本次改为 HttpOnly Cookie，前端仅在内存中保留 Access Token。
- SQLite 中 Liquibase 的 `addColumn` 会重建 `refresh_sessions` 并丢失二级索引；新增 007 变更集恢复唯一 `token_hash` 索引和查询索引。
- 当前前端 `getAsyncRoutes()` 被临时实现为返回空数组；路由转换器则直接将后端 `component` 字符串映射到 `src/views`。缺少映射时会产生 `undefined` 组件，是“加载中”状态的高风险来源。
- 后端 RBAC 已有角色、权限、用户角色和角色权限 CRUD，但没有菜单/路由实体、菜单读取接口或菜单管理 API；菜单功能应成为独立模块并仅引用现有权限编码。
