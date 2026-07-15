# 动态简历模块设计

## 目标

为一份公开简历提供可扩展的动态内容管理：公开页面从后端读取全部业务数据，超级管理员可在后台管理页面编辑、排序、新增和删除简历区块。后续增加简历信息不需要新增数据库表。

## 范围与约束

- 仅维护一份简历，不引入用户维度、简历列表或所有权概念。
- 所有页面业务文案和数据均来自后端；页面布局、颜色与组件实现仍由前端负责。
- 权限 `resume:manage` 仅在启动引导时授予 `SUPER_ADMIN`。
- 公开读取接口不要求登录；管理接口要求 `resume:manage`。
- 简历 Nuxt 项目使用 `NUXT_PUBLIC_RESUME_API_BASE` 指向后端 API，默认值为本地后端地址。
- 启动命令必须兼容 macOS、Linux 与 Windows PowerShell/cmd；不使用 Windows 专属 `set` 语法。

## 数据模型

新增 `resume_documents` 表，仅保存一条文档记录：

| 字段 | 类型 | 用途 |
| --- | --- | --- |
| `id` | UUID | 主键 |
| `content` | TEXT / JSON 文本 | 完整的可扩展简历文档 |
| `schema_version` | INTEGER | 简历内容结构版本，初始为 1 |
| `version` | INTEGER | JPA 乐观锁，避免覆盖并发编辑 |
| `created_at` / `updated_at` | 时间 | 审计字段 |

`content` 的根对象为：

```json
{
  "seo": { "title": "姓名 - 职位", "description": "简历描述" },
  "profile": {
    "name": "姓名",
    "headline": "前端开发实习",
    "contacts": [{ "icon": "ri:mail-line", "label": "邮箱", "text": "name@example.com", "href": "mailto:name@example.com" }]
  },
  "sections": [
    { "id": "summary", "type": "bullet-list", "title": "个人简介", "items": ["..."] },
    { "id": "experience", "type": "timeline", "title": "技术经历", "items": [{ "name": "...", "description": "...", "from": "2024.10", "to": "现在", "bullets": ["..."] }] },
    { "id": "projects", "type": "projects", "title": "项目经验", "items": [{ "name": "...", "description": "...", "role": "...", "link": "https://...", "from": "...", "to": "...", "techs": ["..."], "bullets": ["..."] }] }
  ]
}
```

支持的首批区块类型为 `bullet-list`、`timeline`、`projects`。前端和后台保留 `custom` 类型：后台可编辑其标题和 JSON 内容，公开端在无法识别的类型时不渲染但显示开发警告，保证新类型上线不会导致整个页面白屏。

## 接口与权限

| 方法与路径 | 鉴权 | 行为 |
| --- | --- | --- |
| `GET /api/v1/resume` | 公开 | 读取单份完整简历文档 |
| `GET /api/v1/resume/manage` | `resume:manage` | 读取管理用完整文档与版本号 |
| `PUT /api/v1/resume/manage` | `resume:manage` | 校验并覆盖更新整份文档；版本不一致返回明确的冲突错误 |

更新操作写入审计事件 `RESUME_UPDATED`。系统启动时若不存在记录，则以当前 `yu-resume` 页面中的内容创建默认文档。

## 后端结构

新增 `modules.resume`，沿用现有 announcement 模块的分层：

- `api/ResumeController`：公开与受保护接口。
- `api/dto/ResumeDocumentRequest`、`ResumeDocumentResponse`：请求和响应契约。
- `application/ResumeService`：读取、验证、更新、审计与初始化。
- `infrastructure/ResumeDocumentRepository`：单文档查询。
- `entities/ResumeDocument`：JPA 实体。
- Liquibase 变更集创建表。

在 RBAC 启动数据中添加 `resume:manage` 的中文说明；在导航启动数据中添加“内容管理 → 简历管理”页面，菜单访问要求为该权限。

## 后台管理页面

在 `springboot-admin` 新增 API 客户端和“简历管理”页面，使用 Element Plus：

- 基本信息与 SEO：表单字段编辑；联系人可新增、删除、排序。
- 区块列表：新增、删除、上下移动、选择区块类型。
- 已知区块类型：提供项目、时间线和列表的可视化嵌套表单。
- `custom` 区块：提供格式校验的 JSON 编辑区，避免受固定表单限制。
- 保存带版本号；冲突时提示用户先重新加载，避免静默覆盖。

## 公开简历前端

修复 Nuxt 脚本为跨平台命令，并通过 runtime config 使用 API 地址。页面启动时请求公开接口，根据 `sections` 数组和区块类型渲染已有组件；加载中展示轻量提示，请求失败给出可诊断提示而不展示硬编码简历内容。公开端不包含管理令牌或写接口。

## 测试与验证

- 后端：公开读取、无权限管理读取、超级管理员更新、无效结构、乐观锁冲突及初始化测试。
- 管理前端：文档转换与区块类型编辑的契约测试（若现有测试工具可用）。
- Nuxt：`pnpm dev`、`pnpm build` 与 `pnpm generate` 均在当前 macOS 环境中执行；公开页面使用本地后端数据验证。
- 管理端：类型检查与生产构建通过；超级管理员菜单可见且保存后公开端刷新可见更新。
