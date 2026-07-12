# 认证与会话说明

所有认证接口都在 `/api/v1/auth` 下：

- `POST /register`：注册，传入 `username`、`displayName`、`password`。
- `POST /login`：登录，传入 `username`、`password`。
- `POST /refresh`：刷新令牌，传入 `{"refreshToken":"..."}`。
- `POST /logout`：当前设备登出，传入 `{"refreshToken":"..."}`。

用户名必须匹配 `[A-Za-z0-9_.-]{3,32}`，服务端会转换为小写。显示名最长 80 个字符；密码长度为 10–128。密码通过 Spring Security 的 DelegatingPasswordEncoder 加密，当前实际使用 BCrypt，响应永远不会返回密码哈希。

## Token

登录和刷新成功后都会返回：

```json
{
  "accessToken": "RSA 签名的 JWT",
  "refreshToken": "一次性不透明令牌",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

Access Token 有效期为 15 分钟。调用受保护接口时使用：

```http
Authorization: Bearer <access-token>
```

JWT 包含用户 ID、用户名、令牌版本和有效权限列表。Refresh Token 有效期为 30 天，数据库只保存它的 SHA-256 哈希，绝不保存原文。

## 刷新、复用与登出

每次刷新都会消耗旧 Refresh Token，并返回一组新的 Access/Refresh Token；客户端必须原子地替换旧值。旧 Token 被再次使用时，服务会撤销该 Token 家族并返回 `401 AUTHENTICATION_REQUIRED`。

登出会撤销传入的 Refresh Token。修改密码会撤销该用户所有 Refresh Token。全设备登出、管理员强制下线和账户禁用联动仍待实现。

## 密钥边界

当前实现会在每次应用启动时生成 RSA 密钥对，因此重启后原 Access Token 失效。生产环境外部密钥（环境变量或挂载文件）尚未接入，当前方式仅适用于本地开发与模板验证，不能直接用于生产部署。
