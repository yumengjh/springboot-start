# API 测试台改造 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `api-test.html` 改造成可直观操作现有用户管理和 RBAC API 的本地测试台。

**Architecture:** 页面继续使用原生 HTML、CSS 和 JavaScript，不引入构建工具。一个 `api()` 请求函数统一附带令牌和渲染响应；用户、角色与权限在内存中缓存并渲染为表格和复选框。

**Tech Stack:** HTML5、CSS、浏览器 Fetch API、现有 Spring Boot REST API。

## Global Constraints

- 只修改根目录测试页面，不新增后端接口。
- 默认用户名为 `admin`，不写入任何默认密码或令牌。
- 操作完成后刷新受影响的用户或角色数据。

---

### Task 1: 重构测试页布局与默认登录

**Files:**
- Modify: `api-test.html`

- [x] 将表单布局替换为登录、用户、RBAC、当前用户、运行时配置和响应六个区域。
- [x] 将用户名输入框默认值设为 `admin`，密码保持空白。
- [x] 增加可复用的状态提示与响应渲染区域。
- [x] 通过静态结构与 JavaScript 语法检查确认页面不依赖外部资源；当前环境没有可连接的浏览器。

### Task 2: 增加用户表格与选中用户操作

**Files:**
- Modify: `api-test.html`

- [x] 请求 `GET /api/v1/admin/users` 并渲染总数和用户表格。
- [x] 点击用户行保存选中用户 ID，填充状态与角色操作区域。
- [x] 用 `PATCH /api/v1/admin/users/{id}/status` 更新状态，成功后刷新用户列表。
- [x] 用现有用户角色 PUT/DELETE 接口分配或移除角色，成功后刷新用户和角色数据。

### Task 3: 增加清晰的角色权限视图与编辑

**Files:**
- Modify: `api-test.html`

- [x] 请求 `GET /api/v1/rbac/roles`，渲染角色选择与当前权限摘要。
- [x] 按 `system:user`、`system:role`、`system:config`、`system:audit`、`example:announcement` 分组渲染权限复选框。
- [x] 保存时逐个比较勾选状态与角色当前权限，调用现有权限 PUT/DELETE 接口。
- [x] 保存成功后刷新角色数据并更新权限摘要。

### Task 4: 验证与交付

**Files:**
- Modify: `api-test.html`

- [x] 使用浏览器静态检查 JavaScript 语法：`node --check`（从脚本提取后执行）。
- [x] 通过超级管理员登录，验证用户列表与角色权限接口分别返回 5 位用户、3 个角色和 11 项权限。
- [x] 记录无后台接口支持的操作边界，不显示误导性的编辑按钮。
- [x] 提交页面和设计文档，不推送远程仓库。
