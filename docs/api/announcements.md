# 公告示例模块

这是业务模块的最小参考实现：实体放在 `entities`，Controller、Service、Repository 和 DTO 放在 `modules/announcements`，不把业务开关放入系统运行时配置。

- `GET /api/v1/announcements`：公开读取已发布公告。
- `GET /api/v1/announcements/{id}`：读取任意公告，需要 `example:announcement:read`。
- `POST /api/v1/announcements`：创建草稿，需要 `example:announcement:write`。
- `PUT /api/v1/announcements/{id}`：修改标题和内容，需要 `example:announcement:write`。
- `PUT /api/v1/announcements/{id}/publication?published=true|false`：发布或撤回，需要 `example:announcement:write`。
- `DELETE /api/v1/announcements/{id}`：删除，需要 `example:announcement:delete`。

创建或修改请求：`{"title":"标题","content":"内容"}`。标题最长 160 字符，内容最长 20,000 字符。修改会写入系统审计事件，实体携带 JPA 乐观锁版本字段。
