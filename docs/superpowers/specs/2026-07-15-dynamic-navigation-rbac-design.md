# 动态导航与 RBAC 集成设计

## 目标

为 `springboot-admin` 提供数据库驱动的内部页面菜单、动态路由与管理界面；菜单可由具有菜单管理权限的用户维护，并按现有 RBAC 权限过滤。修复现有动态路由因空假数据或未知组件导致持续“加载中”的问题。

## 范围

- 支持内部页面的目录与页面菜单，不支持外链、iframe 或后端下发任意组件路径。
- 提供菜单树读取和菜单 CRUD。
- 复用 `roles -> permissions` 关系；菜单通过权限编码引用权限目录，不新增菜单—角色关系。
- 前端集成动态路由加载、菜单管理页及路由失败降级。

## 后端架构

新增 `navigation` 模块，包含实体、Repository、应用服务和 REST 控制器。

`navigation_menus` 表字段：

| 字段 | 说明 |
| --- | --- |
| `id` | UUID 主键 |
| `parent_id` | 父菜单 ID，可为空 |
| `code` | 稳定且唯一的菜单编码 |
| `title` | 菜单标题 |
| `route_path` | 前端路由路径，唯一 |
| `component_key` | 受控前端组件键；目录为空，页面必填 |
| `icon` | Iconify 图标键，可为空 |
| `sort_order` | 同级排序值 |
| `menu_type` | `DIRECTORY` 或 `PAGE` |
| `required_permission` | 可为空；非空时必须存在于权限目录 |
| `visible`、`enabled`、`keep_alive`、`system_managed` | 显示、启用、缓存和内置保护标记 |
| 审计字段 | 复用基类的版本与时间字段 |

服务端固定允许的组件键为 `welcome`、`permission-page`、`menu-management`；任何新增页面都必须先在前端注册组件键后才能在服务端允许创建。目录不得有组件键，页面必须有组件键。路径必须以 `/` 开头、不能是保留的认证或错误路由，且父项必须为目录。服务端拒绝重复编码、重复路径、循环父子关系和不存在的权限编码。

内置初始化菜单为首页和菜单管理。两者 `system_managed=true`，不可删除或禁用；菜单管理页面要求 `system:menu:read`，修改接口要求 `system:menu:write`。新增两个权限并授予 `SUPER_ADMIN`。

## API

- `GET /api/v1/navigation/routes`：仅需已认证；根据 JWT 的权限声明过滤启用菜单，返回嵌套 `NavigationRouteResponse`。无所需权限的项目对所有登录用户可见。
- `GET /api/v1/navigation/menus`：需 `system:menu:read`；返回未过滤的管理树。
- `POST /api/v1/navigation/menus`：需 `system:menu:write`；创建目录或页面，返回 `201`。
- `PUT /api/v1/navigation/menus/{id}`：需 `system:menu:write`；更新可变字段，编码不可改名。
- `DELETE /api/v1/navigation/menus/{id}`：需 `system:menu:write`；拒绝删除内置菜单或仍有子项的目录。

所有响应沿用统一 `ApiResponse` 信封。菜单修改记录审计事件；角色或权限修改仍由既有 RBAC 服务撤销受影响会话。

## 前端架构

`src/api/routes.ts` 改为调用后端动态路由接口。前端定义 `componentKey -> async component` 的显式注册表，不再通过后端字符串搜索 `src/views`。收到的路由必须经过转换和校验：未知组件、路径冲突和不合法树会被跳过并显示错误提示；初始化 Promise 必须 resolve/reject，失败时继续保留首页，避免永久加载。

登录和会话恢复完成后调用 `initRouter()`；路由守卫在访问尚未注册的动态路径时等待一次初始化。动态菜单不缓存到 localStorage，以便菜单变更能够在页面刷新后立即生效。

新增 Element Plus 菜单管理页：树表展示菜单，抽屉表单新增或编辑目录/页面，提供排序、显示/启用、缓存、图标、权限和组件键选择。组件键和权限由受控选择项提供。页面按钮通过 `system:menu:read` 与 `system:menu:write` 控制。

## 数据流

```text
登录 / 会话恢复 -> /users/me -> /navigation/routes
  -> 前端组件键注册表校验 -> router.addRoute -> 渲染过滤后的菜单

菜单管理 CRUD -> 后端实体校验 + 审计 -> 数据库
  -> 用户刷新页面 / 重新登录 -> 获取新菜单树
```

## 错误与安全

- 动态路由只控制导航与前端展示；受保护业务 API 始终由现有 `@PreAuthorize` 执行最终授权。
- Refresh Token 与现有 Cookie 安全边界不变。
- 服务端禁止任意组件路径，避免后端数据驱动客户端加载非预期模块。
- 菜单树过滤仅信任 JWT 权限；菜单管理接口另有服务端权限校验。
- 前端无法加载某菜单时不会阻塞应用启动，并会保留首页和菜单管理恢复入口。

## 验证

- 后端单测：实体约束、组件键/层级/权限校验、按权限过滤、内置菜单保护、CRUD 授权与迁移。
- 前端测试或可验证行为：组件键转换、未知组件降级、路由初始化完成、菜单 CRUD 表单与按钮权限。
- 集成：管理员创建菜单并赋予权限；有权限用户可见并访问，无权限用户看不到且受保护 API 仍返回拒绝。
