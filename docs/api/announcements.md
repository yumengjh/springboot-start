# 公告示例模块

这是业务模块的最小参考实现：实体放在 `entities`，Controller、Service、Repository 和 DTO 放在 `modules/announcements`，不把业务开关放入系统运行时配置。

- `GET /api/v1/announcements`：公开读取已发布公告列表。
- `GET /api/v1/announcements/{id}`：公开读取一条已发布公告的内容。
- `GET /api/v1/announcements/manage`：读取全部公告（含草稿），需要 `example:announcement:read`。
- `GET /api/v1/announcements/manage/{id}`：读取任意公告详情，需要 `example:announcement:read`。
- `POST /api/v1/announcements`：创建草稿，需要 `example:announcement:write`。
- `PUT /api/v1/announcements/{id}`：修改标题和内容，需要 `example:announcement:write`。
- `PUT /api/v1/announcements/{id}/publication?published=true|false`：发布或撤回，需要 `example:announcement:write`。
- `DELETE /api/v1/announcements/{id}`：删除，需要 `example:announcement:delete`。

创建或修改请求：`{"title":"标题","content":"内容"}`。标题最长 160 字符，内容最长 20,000 字符。修改会写入系统审计事件，实体携带 JPA 乐观锁版本字段。

公告读取接口会按调用者权限自动区分：匿名调用者得到公开 DTO，不使用标准响应信封；持有 `example:announcement:read` 的调用者访问同一路径时，得到标准信封中的完整管理 DTO。两种调用都经过 IP 访问控制、接口禁用和限流。

公开列表直接返回最小字段数组：

```json
[
  {
    "id": "公告 ID（用于读取详情）",
    "title": "标题",
    "authorDisplayName": "作者显示名",
    "version": 1
  }
]
```

公开详情直接返回 `title`、`content`、`authorDisplayName`、`version`。公开列表保留 `id`，以便客户端请求详情；除此之外，公开结果不含 `authorId`、发布状态、发布时间、`traceId` 或 `timestamp`。草稿使用公开详情路径时与不存在资源一样返回 `404`。以后新增外部开放接口时，在 Controller 方法上标记 `@PublicApi` 即可。默认 `minimalResponse` 为 `false`，只有外部内容接口需要极简结果时才启用它。
