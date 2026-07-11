# Spring Boot Starter Template Design

## Purpose

Build a reusable, production-oriented Spring Boot backend starter. It is an engineering foundation rather than a complete business system. It must start locally with SQLite, run in production with PostgreSQL, demonstrate authentication and RBAC through a small announcement module, and provide reusable service-governance capabilities without mixing business configuration into the system module.

## Scope

The first release includes:

- Java 21 and Spring Boot 4.1.x with Maven Wrapper.
- REST APIs only; no frontend.
- SQLite for the default local profile and PostgreSQL for production.
- Liquibase schema migrations and JPA schema validation.
- User registration, authentication, session management, user administration, and extensible RBAC.
- Short-lived JWT access tokens and rotating, revocable refresh tokens.
- Runtime service configuration for rate limiting, IP policies, brute-force protection, request logging, and security auditing.
- A small announcement module that demonstrates public and permission-protected operations.
- Structured logging, request correlation, audit logging, health and version endpoints.
- OpenAPI documentation, Docker assets, GitHub Actions CI, image publishing, and security scanning.
- Unit and integration tests for the important security and compatibility paths.

The first release explicitly excludes:

- A web administration interface.
- MySQL and SQL Server.
- Runtime database switching.
- Redis or distributed rate-limit counters.
- Email verification, CAPTCHA, password recovery, OAuth login, and multi-factor authentication.
- Business-specific configuration or domain modules beyond the announcement example.
- Deployment to a specific user's server or cloud provider.

## Architecture

The application is a modular monolith. Top-level modules own their data and behavior. Each module uses a light internal separation between API, application, domain, and infrastructure code where useful, without forcing empty abstraction layers.

### Modules

`bootstrap` owns application startup, Spring configuration, profile selection, initial data, and build metadata exposure.

`shared` contains only genuinely cross-cutting contracts and infrastructure: API response and error formats, pagination, validation helpers, request correlation, time abstractions, and shared test support. It must not contain business rules.

`identity` owns users, credentials, authentication, roles, permissions, refresh sessions, account status, login policy, and identity-specific runtime settings such as whether self-registration is allowed. Email, CAPTCHA, OAuth, and MFA integrations will attach through explicit interfaces later.

`system` owns only service-governance behavior: typed runtime settings, IP access policies, general API rate limiting, request logging controls, security-event auditing, health information, and build/version information. Business modules cannot register their own behavior flags in `system`.

`example` owns announcement content and demonstrates how a business module defines data, permissions, endpoints, validation, and audit-relevant actions.

Modules communicate through public application interfaces rather than reaching into another module's repositories.

## Technology Baseline

- Java 21 LTS.
- Spring Boot 4.1.x and Spring Framework 7.x.
- Maven with a committed Maven Wrapper.
- Spring MVC, Bean Validation, Spring Data JPA, and Spring Security.
- Spring Security OAuth2 Resource Server primitives with a local JWT encoder and decoder; the template is not a full OAuth2 authorization server.
- Liquibase YAML changelogs.
- springdoc-openapi for OpenAPI JSON and Swagger UI.
- Spring Boot Actuator for health, information, and metrics.
- JUnit 5, Spring Boot Test, MockMvc, and Testcontainers.

Dependency versions managed by Spring Boot remain unpinned unless compatibility requires an override. Non-managed versions are centralized in `pom.xml` properties.

## Database Design

The `local` profile is the default and uses a file-backed SQLite database at `./data/app.db`. The `postgres` profile uses environment variables for its JDBC URL, username, and password. A process connects to exactly one database, and changing the database requires a restart.

Liquibase is the only schema-authoring mechanism. Hibernate uses schema validation and never creates or updates production tables. Changelogs use portable definitions where possible and database-specific changesets only where SQLite and PostgreSQL differ.

Hibernate's SQLite community dialect is used for local development. CI runs the same integration suite against both SQLite and a real PostgreSQL container.

Application-generated UUIDs are represented by Java `UUID` values and stored as portable 36-character strings. This intentionally favors identical mappings over PostgreSQL-native UUID optimization in the starter. Mutable aggregate roots use optimistic-lock version columns.

Core tables include:

- `users`
- `roles`
- `permissions`
- `user_roles`
- `role_permissions`
- `refresh_sessions`
- `system_settings`
- `ip_access_rules`
- `audit_events`
- `announcements`

## Authentication and Session Design

Users register with a username, display name, and password. Username normalization and uniqueness are enforced. The password is transformed with Spring Security's `DelegatingPasswordEncoder`, initially using BCrypt and retaining the algorithm prefix for future migration.

Successful login returns a short-lived signed JWT access token and an opaque refresh token. Access tokens are not persisted. Each refresh token belongs to a refresh session and is stored only as a cryptographic hash with user, token family, issue time, expiry, revocation status, and optional client metadata.

Refresh tokens rotate on every use. Reuse of an already-consumed refresh token revokes the token family. Supported operations include current-device logout, all-device logout, administrator-forced session revocation, and automatic revocation after a password change or account disablement.

JWT signing keys come from environment variables or mounted secret files. No usable default production key is committed. Development may generate an ephemeral key only when explicitly enabled by the local profile.

## Authorization Design

Users and roles have a many-to-many relationship; roles and permissions also have a many-to-many relationship. Neither custom roles nor permissions are Java enums. Initial roles and permissions are seeded idempotently.

Initial roles are `SUPER_ADMIN`, `ADMIN`, and `USER`. Permissions use the `resource:action` convention, including:

- `system:user:read`
- `system:user:write`
- `system:role:read`
- `system:role:write`
- `system:role:assign`
- `system:config:read`
- `system:config:write`
- `system:audit:read`
- `example:announcement:read`
- `example:announcement:write`
- `example:announcement:delete`

