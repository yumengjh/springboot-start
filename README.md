# Spring Boot Starter Template

A production-oriented Java 21 / Spring Boot 4.1 starter for REST services. It is a
modular monolith with SQLite for local development, PostgreSQL for deployment,
Liquibase migrations, JWT authentication with rotating refresh tokens, database-backed
RBAC, runtime service-governance settings, and an announcement example module.

> This branch documents the intended public contract from the design and implementation
> plan. Commands that require application or container assets must be run after those
> assets are present. See [Verification status](#verification-status).

## Prerequisites

- JDK 21 (`java -version`)
- A POSIX shell for `./mvnw` (Windows users can use `mvnw.cmd`)
- Docker with Compose v2 for PostgreSQL and image workflows (optional for SQLite)

## Quick start with SQLite

The default `local` profile uses `jdbc:sqlite:${APP_DATA_DIR:./data}/app.db`. Supply the
first administrator only through the process environment; the password below is a
local example and must not be reused.

```bash
export APP_BOOTSTRAP_ADMIN_USERNAME=admin
export APP_BOOTSTRAP_ADMIN_DISPLAY_NAME=Administrator
export APP_BOOTSTRAP_ADMIN_PASSWORD='LocalPassword123!'
./mvnw spring-boot:run
```

On first run, Liquibase creates the schema and the bootstrap user receives the
`SUPER_ADMIN` role. Bootstrap is idempotent. Production startup fails with an
actionable error when an administrator is required but no bootstrap credentials are
available. Plaintext bootstrap credentials are never logged.

Verify the running service:

```bash
curl --fail http://localhost:8080/actuator/health/readiness
curl --fail http://localhost:8080/v3/api-docs >/dev/null
```

Swagger UI is enabled for local development at
<http://localhost:8080/swagger-ui/index.html>. Its production availability is
configurable.

### Login and refresh

The authentication contract groups login and refresh beneath `/api/v1/auth`, but the
design does not specify their literal route suffixes or DTO fields. Inspect the
assembled application's OpenAPI document before calling them:

```bash
curl --fail http://localhost:8080/v3/api-docs > openapi.json
```

The conceptual login request supplies the configured username and password; the
conceptual refresh request supplies the most recently returned opaque refresh token.
Do not treat `/api/v1/auth/login`, `/api/v1/auth/refresh`, or any example JSON field
names as authoritative until controller mappings and OpenAPI are present in the
assembled repository.

Refresh tokens are one-time values: replace the stored token after every successful
refresh. See [Authentication](docs/api/authentication.md) for lifecycle and key
management.

## PostgreSQL with Compose

Copy the variable contract and fill in values locally; never commit the resulting
`.env` file.

```bash
cp .env.example .env
docker compose config
docker compose up --build --wait
docker compose ps
```

Compose selects the `postgres` profile and waits for PostgreSQL health before starting
the application. The application profile reads `DB_URL`, `DB_USERNAME`, and
`DB_PASSWORD`. Stop services with `docker compose down`; add `--volumes` only when you
intend to delete the local database.

## Build and test

```bash
./mvnw clean verify
```

The complete suite covers unit tests, SQLite integration, and PostgreSQL integration
when its Testcontainers prerequisites are available. A successful verification also
generates the JaCoCo report under `target/site/jacoco/`.

Build a container image:

```bash
docker build -t spring-boot-starter-template:local .
docker image inspect spring-boot-starter-template:local
```

The multi-stage image uses a Java 21 runtime, runs as a non-root user, exposes port
8080, and includes a health check.

## Configuration contract

| Setting | Purpose | Required |
| --- | --- | --- |
| `APP_DATA_DIR` | Parent directory for the local SQLite file | No; defaults to `./data` |
| `DB_URL` | PostgreSQL JDBC URL | With `postgres` profile |
| `DB_USERNAME` | PostgreSQL login | With `postgres` profile |
| `DB_PASSWORD` | PostgreSQL secret | With `postgres` profile |
| `APP_BOOTSTRAP_ADMIN_USERNAME` | Initial super-admin username | When bootstrap is required |
| `APP_BOOTSTRAP_ADMIN_DISPLAY_NAME` | Initial super-admin display name | When bootstrap is required |
| `APP_BOOTSTRAP_ADMIN_PASSWORD` | Initial super-admin password | When bootstrap is required |

JWT RSA keys, trusted proxy networks, CORS origins, and production Swagger exposure are
startup configuration. Use the exact property/environment bindings present in the
implemented configuration and `.env.example`; the design does not define additional
environment variable names. The local profile may generate an ephemeral RSA key only
when `app.security.jwt.allow-ephemeral-key=true`. Never enable that behavior in
production.

See [Deployment](docs/operations/deployment.md) for the production checklist and
[Runtime settings](docs/operations/runtime-settings.md) for settings that may be
changed without restart.

## Package structure

```text
com.yumg.starter
├── bootstrap   startup, Spring configuration, seeds, and build metadata
├── shared      cross-cutting API, persistence, security, web, and test contracts
├── identity    users, credentials, sessions, roles, and permissions
├── system      service governance, settings, IP policy, audit, and system info
└── example     announcement domain and API
```

Each feature module may separate `api`, `application`, `domain`, and `infrastructure`
internally. Controllers call application services; they do not reach across modules to
repositories. Modules communicate through public application interfaces. `shared`
contains no business rules.

When adding a business module:

1. Keep its entities, repositories, policies, DTOs, and runtime behavior flags inside
   the module.
2. Add Liquibase changesets; never use Hibernate to author or update the schema.
3. Define database-backed `resource:action` permissions and seed them idempotently.
4. Enforce permissions in application services/controllers and document them in
   OpenAPI.
5. Add anonymous, denied, allowed, migration, and optimistic-concurrency tests as
   applicable.

See [RBAC](docs/api/rbac.md) for a worked permission-extension checklist.

## API and operations guides

- [Project goal, current status, and roadmap](docs/PROJECT_STATUS.md)
- [Authentication and sessions](docs/api/authentication.md)
- [Roles and permissions](docs/api/rbac.md)
- [Runtime settings](docs/operations/runtime-settings.md)
- [Deployment](docs/operations/deployment.md)

All application APIs are versioned under `/api/v1`. Health endpoints are
`/actuator/health/liveness` and `/actuator/health/readiness`; build information is
available at `/api/v1/system/info`. Errors use stable codes, human-readable messages,
trace IDs, and validation details without exposing exception internals.

## Non-goals

This starter does not include a frontend or admin UI, runtime database switching,
MySQL, SQL Server, Redis/distributed counters, email verification, CAPTCHA, password
recovery, OAuth login, MFA, business-specific configuration, extra domain modules, or
deployment to a particular server/cloud provider.

## Verification status

The documented implementation contract is defined by the design and plan. Actual
startup, endpoint payloads, Docker behavior, and `./mvnw clean verify` are
environment- and implementation-dependent and must be confirmed in the assembled
repository. A missing Docker daemon may prevent PostgreSQL/Testcontainers and image
checks without invalidating SQLite-only documentation checks.
