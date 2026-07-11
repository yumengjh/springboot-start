# RBAC guide

## Data model

RBAC is database-backed and extensible:

```text
users --< user_roles >-- roles --< role_permissions >-- permissions
```

Roles and permissions are records, not Java enums. Role codes and permission codes are
unique. Permission codes are immutable and follow `resource:action`. The centralized
authorization layer grants `SUPER_ADMIN` bypass behavior; controllers must not repeat
special-case checks.

Initial roles are:

- `SUPER_ADMIN`
- `ADMIN`
- `USER`

Initial permissions are:

- `system:user:read`, `system:user:write`
- `system:role:read`, `system:role:write`, `system:role:assign`
- `system:config:read`, `system:config:write`
- `system:audit:read`
- `example:announcement:read`, `example:announcement:write`,
  `example:announcement:delete`

Seed execution is idempotent. The seed's role-to-permission assignments in the
implemented initializer are authoritative; do not assume every named role receives
every permission.

## API and enforcement

The identity API provides role/permission CRUD, assignment operations, paginated user
listing, account-status operations, and forced session revocation. Exact route suffixes
and DTO shapes are published in `/v3/api-docs`.

Access tokens contain the user's effective permission set. Spring authorities are
derived from that set. A protected method uses the shared evaluator, for example:

```java
@PreAuthorize("@permissionEvaluator.has('system:user:read')")
public PageResponse<UserResponse> listUsers(Pageable pageable) {
    // delegate to the application service
}
```

Enforce business authorization at the application boundary as well as the HTTP entry
point when the service can be invoked elsewhere. Expected HTTP behavior is `401` for
anonymous requests and `403` for an authenticated principal without permission.

Removing a permission affects newly issued access tokens; clients should refresh or
log in again. For urgent containment, also revoke the user's sessions. Role deletion is
rejected while it remains assigned unless assignments are explicitly removed in the
same service transaction.

## Add a module permission

For a new `billing` module with invoice mutation:

1. Choose a stable code such as `billing:invoice:write`; do not encode a role name in
   the permission.
2. Add the permission to the module/security seed using an idempotent insert or lookup.
3. Assign it to the intended seeded roles explicitly. Do not silently broaden `USER`.
4. Protect controller and application-service mutations with
   `@permissionEvaluator.has('billing:invoice:write')`.
5. Add the permission and required bearer authentication to OpenAPI.
6. Test anonymous `401`, insufficient-permission `403`, allowed behavior, removal on a
   newly issued token, and centralized `SUPER_ADMIN` behavior.
7. If administration APIs expose permission catalogs, verify the new record appears
   without exposing user credentials or refresh hashes.

Keep role/permission repositories inside `identity`. A business module depends on the
public permission-evaluation contract, not those repositories.
