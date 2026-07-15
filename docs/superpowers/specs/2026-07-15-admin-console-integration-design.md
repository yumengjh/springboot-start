# 管理后台完整集成设计

## 目标

将 `api-test.html` 覆盖的已实现 HTTP 能力迁移到 `springboot-admin`，作为使用 Vue 3 与 Element Plus 的正式后台功能。页面复用现有 HttpOnly Refresh Cookie、Access Token、统一 API 信封、动态路由和 RBAC；不再在正式后台暴露调试台式的原始 Token、手工基础地址或响应 JSON 面板。

## 边界

- 覆盖用户管理、RBAC、运行时配置、IP 规则、审计、公告、个人安全中心及系统概览。
- 登录、注册、刷新和退出保持既有页面与会话方案，不再复制 `api-test.html` 的 Token 输入输出。
- 仅使用 Element Plus 表格、分页、表单、抽屉、对话框、标签、通知与确认框；不嵌入 `api-test.html`。
- 不新建业务 API。仅扩展导航组件键和初始化菜单，使已有后端 API 可被访问。
- `/welcome` 保持静态首页，动态菜单不得再次下发或渲染同路径的首页条目。

## 页面、API 与授权

| 页面 | 路径 / 组件键 | 使用 API | 页面权限 | 写操作权限 |
| --- | --- | --- | --- | --- |
| 系统概览 | `/welcome` / 现有 `welcome` | `GET /actuator/health`、`GET /api/v1/system/info`、Swagger/OpenAPI 链接 | 已登录 | 无 |
| 用户管理 | `/system/users` / `user-management` | 管理员用户列表、详情、状态、会话撤销、用户角色分配 | `system:user:read` | `system:user:write`、`system:role:assign` |
| 权限中心 | `/system/rbac` / `rbac-management` | 角色、权限 CRUD，角色权限授予/撤销 | `system:role:read` | `system:role:write` |
| 运行时配置 | `/system/config` / `runtime-config` | 运行时配置读取与更新 | `system:config:read` | `system:config:write` |
| IP 规则 | `/system/ip-rules` / `ip-rule-management` | 持久化 IP 规则列表、创建、删除 | `system:config:read` | `system:config:write` |
| 菜单管理 | `/system/menu` / `menu-management` | 动态菜单 CRUD | `system:menu:read` | `system:menu:write` |
| 审计日志 | `/system/audit` / `audit-log` | 分页审计事件 | `system:audit:read` | 无 |
| 公告管理 | `/content/announcements` / `announcement-management` | 公告管理读写、发布、删除；公开读取仅作为预览 | `example:announcement:read` | `example:announcement:write`、`example:announcement:delete` |
| 个人安全中心 | `/account/security` / `account-security` | 本人资料、显示名、密码、会话与全部设备退出 | 已登录 | 已登录；密码与全部设备退出需二次确认 |

页面权限由后端 `/navigation/routes` 的 `requiredPermission` 决定菜单可见性；每个写按钮额外直接检查 Pinia 当前用户的 `permissions`。后端 `@PreAuthorize` 始终是最终授权边界，前端控制只用于体验，不作为安全措施。

## 交互设计

### 数据页通用规则

- 列表使用 `el-table`、加载态、空态和 `el-pagination`；查询条件（用户关键词、状态、审计分页）保留在页面响应式状态中。
- 创建和编辑使用 `el-dialog`/`el-drawer`，前端先按 DTO 规则校验，再提交。
- 删除、禁用、撤销会话、发布状态变更和“退出全部设备”使用 `ElMessageBox.confirm`。
- 成功后只刷新受影响的数据；401 继续交给现有 HTTP 拦截器刷新 Access Token；403 提示无权限，422/400 显示接口字段校验信息。

### 各页细节

- 用户管理：表格显示账号、显示名、状态、角色；详情抽屉包含角色多选、状态选择与会话撤销。不能在前端绕过最后超级管理员的保护，直接展示服务端 `409`。
- 权限中心：左右布局或 Tabs：角色表、权限表、当前角色的分组复选权限。保存角色权限时按差集逐项调用授予/撤销 API，任一失败即停止并刷新角色详情。
- 运行时配置：按安全、限流、CORS、接口策略、IP 策略和身份注册开关分组，以开关、数字输入、文本域替代通用键值调试表单；高风险 IP/CORS 修改显示风险提示。
- IP 规则：表格展示 `ALLOW/DENY`、网络、到期时间和原因；表单支持本地日期时间转换为毫秒时间戳，删除需确认。
- 审计日志：只读分页表格，展示时间、动作、结果、操作者、目标、Trace ID 与格式化 metadata。
- 公告管理：左侧公告表格，右侧/抽屉编辑标题、正文、发布状态；草稿创建、保存、发布/撤回和删除是独立操作，公开预览在新窗口打开现有公共 URL。
- 个人安全中心：显示本人资料与会话表；显示名和密码分开表单；“退出全部设备”成功后清空本地会话并跳转登录页。
- 系统概览：健康标签、版本/构建信息、Swagger UI 与 OpenAPI JSON 快捷链接；这些只读能力不新增后台权限。

## 前端结构

```text
src/api/
  admin-users.ts, rbac.ts, runtime-config.ts, security.ts, announcements.ts
src/views/
  system/users, system/rbac, system/config, system/ip-rules,
  system/audit, content/announcements, account/security
src/composables/
  useCurrentPermissions.ts
```

每个 API 文件输出接口 DTO、分页类型和请求函数；页面不直接拼接 HTTP 路径。`useCurrentPermissions` 基于 `useUserStoreHook().permissions` 提供 `can(permission)`，避免误用当前路由的 `meta.auths` 来判定按钮权限。

## 动态导航扩展

后端允许的受控组件键扩展为：`user-management`、`rbac-management`、`runtime-config`、`ip-rule-management`、`audit-log`、`announcement-management`、`account-security`，并在前端显式注册对应组件。`NavigationBootstrap` 初始化“系统管理”目录及其子页、“内容管理/公告管理”和“账户/安全中心”；菜单条目全为 `systemManaged`，不可在菜单页面删除或修改。

前端继续使用受控注册表，并在合并动态菜单前移除已由静态路由注册的路径。叶子页面不得带 `children: []`，以避免侧栏按空目录过滤。

## 测试与验收

- 后端：为新增组件键白名单和内置菜单初始化补回归测试；完整 Maven 测试通过。
- 前端：扩展现有路由转换脚本，验证内置静态首页不会重复进入动态菜单、每个受控组件键可转换、叶子菜单无空 children。
- 页面：为 API DTO 和关键权限计算补可执行单测或 Node 验证脚本；运行 `pnpm typecheck`、`pnpm build` 与 `git diff --check`。
- 手工验收：超级管理员可见全部后台页面；仅拥有读权限的账号看不到写按钮且后端仍拒绝写请求；用户、角色、配置、IP 规则、公告和个人安全操作可完成并刷新结果。

## 不在本次范围

- 新增工作流、批量导入/导出、消息通知、仪表盘图表或新的后端业务接口。
- 将 Swagger/OpenAPI 文档嵌入 iframe；只提供安全的新窗口链接。
