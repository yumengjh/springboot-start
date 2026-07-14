import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

const page = readFileSync(new URL('../api-test.html', import.meta.url), 'utf8');

assert.match(page, /id="username"[^>]*value="admin"/);
assert.match(page, /id="userCount"/);
assert.match(page, /id="userRows"/);
assert.match(page, /id="roleSelect"/);
assert.match(page, /id="permissionGroups"/);
assert.match(page, /function renderUsers\(/);
assert.match(page, /function renderRoles\(/);
assert.match(page, /function saveRolePermissions\(/);
assert.match(page, /id="endpointRatePatterns"/);
assert.match(page, /id="ipAllowList"/);
assert.match(page, /function loadAuditEvents\(/);
assert.match(page, /id="announcementTitle"/);
assert.match(page, /function createAnnouncement\(/);
assert.match(page, /function setAnnouncementPublication\(/);
assert.match(page, /id="rememberLogin"/);
assert.match(page, /function restoreLogin/);
assert.match(page, /const health =/);
assert.match(page, /const systemInfo =/);
assert.match(page, /const openApi =/);
assert.match(page, /REQUIRED_CONSOLE_CORS_METHODS/);
assert.match(page, /function validateConsoleCorsMethods\(/);
assert.match(page, /id="swaggerLink"/);
assert.match(page, /function loadSelectedUser\(/);
assert.match(page, /function revokeUserSessions\(/);
assert.match(page, /function loadPermissions\(/);
assert.match(page, /id="rbacRoleCode"/);
assert.match(page, /id="rbacPermissionCode"/);
assert.match(page, /function createRole\(/);
assert.match(page, /function updateRole\(/);
assert.match(page, /function deleteRole\(/);
assert.match(page, /function createPermission\(/);
assert.match(page, /function updatePermission\(/);
assert.match(page, /function deletePermission\(/);
assert.match(page, /(?:function|const) loadMySessions/);
assert.match(page, /function revokeMySessions\(/);
assert.match(page, /function clearLoginState\(/);
assert.match(page, /id="ipRuleNetwork"/);
assert.match(page, /function loadIpRules\(/);
assert.match(page, /function createIpRule\(/);
assert.match(page, /function deleteIpRule\(/);
assert.match(page, /id="auditPage"/);
assert.match(page, /function loadPublicAnnouncementById\(/);
assert.match(page, /anonymous = false/);
for (const route of [
  '/api/v1/auth/register', '/api/v1/auth/login', '/api/v1/auth/refresh', '/api/v1/auth/logout',
  '/api/v1/users/me', '/api/v1/users/me/sessions', '/api/v1/admin/users', '/api/v1/rbac/roles', '/api/v1/rbac/permissions',
  '/api/v1/system/runtime-config', '/api/v1/system/ip-access-rules', '/api/v1/system/audit-events',
  '/api/v1/system/info', '/api/v1/announcements', '/api/v1/announcements/manage',
  '/actuator/health', '/v3/api-docs'
]) assert.ok(page.includes(route), `missing route coverage: ${route}`);
assert.match(page, /function newAnnouncement\(/);
assert.match(page, /function setAnnouncementMode\(/);
assert.match(page, /id="updateAnnouncementButton"[^>]*disabled/);
assert.match(page, /id="announcementLookupId"/);
assert.match(page, /function loadManagedAnnouncements\(/);
assert.match(page, /管理视图：全部公告/);