Controllers and application services enforce permissions with method security. `SUPER_ADMIN` bypass behavior is centralized in the authorization layer rather than repeated in controllers.

## Runtime Configuration

Runtime settings are typed, validated, persisted, cached as immutable snapshots, and auditable. Updating a setting performs authorization, type and range validation, persistence, audit recording, and atomic cache replacement. Invalid settings never partially apply.

The system module owns settings for:

- General API rate-limit capacity and window.
- Temporary and permanent IP deny rules.
- Optional IP allow rules for protected administration endpoints.
- Whether brute-force protection is active.
- Login failure threshold, observation window, and lock duration.
- Request-log and security-audit switches.

JWT signing material, database connections, trusted proxy ranges, CORS origins, and initial administrator credentials are startup configuration, not runtime settings.

Business and identity modules own their own settings. For example, self-registration policy belongs to `identity`, while announcement publication policy would belong to `example`. The common configuration mechanism may be reused, but ownership, keys, validation, APIs, and permissions remain inside the owning module.

Rate-limit and brute-force counters are local in-memory state in the first release. Persistent IP rules and account locks survive restarts. The design exposes narrow counter-store interfaces so a Redis implementation can be added for multi-instance deployments without changing controllers or policies.

## Client IP Security

The application uses the direct remote address by default. Forwarded headers are trusted only when the immediate sender matches explicitly configured trusted proxy networks. Arbitrary `X-Forwarded-For` values must never be treated as authoritative. IP parsing, normalization, and trusted-hop selection are centralized and tested.

## API Design

All application endpoints use `/api/v1`. Public authentication endpoints, authenticated self-service endpoints, and administration endpoints are clearly grouped.

Responses use a consistent envelope where it adds value, with stable error codes, human-readable messages, request trace IDs, and field validation details. HTTP status codes retain their normal meaning. Pagination has one shared request and response contract.

OpenAPI documents authentication requirements, permissions, example payloads, validation constraints, error responses, pagination, and token lifecycle. Swagger UI is enabled for local development and configurable in production.

The example announcement module provides:

- Public listing of published announcements.
- Authenticated retrieval where appropriate.
- Permission-protected create, update, publish/unpublish, and delete operations.
- Optimistic concurrency protection for modification.

## Observability and Audit

Every request receives or propagates a trace ID. Logs are structured and include timestamp, level, logger, trace ID, request method/path, status, duration, and authenticated user ID when available. Passwords, raw tokens, authorization headers, and sensitive request bodies are never logged.

Audit events record security and administrative actions, including login success/failure, token reuse detection, account status changes, role assignments, runtime-setting changes, IP-rule changes, and announcement mutations. Audit records include actor, action, target, result, timestamp, trace ID, and sanitized metadata.

Actuator exposes liveness, readiness, application information, build version, Git commit, and metrics. Sensitive actuator endpoints remain inaccessible publicly.

## Versioning

API compatibility is represented by the `/api/v1` path. Database schema history belongs to Liquibase. Concurrent record changes use optimistic-lock versions. Application build version, commit, and build time are exposed through a read-only information endpoint and Actuator.

## Container and Local Operations

The Dockerfile is a multi-stage build using Eclipse Temurin 21. The runtime image contains only the built application and JRE, runs as a non-root user, exposes a health check, and defines writable storage for SQLite when that profile is selected.

`compose.yaml` demonstrates the PostgreSQL profile with a health-checked database, persistent volume, environment variables, and application dependency ordering. SQLite runs without Compose.

Configuration examples use `.env.example`; no secrets are committed.

## CI and Image Delivery

GitHub Actions CI runs on pull requests and pushes. It verifies formatting or static checks, unit tests, SQLite integration tests, PostgreSQL integration tests, and package creation. PostgreSQL tests use Testcontainers when Docker is available in the runner.

On main-branch pushes and version tags, a separate workflow builds a multi-stage image, runs a vulnerability scan, and publishes to GitHub Container Registry. Deployment to a specific server is intentionally excluded; the documentation identifies the extension point for SSH, webhook, Kubernetes, or platform-specific deployment.

Workflow permissions are least-privilege, actions are pinned to stable major versions or immutable revisions where practical, and untrusted pull requests never receive publishing credentials.

## Testing Strategy

Unit tests cover policy and validation code. Web security tests cover anonymous, authenticated, insufficient-permission, and sufficient-permission paths. Integration tests cover database migrations and repository behavior on SQLite and PostgreSQL.

Security-critical scenarios include:

- Password hashing and invalid credentials.
- Access-token validation and expiry.
- Refresh rotation, revocation, and reuse detection.
- Disabled and locked accounts.
- RBAC assignment and permission enforcement.
- Brute-force thresholds and recovery windows.
- IP deny and trusted-proxy handling.
- Runtime-setting validation, immediate refresh, and auditing.
- Sensitive-data redaction.
- Announcement read/write authorization and optimistic locking.

## Initial Data and First Run

Roles and permissions are created idempotently. The initial super administrator is created only when required environment variables are present and no super administrator exists. Production startup fails with an actionable message if bootstrap is required but credentials are absent. Plaintext bootstrap credentials are never logged.

## Success Criteria

The template is complete when:

- A new developer can run it with SQLite using documented commands.
- The PostgreSQL profile starts through Compose and passes the same core tests.
- Registration, login, token refresh/rotation, logout, user administration, and RBAC work through documented APIs.
- Runtime service settings take effect without restart and produce audit records.
- The announcement module demonstrates public and permission-protected behavior.
- OpenAPI accurately describes every public endpoint and security requirement.
- The application builds with Maven Wrapper and as a non-root container image.
- CI definitions validate both databases and define safe GHCR image publishing.
- No committed file contains a usable production secret.
