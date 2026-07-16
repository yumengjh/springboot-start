# Progress

## 2026-07-14

- 建立剩余目标收口计划。
- 已确认 Refresh Token 重用事务回滚、Compose JWT 密钥缺失、日志/防爆破/OpenAPI/身份能力缺口。
- Refresh 重用路径已改为 `noRollbackFor = ApiException`，并有回归测试。
- 新增防爆破观察窗口配置、请求日志开关与请求结构化日志过滤器；审计开关通过惰性依赖生效，避免循环注入。
- 新增本人 Refresh Session 列表及退出全部设备端点；注册开关作为 `identity.registration.enabled` 运行时设置。
- 新增角色、权限目录 CRUD 与关联会话失效/审计。
- 已补 Maven Git 构建元数据、OpenAPI 通用错误组件、Compose JWT 密钥变量说明，以及测试页对 RBAC CRUD/本人会话的覆盖。
- 已完成 `node scripts/verify-api-test-console.mjs`、HTML 内联脚本语法检查、完整 Maven 测试（PostgreSQL/Testcontainers 因本机无 Docker 跳过）和 `git diff --check`。
- 已以 SQLite 在 `http://localhost:8080` 启动新代码并完成 HTTP 回归：健康检查、公开公告、登录、Git 系统信息、OpenAPI、会话列表、CORS POST 预检、RBAC 角色/权限 CRUD、注册开关以及“退出全部设备”后旧 Access Token 立即失效。
- 额外修复：删除未被使用的权限不再无故失效无关用户令牌；管理员/本人撤销会话会递增 token version。
- 开始整理全量 API 参考手册：以实时 OpenAPI、Controller 与 DTO 为事实来源，避免仅靠旧专题文档遗漏接口。
- 已新增 `docs/API_REFERENCE.md`：覆盖通用响应/错误/认证约定、所有业务 API、全部 18 个运行时配置键、公开公告的双视图规则、OpenAPI/Swagger 和 Actuator。
- 已用运行中的 `/v3/api-docs` 校验 31/31 个 OpenAPI 路径均出现在手册中，并校验 18/18 个运行时配置键均有说明；`git diff --check` 通过。

## 2026-07-15

- 用户确认直接实现前后端登录联动，选择 HttpOnly Cookie 方案；动态菜单暂时保留前端假数据。
- 已确认后端与前端是独立 Git 仓库，未开始修改业务代码。
- 后端认证改为 HttpOnly、SameSite=Strict 的 `refresh_token` Cookie；登录体增加 `rememberMe`，会话持久化标记随轮换保留，刷新和退出从 Cookie 读取 Token。
- 前端完成 Element Plus 注册页、登录页记住我入口、Cookie 自动刷新、401 单次刷新重试和服务端退出；Access Token 仅驻留内存。Vite `/api` 代理到本地 8080，动态菜单保持假数据。
- 修复 SQLite `addColumn` 造成 Refresh Session 索引丢失的问题，并增加 CORS credentials 支持。
- 验证：`./mvnw test` 通过（56 个测试，1 个 PostgreSQL/Docker 测试跳过）；`pnpm typecheck` 与 `pnpm build` 通过；临时 SQLite 实例的注册、Cookie 登录、刷新轮换、退出清 Cookie HTTP 冒烟通过；两个仓库均通过 `git diff --check`。
- 发现 8080 上残留 7 月 14 日启动的旧 JVM，旧 `LoginRequest` 不识别前端新增的 `rememberMe` 字段而返回 `UNKNOWN_FIELD`；已重启为当前代码。携带该字段的无效账号请求现正确返回 `401 INVALID_CREDENTIALS`，健康检查为 `UP`。
- 开始第 9 阶段：动态菜单与路由模块的设计调查。确认前端目前使用空的假路由接口，后端尚无菜单实体；动态组件映射缺失需要在新协议中显式校验并降级处理。
- 用户确认动态导航设计，并授权跳过额外文档审阅直接实施；设计已写入 `docs/superpowers/specs/2026-07-15-dynamic-navigation-rbac-design.md`。
- 已生成 `docs/superpowers/plans/2026-07-15-dynamic-navigation-rbac.md`，将按内联方式执行。
- 已完成 `navigation` 模块：Liquibase 菜单表、受控组件键校验、菜单 CRUD、JWT 权限过滤路由接口、审计事件和内置首页/菜单管理条目；新增 `system:menu:read/write`。
- 管理后台已将假动态路由替换为后端接口，采用受控组件注册表加载路由；接口失败也会结算加载状态并保留静态首页，避免侧栏永久显示加载中。已提供菜单 CRUD 页面及写权限控制。
- 新增 SQLite 迁移与导航服务回归测试；前端类型检查和生产构建均通过。
- 修复动态菜单叶子节点未显示：前端路由转换曾为页面菜单输出 `children: []`，被侧栏按“空目录”过滤；现在叶子页面省略该字段，并新增独立回归脚本验证该数据形状。
- 修复内置首页与静态首页冲突：动态接口中的 `/welcome` 曾保留 `name: home` 于侧栏，而前端路由注册会跳过该同路径条目，点击后触发 `No match for { name: home }`。动态菜单现在在渲染前剔除已注册的静态路径，并有回归验证。
- 用户确认继续集成 `api-test.html` 覆盖的正式管理能力；已完成多页面 Element Plus 集成设计，待用户审阅设计文件后进入实现计划。
- 用户已确认设计，已生成管理后台完整集成实施计划，等待选择执行方式。
- 用户要求在当前会话直接执行；已完成 `api-test.html` 所覆盖能力的 Element Plus 页面化集成：用户管理、角色与权限、运行时配置、IP 规则、审计日志、公告管理、账户安全和首页健康/系统信息概览。
- 后端导航受控组件白名单扩展为上述页面；启动器已向本地 SQLite 实际种入 `system`、`content`、`account` 目录及其内置页面，并把现有菜单管理项归入系统目录。`NavigationServiceTest` 先失败后通过，完整 `./mvnw clean test` 通过：62 个测试通过，1 个 PostgreSQL/Testcontainers 测试跳过。
- 前端新增按领域拆分的 API 客户端、当前用户权限组合式函数和五个可执行 Node 契约脚本；`node scripts/router-conversion.test.mjs`、`permission-composable`、`admin-page-contract`、`runtime-config-contract`、`announcement-contract` 与 `pnpm typecheck` 均通过。两个仓库的 `git diff --check` 均通过。
- 已重启本地后端至当前代码，`/actuator/health` 返回 `UP`；数据库中确认八个新页面菜单均已落库。由于没有读取或输出本地管理员密码，未执行带超级管理员身份的浏览器端到端点击验证。
- 已补齐 `vue-demi` 与 `tippy.js` 的直接依赖并更新锁文件；前端完整 Node 契约测试、`pnpm typecheck`、`pnpm build` 与 `git diff --check` 均成功。第 10 阶段已完成。
- 修复动态路由页切换白屏：用户管理、RBAC、IP 规则与公告页同时输出了卡片/弹窗等多个根节点，Vue `Transition` 因此无法动画并会在切换时卸载异常。已统一为单一 `.page-root` 根元素；新增 `route-page-root.test.mjs`（修复前失败、修复后通过），并重新通过 `pnpm typecheck` 与前端 `git diff --check`。
- 头像下拉菜单已增加“账户安全”“刷新会话”“退出系统”，覆盖纵向、横向和混合布局；账户安全页也提供刷新当前会话、退出当前登录和退出全部设备。刷新会话轮换 HttpOnly Refresh Cookie、更新内存 Access Token 与用户资料；任何 Token 不会显示或写入页面。`avatar-session-menu.test.mjs` 和 `pnpm typecheck` 通过。
- 修复 `/api/v1/users/me/sessions` 的 `500 INTERNAL_ERROR`（traceId `c4878547f72b43079904397999017909`）：定位到 006 迁移前的 150 条 Refresh Session 缺少 `persistent` 值，而实体字段是基本类型 `boolean`，Hibernate 读取时无法注入 `NULL`。新增 009 Liquibase 迁移归一为 `false`，本地重启时已实际更新 150 条；数据库复核空值为 0，健康检查为 `UP`，迁移回归测试通过。
- 修复 Vite 开发代理与依赖布局：重启前端服务后，`/actuator/health`、`/swagger-ui/index.html`、`/v3/api-docs` 均由 8848 正确代理至 8080，不再回退到 SPA HTML。补齐 `vue-demi`（CDN 插件）与 `tippy.js`（`vue-tippy` peer）为直接依赖，避免 pnpm 不创建根链接导致开发服务器无法重启；前端入口和 `pnpm typecheck` 均通过。

