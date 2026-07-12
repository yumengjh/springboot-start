# Project Goal and Delivery Status

## Goal

`springboot-start` is a reusable Spring Boot backend foundation for the author's own projects and later client work. It is not a generic collection of snippets or an all-in-one SaaS. Its first usable milestone is a runnable modular monolith with:

- SQLite for local startup and PostgreSQL for deployment.
- Database migrations, container delivery, GitHub Actions, health checks, and safe configuration.
- Account registration, JWT access tokens, rotating refresh sessions, and revocable login state.
- Database-backed RBAC, system audit logs, runtime service configuration, rate limiting, IP policy, and brute-force protection.
- A small announcement module that shows how a real business module uses permission checks and optimistic locking.

The codebase follows the maintainable parts of `yumer-server` rather than a rigid textbook layering:

```text
com.yumg.starter
├── config/                 startup, framework configuration, bootstrap data
├── common/                 API envelope, errors, trace ID, base entity, web utilities
├── entities/               application entities shared by multiple modules
└── modules/
    ├── auth/               registration, login, JWT, refresh sessions
    ├── users/              account state and user administration
    ├── rbac/               roles, permissions, assignments
    ├── runtime-config/     typed runtime configuration and cache refresh
    ├── security/           audit, IP rules, rate limits, brute-force policy
    └── announcements/      sample business module
```

Until domain entities exist, `common/entity` contains only reusable JPA base types. New concrete entities are added under `entities/`, and repositories/services/controllers remain inside their owning module. A module may use another module's exported service, but must not access its repository directly.

## Completed

- Java 21, Spring Boot 4.1, Maven Wrapper, local and PostgreSQL profiles.
- Portable Liquibase changelogs for users, roles, permissions, refresh sessions, runtime settings, IP rules, audits, and announcements.
- SQLite foreign-key enforcement and PostgreSQL/SQLite migration test coverage.
- Standard API errors, validation errors, safe 401/403 responses, request trace IDs, and response pagination.
- Container image, Compose example, GitHub Actions CI, image scan/publish workflow, and operational documentation.
- Initial architecture rules that prevent cross-module controller-to-repository dependencies.
- The Yumer-style top-level packages now exist: `config`, `common`, `entities`, and
  `modules`. The first module, `modules/auth`, contains public registration.
- Registration validates input, lowercases usernames, hashes passwords with Spring
  Security's delegating BCrypt encoder, persists users through JPA, and never returns
  credentials in its API response.

## CI Status

The first remote CI run exposed concrete integration issues:

- Spring Boot 4.1 needs `spring-boot-liquibase` in addition to `liquibase-core`; otherwise Liquibase does not create the schema during tests.
- The original architecture test did not allow the base persistence package. The package has been moved into `common/entity` and the rule now reflects the intended common layer.
- The image workflow referenced `aquasecurity/trivy-action@0.32.0`, but Trivy Action tags use the `v` prefix. It now uses `v0.36.0`.

These fixes are part of the next push. GitHub Actions is the source of truth for Java 21 compilation and test execution because the temporary agent environment lacks a writable Java 21/Maven setup.

## In Progress

- Complete the remaining Yumer-style modules: `users`, `rbac`, `runtime-config`,
  `security`, and `announcements`.
- Extend the auth module from registration to login, JWT access tokens, refresh-token
  rotation, logout, and account state transitions.
- Finish typed runtime configuration, audit records, and their integration with the identity/security modules.

## Next Delivery Order

1. Implement the rest of `auth`, then `users` and `rbac`: login, token refresh rotation,
   logout, account state, roles, and permissions.
3. Implement `runtime-config` and `security`: audited hot settings, IP rules, rate limits, and brute-force protection.
4. Implement `announcements` as the reference business module.
5. Add OpenAPI, bootstrap administrator support, full Docker/Compose verification, and a final end-to-end CI pass.

## Explicit Non-Goals for the First Milestone

- Frontend or management UI.
- Runtime database switching, MySQL, SQL Server, or Redis-backed distributed counters.
- Email verification, CAPTCHA, password recovery, OAuth login, MFA, or a provider-specific deployment target.
