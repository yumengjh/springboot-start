# RBAC 权限说明

权限模型由数据库维护：

```text
users --< user_roles >-- roles --< role_permissions >-- permissions
```

角色和权限都不是 Java 枚举，后续可在数据库中扩展。权限编码使用 `资源:动作`，例如 `system:role:assign`。

## 内置角色与权限

初始角色：`SUPER_ADMIN`、`ADMIN`、`USER`。`SUPER_ADMIN` 会获得当前所有内置权限；新注册用户会自动获得 `USER`。

内置权限包括：

- `system:user:read`、`system:user:write`
- `system:role:read`、`system:role:write`、`system:role:assign`
- `system:config:read`、`system:config:write`
- `system:audit:read`
- `example:announcement:read`、`example:announcement:write`、`example:announcement:delete`

权限会写入新签发的 JWT；修改角色或权限后，用户应重新登录或刷新 Token。

## 已实现接口

以下接口均位于 `/api/v1/rbac`，且需要对应的 JWT 权限：

- `GET /roles`：查看角色及其权限，需 `system:role:read`。
- `PUT /users/{userId}/roles/{roleCode}`：给用户分配角色，需 `system:role:assign`。
- `DELETE /users/{userId}/roles/{roleCode}`：移除用户角色，需 `system:role:assign`。
- `PUT /roles/{roleCode}/permissions/{permissionCode}`：给角色授予权限，需 `system:role:write`。
- `DELETE /roles/{roleCode}/permissions/{permissionCode}`：移除角色权限，需 `system:role:write`。

角色/权限创建、管理员用户列表、禁用、锁定和解锁接口尚未实现。