## 2026-07-16

- 完成后端运行性审查：全量 Maven 基线为 83 个测试通过、0 失败、1 个 PostgreSQL/Testcontainers 测试跳过。
- 用户确认直接实现 GC 资源/策略中心，并要求在开发阶段增加受限 SQL 调试台；第 11 阶段开始。
- 已完成 014 Liquibase 迁移：GC 策略、运行记录、锁表以及 Refresh Session 的用户/家族/到期索引。SQLite 迁移断言先失败后通过。
- 已完成 GC 初始资源和调度器：Refresh Token、审计日志、限流状态、登录失败状态均通过 `GcResource` 注册；策略独立持久化，自动任务使用数据库锁。`GcExecutionServiceTest` 先因缺类失败后通过。
- 已完成 GC 管理 API、本地 SQL 执行 API、后台 GC 可视化与 SQL 执行台；导航项按 `system:gc:read` 与 `system:sql:execute` 权限展示。SQL 控制台仅 local+显式开关可运行，多语句拒绝、写操作确认、敏感列脱敏和审计均已覆盖。
- 验证：后端 `./mvnw test` 为 85 通过、0 失败、1 跳过；管理端 `pnpm typecheck`、`pnpm build` 成功。
- GC 结果协议已改为结构化 JSON：每次任务会返回总候选数、总删除数和各资源的候选/删除/说明，管理端不再显示 `resource:a/b` 压缩字符串。GC 页面重构为运行概览、资源策略、运行记录三个视图，支持单资源预览/真实清理、按预览/真实/来源/状态筛选以及任务详情抽屉。
- 为结构化结果先补充了失败测试，再实现编码/解码；最新验证为后端 `./mvnw test` 87 通过、0 失败、1 跳过，管理端 `pnpm typecheck && pnpm build` 成功。已重启后端，`http://localhost:8080/actuator/health` 与经 8848 代理的健康检查均为 `UP`。
- 修复 GC 策略“编辑后立即预览被旧值覆盖”：前端策略改为显式未保存状态，预览/真实清理前会等待保存目标策略完成后才调用执行接口。新增独立的 `maintenance.gc.schedule.enabled`，GC 页可管理全局自动清理开关和 1–1440 分钟执行间隔；关闭自动计划不会影响手动运行。新增调度开关回归测试先失败后通过；全量后端测试为 88 通过、0 失败、1 跳过，前端类型检查和构建通过。重启后已确认新设置初始化至 SQLite。
