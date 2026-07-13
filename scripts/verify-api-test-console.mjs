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
