# Spring Boot Starter Template Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a reusable Spring Boot backend starter with SQLite/PostgreSQL support, JWT refresh sessions, extensible RBAC, runtime service governance, an announcement example, documented APIs, containers, and CI image delivery.

**Architecture:** Use a modular monolith whose `identity`, `system`, and `example` modules own their APIs, application logic, data, and policies. Keep only real cross-cutting contracts in `shared`; use Liquibase as the schema authority, JPA validation at startup, and adapters for security state that may become distributed later.

**Tech Stack:** Java 21, Spring Boot 4.1.x, Maven Wrapper, Spring MVC, Spring Data JPA, Spring Security, OAuth2 Resource Server JWT primitives, Liquibase, SQLite, PostgreSQL, springdoc-openapi, Actuator, JUnit 5, MockMvc, and Testcontainers.

## Global Constraints

- Java baseline is exactly 21 LTS.
- Spring Boot baseline is 4.1.x and Spring Framework 7.x.
- Default `local` profile uses file-backed SQLite at `./data/app.db`.
- `postgres` profile reads its connection from environment variables; database changes require restart.
- Liquibase owns schema changes; Hibernate uses `ddl-auto: validate`.
- Runtime system settings control service governance only; module-specific behavior remains in the owning module.
- Access tokens are short-lived signed JWTs; opaque refresh tokens rotate and are stored only as hashes.
- Roles and permissions are database records, not Java enums.
- No frontend, Redis, MySQL, SQL Server, email verification, CAPTCHA, OAuth login, MFA, or provider-specific deployment is included.
- No committed file contains a usable production secret.

## Planned File Structure

```text
pom.xml
.mvn/wrapper/maven-wrapper.properties
mvnw
mvnw.cmd
src/main/java/com/yumg/starter/
  StarterApplication.java
  bootstrap/{BootstrapAdminInitializer,SecurityConfiguration,OpenApiConfiguration}.java
  shared/api/{ApiError,ApiExceptionHandler,PageResponse}.java
  shared/persistence/{BaseUuidEntity,AuditedEntity}.java
  shared/security/{CurrentPrincipal,PermissionEvaluator}.java
  shared/web/{ClientIpResolver,TraceIdFilter}.java
  identity/api/{AuthController,UserController,RoleController}.java
  identity/api/dto/*.java
  identity/application/{AuthenticationService,UserService,RoleService,TokenService}.java
  identity/domain/{User,UserStatus,Role,Permission,RefreshSession}.java
  identity/infrastructure/*Repository.java
  system/api/{SystemSettingController,IpAccessRuleController,AuditController,SystemInfoController}.java
  system/application/{RuntimeSettingService,IpAccessService,AuditService,BruteForceService,RateLimitService}.java
  system/domain/{SystemSetting,SettingKey,IpAccessRule,AuditEvent}.java
  system/infrastructure/*Repository.java
  example/api/AnnouncementController.java
  example/application/AnnouncementService.java
  example/domain/Announcement.java
  example/infrastructure/AnnouncementRepository.java
src/main/resources/
  application.yml
  application-local.yml
  application-postgres.yml
  db/changelog/db.changelog-master.yaml
  db/changelog/changes/001-identity.yaml
  db/changelog/changes/002-system.yaml
  db/changelog/changes/003-announcement.yaml
src/test/java/com/yumg/starter/**
Dockerfile
compose.yaml
.env.example
.github/workflows/ci.yml
.github/workflows/image.yml
README.md
```

---

### Task 1: Bootable Project and Configuration Baseline

**Files:**
- Create: `pom.xml`
- Create: `.mvn/wrapper/maven-wrapper.properties`
- Create: `mvnw`
- Create: `mvnw.cmd`
- Create: `src/main/java/com/yumg/starter/StarterApplication.java`
- Create: `src/main/resources/application.yml`
- Create: `src/test/java/com/yumg/starter/StarterApplicationTest.java`
- Create: `.gitignore`

**Interfaces:**
- Produces: a Java 21 Spring Boot application invoked with `./mvnw spring-boot:run` and tested with `./mvnw test`.

- [ ] **Step 1: Write the context smoke test**

```java
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration"
})
class StarterApplicationTest {
    @Test void contextLoads() {}
}
```

- [ ] **Step 2: Create the Maven project**

Use group `com.yumg`, artifact `spring-boot-starter-template`, Java `21`, packaging `jar`, and Spring Boot `4.1.0`. Add starters for web, validation, data-jpa, security, oauth2-resource-server, actuator, cache, Liquibase, PostgreSQL, SQLite JDBC, Hibernate community dialects, springdoc OpenAPI, test, security-test, and Testcontainers PostgreSQL. Configure compiler parameter names, build-info generation, the Spring Boot plugin, and JaCoCo.

- [ ] **Step 3: Add the entry point and default configuration**

```java
@SpringBootApplication
@EnableMethodSecurity
@ConfigurationPropertiesScan
public class StarterApplication {
    public static void main(String[] args) {
        SpringApplication.run(StarterApplication.class, args);
    }
}
```

Set `spring.profiles.default=local`, UTC JSON serialization, graceful shutdown, `server.forward-headers-strategy=none`, and `spring.jpa.hibernate.ddl-auto=validate`.

- [ ] **Step 4: Run the smoke test**

Run: `./mvnw -q -Dtest=StarterApplicationTest test`

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

```bash
git add pom.xml .mvn mvnw mvnw.cmd .gitignore src
git commit -m "build: scaffold Spring Boot project"
```

### Task 2: Shared API Contracts, Errors, and Trace IDs

**Files:**
- Create: `src/main/java/com/yumg/starter/shared/api/ApiError.java`
- Create: `src/main/java/com/yumg/starter/shared/api/FieldViolation.java`
- Create: `src/main/java/com/yumg/starter/shared/api/ApiException.java`
- Create: `src/main/java/com/yumg/starter/shared/api/ApiExceptionHandler.java`
- Create: `src/main/java/com/yumg/starter/shared/api/PageResponse.java`
- Create: `src/main/java/com/yumg/starter/shared/web/TraceIdFilter.java`
- Test: `src/test/java/com/yumg/starter/shared/api/ApiExceptionHandlerTest.java`
- Test: `src/test/java/com/yumg/starter/shared/web/TraceIdFilterTest.java`

**Interfaces:**
- Produces: `ApiError(String code, String message, String traceId, List<FieldViolation> violations, Instant timestamp)`.
- Produces: `PageResponse<T>.from(Page<T>)`.
- Produces: response header `X-Trace-Id` and MDC key `traceId`.

- [ ] **Step 1: Write failing MVC tests**

Assert that invalid input returns status 400, code `VALIDATION_FAILED`, field violations, and a nonblank trace ID. Assert that an incoming syntactically valid `X-Trace-Id` is propagated and an invalid value is replaced.

- [ ] **Step 2: Implement the contracts**

```java
public record ApiError(
    String code,
    String message,
    String traceId,
    List<FieldViolation> violations,
    Instant timestamp
) {}

public record PageResponse<T>(List<T> items, int page, int size, long totalElements, int totalPages) {
    public static <T> PageResponse<T> from(Page<T> source) {
        return new PageResponse<>(source.getContent(), source.getNumber(), source.getSize(),
            source.getTotalElements(), source.getTotalPages());
    }
}
```

Map validation, malformed JSON, authentication, authorization, optimistic locking, not-found, conflict, rate-limit, and unexpected failures to stable codes. Do not expose stack traces or exception class names.

- [ ] **Step 3: Implement trace propagation**

Accept only `[A-Za-z0-9_-]{8,64}`. Otherwise generate a UUID without hyphens. Put it in MDC for the filter chain, return it as `X-Trace-Id`, and clear MDC in `finally`.

- [ ] **Step 4: Run shared tests**

Run: `./mvnw -q -Dtest='ApiExceptionHandlerTest,TraceIdFilterTest' test`

Expected: all tests pass.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/yumg/starter/shared src/test/java/com/yumg/starter/shared
git commit -m "feat: add shared API error and tracing contracts"
```

### Task 3: SQLite/PostgreSQL Persistence and Migrations

**Files:**
- Create: `src/main/resources/application-local.yml`
- Create: `src/main/resources/application-postgres.yml`
- Create: `src/main/resources/db/changelog/db.changelog-master.yaml`
- Create: `src/main/resources/db/changelog/changes/001-identity.yaml`
- Create: `src/main/resources/db/changelog/changes/002-system.yaml`
- Create: `src/main/resources/db/changelog/changes/003-announcement.yaml`
- Create: `src/main/java/com/yumg/starter/shared/persistence/BaseUuidEntity.java`
- Create: `src/main/java/com/yumg/starter/shared/persistence/AuditedEntity.java`
- Test: `src/test/java/com/yumg/starter/persistence/SqliteMigrationTest.java`
- Test: `src/test/java/com/yumg/starter/persistence/PostgresMigrationTest.java`

**Interfaces:**
- Produces: string-backed UUID IDs, `createdAt`, `updatedAt`, and `version` fields.
- Produces: all tables and indexes named in the design specification.

- [ ] **Step 1: Write migration startup tests**

Create one `@SpringBootTest` using a temporary SQLite file and one Testcontainers-backed PostgreSQL test. Query Liquibase's changelog table and assert that `users`, `system_settings`, and `announcements` exist.

- [ ] **Step 2: Configure both profiles**

For SQLite use `jdbc:sqlite:${APP_DATA_DIR:./data}/app.db`, `org.sqlite.JDBC`, `org.hibernate.community.dialect.SQLiteDialect`, a single-connection Hikari pool, and Liquibase enabled. For PostgreSQL use `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`, the PostgreSQL driver, and a bounded Hikari pool.

- [ ] **Step 3: Define base persistence types**

```java
@MappedSuperclass
public abstract class BaseUuidEntity {
    @Id @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @Version
    private long version;

    @PrePersist
    void assignId() {
        if (id == null) id = UUID.randomUUID().toString();
    }
}
```

`AuditedEntity` extends this type and adds UTC `createdAt` and `updatedAt` lifecycle timestamps.

- [ ] **Step 4: Write portable Liquibase changelogs**

Use `varchar(36)` IDs, `varchar` enum values, UTC-capable timestamps, explicit unique constraints, join-table composite unique constraints, and indexes for usernames, token hashes, permission codes, audit timestamps, IP rule lookup, and announcement publication queries.

- [ ] **Step 5: Run both migration tests**

Run: `./mvnw -q -Dtest='SqliteMigrationTest,PostgresMigrationTest' test`

Expected: both profiles start and validate the schema.

- [ ] **Step 6: Commit**

```bash
git add src/main/resources src/main/java/com/yumg/starter/shared/persistence src/test/java/com/yumg/starter/persistence
git commit -m "feat: add portable SQLite and PostgreSQL schema"
```

### Task 4: Users, Registration, and Password Policy

**Files:**
- Create: `src/main/java/com/yumg/starter/identity/domain/UserStatus.java`
- Create: `src/main/java/com/yumg/starter/identity/domain/User.java`
- Create: `src/main/java/com/yumg/starter/identity/infrastructure/UserRepository.java`
- Create: `src/main/java/com/yumg/starter/identity/api/dto/RegisterRequest.java`
- Create: `src/main/java/com/yumg/starter/identity/api/dto/UserResponse.java`
- Create: `src/main/java/com/yumg/starter/identity/application/UserService.java`
- Create: `src/main/java/com/yumg/starter/identity/api/AuthController.java`
- Create: `src/main/java/com/yumg/starter/bootstrap/PasswordConfiguration.java`
- Test: `src/test/java/com/yumg/starter/identity/UserRegistrationTest.java`

**Interfaces:**
- Produces: `UserService.register(RegisterRequest): UserResponse`.
- Produces: `POST /api/v1/auth/register`.

- [ ] **Step 1: Write registration tests**

Cover normalization to lowercase, duplicate username conflict, BCrypt-prefixed encoded password, default `ACTIVE` status, absence of password hash in JSON, and rejection of username/password/display-name constraint violations.

- [ ] **Step 2: Implement user domain and repository**

```java
public enum UserStatus { ACTIVE, LOCKED, DISABLED }

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByNormalizedUsername(String normalizedUsername);
    boolean existsByNormalizedUsername(String normalizedUsername);
}
```

Keep the credential fields encapsulated. Expose state-transition methods `lock()`, `unlock()`, `disable()`, `enable()`, and `changePasswordHash(String)` that enforce valid transitions.

- [ ] **Step 3: Implement registration**

Validate username with `[A-Za-z0-9_.-]{3,32}`, display name length 1-80, and password length 10-128. Normalize username with `Locale.ROOT`, encode through `PasswordEncoder`, persist, and return only safe fields.

- [ ] **Step 4: Run registration tests**

Run: `./mvnw -q -Dtest=UserRegistrationTest test`

Expected: all registration and validation cases pass.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/yumg/starter/identity src/main/java/com/yumg/starter/bootstrap/PasswordConfiguration.java src/test/java/com/yumg/starter/identity
git commit -m "feat: add user registration and password storage"
```

### Task 5: JWT Access Tokens and Rotating Refresh Sessions

**Files:**
- Create: `src/main/java/com/yumg/starter/bootstrap/JwtProperties.java`
- Create: `src/main/java/com/yumg/starter/bootstrap/SecurityConfiguration.java`
- Create: `src/main/java/com/yumg/starter/identity/domain/RefreshSession.java`
- Create: `src/main/java/com/yumg/starter/identity/infrastructure/RefreshSessionRepository.java`
- Create: `src/main/java/com/yumg/starter/identity/application/TokenService.java`
- Create: `src/main/java/com/yumg/starter/identity/application/AuthenticationService.java`
- Create: `src/main/java/com/yumg/starter/identity/api/dto/LoginRequest.java`
- Create: `src/main/java/com/yumg/starter/identity/api/dto/TokenResponse.java`
- Create: `src/main/java/com/yumg/starter/identity/api/dto/RefreshRequest.java`
- Modify: `src/main/java/com/yumg/starter/identity/api/AuthController.java`
- Test: `src/test/java/com/yumg/starter/identity/AuthenticationFlowTest.java`
- Test: `src/test/java/com/yumg/starter/identity/RefreshTokenRotationTest.java`

**Interfaces:**
- Produces: `AuthenticationService.login(LoginRequest, ClientContext): TokenResponse`.
- Produces: `AuthenticationService.refresh(String, ClientContext): TokenResponse`.
- Produces: login, refresh, logout-current, and logout-all endpoints.

- [ ] **Step 1: Write authentication and rotation tests**

Cover valid/invalid login, disabled/locked user denial, access JWT claims, refresh hash persistence, one-time rotation, explicit revocation, old-token reuse revoking the family, logout-current, logout-all, and password-change session revocation.

- [ ] **Step 2: Configure JWT encoding and decoding**

Use RSA SHA-256. Read PEM private/public keys from configured values or files. Permit ephemeral RSA generation only under `local` when `app.security.jwt.allow-ephemeral-key=true`. JWT claims include issuer, subject user ID, username, permissions, token version, issued time, expiry, and unique token ID.

- [ ] **Step 3: Implement refresh sessions**

Generate 32 random bytes with `SecureRandom`, encode Base64 URL-safe without padding, return the raw token once, and store `SHA-256(rawToken)` as lowercase hex. Persist `familyId`, `userId`, `expiresAt`, `consumedAt`, `revokedAt`, and sanitized client metadata.

- [ ] **Step 4: Implement rotation transaction**

Lock the matching session row for update. Reject expired/revoked tokens. If `consumedAt` is already set, revoke every session in the family and return `REFRESH_TOKEN_REUSED`. Otherwise mark consumed and create the successor before returning new access and refresh tokens.

- [ ] **Step 5: Run authentication tests**

Run: `./mvnw -q -Dtest='AuthenticationFlowTest,RefreshTokenRotationTest' test`

Expected: all token lifecycle cases pass.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/yumg/starter/bootstrap src/main/java/com/yumg/starter/identity src/test/java/com/yumg/starter/identity
git commit -m "feat: add JWT authentication and refresh rotation"
```

### Task 6: Extensible RBAC and User Administration

**Files:**
- Create: `src/main/java/com/yumg/starter/identity/domain/Role.java`
- Create: `src/main/java/com/yumg/starter/identity/domain/Permission.java`
- Create: `src/main/java/com/yumg/starter/identity/infrastructure/RoleRepository.java`
- Create: `src/main/java/com/yumg/starter/identity/infrastructure/PermissionRepository.java`
- Create: `src/main/java/com/yumg/starter/shared/security/CurrentPrincipal.java`
- Create: `src/main/java/com/yumg/starter/shared/security/PermissionEvaluator.java`
- Create: `src/main/java/com/yumg/starter/identity/application/RoleService.java`
- Create: `src/main/java/com/yumg/starter/identity/api/RoleController.java`
- Create: `src/main/java/com/yumg/starter/identity/api/UserController.java`
- Create: `src/main/java/com/yumg/starter/bootstrap/SecuritySeedInitializer.java`
- Test: `src/test/java/com/yumg/starter/identity/RbacIntegrationTest.java`
- Test: `src/test/java/com/yumg/starter/identity/UserAdministrationTest.java`

**Interfaces:**
- Produces: `PermissionEvaluator.has(String permissionCode): boolean` for SpEL use.
- Produces: CRUD APIs for roles and permissions, assignment APIs, paginated user listing, status operations, and forced session revocation.

- [ ] **Step 1: Write RBAC denial and assignment tests**

Assert anonymous 401, authenticated-without-permission 403, permission grant enabling access, permission removal disabling access on the next access token, idempotent seed execution, and `SUPER_ADMIN` centralized bypass.

- [ ] **Step 2: Implement role and permission entities**

Use unique immutable permission codes and unique role codes. Allow role display names and descriptions to change. Prevent deletion of a role while assigned unless assignments are explicitly removed in the same service transaction.

- [ ] **Step 3: Implement authorization projection**

At login/refresh, load the effective permission set and embed it in the access JWT. Convert each permission to a Spring authority and provide `@PreAuthorize("@permissionEvaluator.has('system:user:read')")` for protected methods.

- [ ] **Step 4: Implement admin APIs and seed data**

Seed the exact roles and permissions from the design idempotently. Protect role mutation, assignment, user listing, account state, and session revocation with the corresponding permission codes. Never return password hashes or refresh hashes.

- [ ] **Step 5: Run RBAC tests**

Run: `./mvnw -q -Dtest='RbacIntegrationTest,UserAdministrationTest' test`

Expected: authorization and administration cases pass.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/yumg/starter/identity src/main/java/com/yumg/starter/shared/security src/main/java/com/yumg/starter/bootstrap/SecuritySeedInitializer.java src/test/java/com/yumg/starter/identity
git commit -m "feat: add extensible RBAC and user administration"
```

### Task 7: Typed Runtime Settings and Audit Events

**Files:**
- Create: `src/main/java/com/yumg/starter/system/domain/SettingKey.java`
- Create: `src/main/java/com/yumg/starter/system/domain/SystemSetting.java`
- Create: `src/main/java/com/yumg/starter/system/domain/AuditEvent.java`
- Create: `src/main/java/com/yumg/starter/system/infrastructure/SystemSettingRepository.java`
- Create: `src/main/java/com/yumg/starter/system/infrastructure/AuditEventRepository.java`
- Create: `src/main/java/com/yumg/starter/system/application/RuntimeSettingService.java`
- Create: `src/main/java/com/yumg/starter/system/application/AuditService.java`
- Create: `src/main/java/com/yumg/starter/system/api/SystemSettingController.java`
- Create: `src/main/java/com/yumg/starter/system/api/AuditController.java`
- Test: `src/test/java/com/yumg/starter/system/RuntimeSettingServiceTest.java`
- Test: `src/test/java/com/yumg/starter/system/AuditIntegrationTest.java`

**Interfaces:**
- Produces: `RuntimeSettingService.get(SettingKey<T>): T`.
- Produces: `RuntimeSettingService.update(String key, JsonNode value, CurrentPrincipal actor)`.
- Produces: immutable `RuntimeSettingsSnapshot` atomically replaced after committed updates.

- [ ] **Step 1: Write typed setting tests**

Cover default values, database overrides, integer/boolean/duration/CIDR validation, out-of-range rejection, unknown-key rejection, atomic snapshot refresh, rollback preserving the prior snapshot, permission denial, and sanitized before/after audit metadata.

- [ ] **Step 2: Define the system setting registry**

Define keys for general rate capacity/window, brute-force enabled/failure threshold/window/lock duration, request logging enabled, and security auditing enabled. Each key declares Java type, default value, parser, validator, and whether changes take effect immediately.

- [ ] **Step 3: Implement transactional update and after-commit refresh**

Persist normalized JSON values and version them optimistically. Record an audit event in the same transaction. Register an after-commit callback that reloads and atomically swaps the immutable snapshot, so rolled-back values never reach memory.

- [ ] **Step 4: Implement APIs**

Expose listing and update endpoints under `/api/v1/system/settings` with `system:config:read` and `system:config:write`. Expose paginated audit search under `/api/v1/system/audit-events` with `system:audit:read`.

- [ ] **Step 5: Run setting and audit tests**

Run: `./mvnw -q -Dtest='RuntimeSettingServiceTest,AuditIntegrationTest' test`

Expected: validation, refresh, rollback, authorization, and audit cases pass.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/yumg/starter/system src/test/java/com/yumg/starter/system
git commit -m "feat: add typed runtime settings and audits"
```

### Task 8: Client IP, IP Rules, Rate Limiting, and Brute-Force Protection

**Files:**
- Create: `src/main/java/com/yumg/starter/bootstrap/TrustedProxyProperties.java`
- Create: `src/main/java/com/yumg/starter/shared/web/ClientIpResolver.java`
- Create: `src/main/java/com/yumg/starter/system/domain/IpAccessRule.java`
- Create: `src/main/java/com/yumg/starter/system/infrastructure/IpAccessRuleRepository.java`
- Create: `src/main/java/com/yumg/starter/system/application/IpAccessService.java`
- Create: `src/main/java/com/yumg/starter/system/application/RateLimitService.java`
- Create: `src/main/java/com/yumg/starter/system/application/BruteForceService.java`
- Create: `src/main/java/com/yumg/starter/system/api/IpAccessRuleController.java`
- Create: `src/main/java/com/yumg/starter/system/web/ServiceProtectionFilter.java`
- Test: `src/test/java/com/yumg/starter/shared/web/ClientIpResolverTest.java`
- Test: `src/test/java/com/yumg/starter/system/ServiceProtectionIntegrationTest.java`
- Test: `src/test/java/com/yumg/starter/system/BruteForceServiceTest.java`

**Interfaces:**
- Produces: `ClientIpResolver.resolve(HttpServletRequest): InetAddress`.
- Produces: `IpAccessService.evaluate(InetAddress, Instant): AccessDecision`.
- Produces: in-memory counter-store interfaces that can be replaced by Redis adapters.

- [ ] **Step 1: Write spoofing and protection tests**

Cover direct address selection, ignored forwarded headers from untrusted peers, trusted proxy chain walking, IPv4/IPv6 and CIDR normalization, active/expired deny rules, protected-admin allow rules, rate-limit 429 with `Retry-After`, login failure threshold, successful-login reset, and lock expiry.

- [ ] **Step 2: Implement trusted proxy resolution**

Parse configured trusted CIDRs at startup and fail fast on malformed entries. When the direct peer is trusted, walk `Forwarded`/`X-Forwarded-For` from right to left and select the first untrusted hop. Otherwise use the direct peer and ignore forwarded headers.

- [ ] **Step 3: Implement IP rules**

Support `DENY` and `ADMIN_ALLOW` rules with CIDR, optional expiry, reason, creator, and active status. Normalize networks before persistence and prefer the most specific matching rule. Record changes through `AuditService`.

- [ ] **Step 4: Implement local counters**

Use thread-safe bounded caches keyed by normalized IP for general rate limits and by normalized username plus IP for login failures. Read limits from `RuntimeSettingService` on evaluation. Clean expired entries and cap cache size to prevent memory exhaustion.

- [ ] **Step 5: Wire filters and authentication hooks**

Run IP and general rate protection before authentication. Record failed login attempts without revealing whether a username exists. Clear relevant counters after successful login. Return stable `IP_BLOCKED`, `RATE_LIMITED`, and `LOGIN_TEMPORARILY_LOCKED` errors.

- [ ] **Step 6: Run protection tests**

Run: `./mvnw -q -Dtest='ClientIpResolverTest,ServiceProtectionIntegrationTest,BruteForceServiceTest' test`

Expected: all proxy, IP, rate-limit, and brute-force cases pass.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/yumg/starter/bootstrap/TrustedProxyProperties.java src/main/java/com/yumg/starter/shared/web src/main/java/com/yumg/starter/system src/test/java/com/yumg/starter/shared/web src/test/java/com/yumg/starter/system
git commit -m "feat: add service access protections"
```

### Task 9: Announcement Example Module

**Files:**
- Create: `src/main/java/com/yumg/starter/example/domain/Announcement.java`
- Create: `src/main/java/com/yumg/starter/example/infrastructure/AnnouncementRepository.java`
- Create: `src/main/java/com/yumg/starter/example/application/AnnouncementService.java`
- Create: `src/main/java/com/yumg/starter/example/api/AnnouncementController.java`
- Create: `src/main/java/com/yumg/starter/example/api/dto/AnnouncementRequest.java`
- Create: `src/main/java/com/yumg/starter/example/api/dto/AnnouncementResponse.java`
- Test: `src/test/java/com/yumg/starter/example/AnnouncementAuthorizationTest.java`
- Test: `src/test/java/com/yumg/starter/example/AnnouncementConcurrencyTest.java`

**Interfaces:**
- Produces: public published listing and permission-protected create, update, publish/unpublish, and delete APIs.

- [ ] **Step 1: Write business and authorization tests**

Cover public listing excluding drafts, authorized creation, write denial, delete denial, validation, audit generation, publication transitions, and stale optimistic version returning 409 `OPTIMISTIC_LOCK_CONFLICT`.

- [ ] **Step 2: Implement announcement domain**

Store title, content, published flag, published-at timestamp, author ID, audit timestamps, and optimistic version. Enforce title length 1-160 and content length 1-20,000. Publishing sets the timestamp; unpublishing clears it.

- [ ] **Step 3: Implement service and APIs**

Keep query and mutation rules in `AnnouncementService`. Protect mutations with `example:announcement:write` or `example:announcement:delete`. Return DTOs rather than entities and emit audit events for mutations.

- [ ] **Step 4: Run announcement tests**

Run: `./mvnw -q -Dtest='AnnouncementAuthorizationTest,AnnouncementConcurrencyTest' test`

Expected: content, permission, audit, and concurrency cases pass.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/yumg/starter/example src/test/java/com/yumg/starter/example
git commit -m "feat: add RBAC-protected announcement example"
```

### Task 10: OpenAPI, Logging, Health, and Version Information

**Files:**
- Create: `src/main/java/com/yumg/starter/bootstrap/OpenApiConfiguration.java`
- Create: `src/main/java/com/yumg/starter/system/api/SystemInfoController.java`
- Create: `src/main/java/com/yumg/starter/system/application/RequestLogSanitizer.java`
- Create: `src/main/java/com/yumg/starter/system/web/RequestLoggingFilter.java`
- Create: `src/main/resources/logback-spring.xml`
- Modify: `src/main/resources/application.yml`
- Modify: all controllers to add operation, response, and security documentation.
- Test: `src/test/java/com/yumg/starter/system/ObservabilityTest.java`
- Test: `src/test/java/com/yumg/starter/OpenApiContractTest.java`

**Interfaces:**
- Produces: `/api/v1/system/info`, `/actuator/health/liveness`, `/actuator/health/readiness`, `/v3/api-docs`, and configurable Swagger UI.

- [ ] **Step 1: Write observability and OpenAPI tests**

Assert health endpoints are public but sensitive actuator endpoints are denied; system info contains version/build/commit fields; OpenAPI declares bearer JWT security and every controller route; generated docs contain validation schemas and common error responses.

- [ ] **Step 2: Configure safe structured logging**

Emit JSON-compatible key/value fields for timestamp, level, logger, trace ID, request method/path, status, duration, and user ID. Redact `Authorization`, cookies, passwords, raw tokens, secret keys, and configured sensitive JSON fields. Do not log request bodies by default.

- [ ] **Step 3: Add build and Git metadata**

Generate Spring Boot build info and Git properties when available. Return unknown values as `null`, not fake values. Expose only non-sensitive info.

- [ ] **Step 4: Document APIs**

Configure bearer authentication globally and explicitly mark public routes. Add endpoint summaries, permissions, examples, response schemas, and stable error codes. Group identity, system, and example APIs.

- [ ] **Step 5: Run documentation and observability tests**

Run: `./mvnw -q -Dtest='ObservabilityTest,OpenApiContractTest' test`

Expected: security, metadata, redaction, and API contract checks pass.

- [ ] **Step 6: Commit**

```bash
git add pom.xml src/main/java src/main/resources src/test/java
git commit -m "feat: add API documentation and observability"
```

### Task 11: Initial Administrator and Operational Configuration

**Files:**
- Create: `src/main/java/com/yumg/starter/bootstrap/BootstrapAdminProperties.java`
- Create: `src/main/java/com/yumg/starter/bootstrap/BootstrapAdminInitializer.java`
- Create: `.env.example`
- Test: `src/test/java/com/yumg/starter/bootstrap/BootstrapAdminInitializerTest.java`

**Interfaces:**
- Produces: idempotent first-run super-admin creation from `APP_BOOTSTRAP_ADMIN_USERNAME`, `APP_BOOTSTRAP_ADMIN_DISPLAY_NAME`, and `APP_BOOTSTRAP_ADMIN_PASSWORD`.

- [ ] **Step 1: Write bootstrap tests**

Cover first creation, repeated startup, no plaintext secret logging, existing super-admin behavior, local optional bootstrap, and production startup failure when no super-admin exists and required credentials are absent.

- [ ] **Step 2: Implement bootstrap policy**

Run after migrations and security seed data. Create exactly one configured user, encode the password, assign `SUPER_ADMIN`, and log only the username and success. Clear any retained password character array after use where practical.

- [ ] **Step 3: Add environment contract**

Document every environment variable in `.env.example` with non-secret example placeholders, safe local values, and comments identifying mandatory production values. Keep actual `.env` ignored.

- [ ] **Step 4: Run bootstrap tests**

Run: `./mvnw -q -Dtest=BootstrapAdminInitializerTest test`

Expected: initialization and fail-fast behavior pass.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/yumg/starter/bootstrap src/test/java/com/yumg/starter/bootstrap .env.example .gitignore
git commit -m "feat: add secure first-run administrator bootstrap"
```

### Task 12: Container, Compose, and GitHub Actions

**Files:**
- Create: `Dockerfile`
- Create: `.dockerignore`
- Create: `compose.yaml`
- Create: `.github/workflows/ci.yml`
- Create: `.github/workflows/image.yml`
- Test: `src/test/java/com/yumg/starter/ArchitectureRulesTest.java`

**Interfaces:**
- Produces: non-root Java 21 runtime image, PostgreSQL Compose environment, PR/push CI, and GHCR publishing on main/tags.

- [ ] **Step 1: Add architecture and secret-safety test**

Use ArchUnit or package assertions to prevent controllers from accessing repositories outside their module and to keep business packages out of `shared`. Add a repository scan script step that rejects private-key blocks, raw JWT secrets, and populated password assignments in tracked configuration.

- [ ] **Step 2: Write the multi-stage Dockerfile**

Build with a Temurin 21 JDK image and Maven Wrapper cache mounts, then copy only the executable JAR into a Temurin 21 JRE image. Create an unprivileged user, own `/app/data`, run with `USER`, expose 8080, configure a Java-based health check, and use `ENTRYPOINT ["java","-jar","/app/app.jar"]`.

- [ ] **Step 3: Write PostgreSQL Compose**

Define PostgreSQL with a health check and named volume; define the application with profile `postgres`, environment substitutions, dependency on healthy PostgreSQL, application health check, and no embedded secrets beyond explicit local-only defaults.

- [ ] **Step 4: Write CI workflow**

Use least-privilege read permissions, Temurin 21, Maven dependency caching, `./mvnw verify`, test report upload on failure, and concurrency cancellation. Ensure untrusted PRs receive no publishing token.

- [ ] **Step 5: Write image workflow**

On main and `v*` tags, grant only `contents: read`, `packages: write`, and required security-event permission. Build with BuildKit, scan with Trivy, fail on unfixed critical vulnerabilities unless explicitly allowlisted with rationale, and publish semantic/ref tags to GHCR.

- [ ] **Step 6: Validate assets**

Run: `./mvnw -q -Dtest=ArchitectureRulesTest test`

Run when Docker is available: `docker compose config && docker build -t spring-boot-starter-template:test .`

Expected: architecture test passes, Compose resolves, image builds, and its configured user is non-root.

- [ ] **Step 7: Commit**

```bash
git add Dockerfile .dockerignore compose.yaml .github src/test/java/com/yumg/starter/ArchitectureRulesTest.java
git commit -m "ci: add containers and GitHub delivery workflows"
```

### Task 13: Documentation and Full Verification

**Files:**
- Create: `README.md`
- Create: `docs/api/authentication.md`
- Create: `docs/api/rbac.md`
- Create: `docs/operations/runtime-settings.md`
- Create: `docs/operations/deployment.md`
- Modify: any source or test file needed to correct verification findings.

**Interfaces:**
- Produces: a complete onboarding and operations guide with copy-paste commands and a verified starter repository.

- [ ] **Step 1: Write README quick start**

Document prerequisites, SQLite startup, first-admin creation, Swagger URL, login/refresh examples, PostgreSQL Compose startup, tests, image build, environment variables, package structure, extension rules, and explicit non-goals.

- [ ] **Step 2: Write focused guides**

Authentication guide: token lifetimes, RSA key configuration, rotation, logout, reuse response, and client storage guidance.

RBAC guide: role/permission data model, seed values, method-security examples, and adding a new module permission.

Runtime settings guide: ownership boundary, typed keys, update/audit flow, local-counter limitation, and Redis adapter extension point.

Deployment guide: PostgreSQL requirements, secrets, trusted proxies, CORS, health probes, GHCR publishing, database backup, Liquibase rollout, and generic CD extension points.

- [ ] **Step 3: Run the complete verification suite**

Run: `./mvnw clean verify`

Expected: `BUILD SUCCESS`, all unit/integration tests pass, and JaCoCo report is generated.

- [ ] **Step 4: Verify both runnable profiles**

Run local: `APP_BOOTSTRAP_ADMIN_USERNAME=admin APP_BOOTSTRAP_ADMIN_DISPLAY_NAME=Administrator APP_BOOTSTRAP_ADMIN_PASSWORD='LocalPassword123!' ./mvnw spring-boot:run`

Expected: SQLite migrations complete, application becomes ready, health returns `UP`, and OpenAPI loads.

Run PostgreSQL when Docker is available: `docker compose up --build --wait`

Expected: database and application report healthy; authentication and announcement smoke requests succeed.

- [ ] **Step 5: Scan repository safety and cleanliness**

Run: `git grep -nE 'BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY|password:[[:space:]]+[^$<{]' -- ':!docs/**' ':!.env.example' || true`

Run: `git status --short`

Expected: no private keys or populated production passwords; working tree clean after final commit.

- [ ] **Step 6: Commit documentation and final fixes**

```bash
git add README.md docs src pom.xml Dockerfile compose.yaml .github .env.example
git commit -m "docs: complete starter usage and operations guide"
```

## Plan Self-Review

- Every design section maps to at least one task: architecture (1/6/12), databases (3), authentication (4/5), authorization (6), runtime settings (7), IP/rate/brute-force controls (8), example module (9), observability/API/versioning (10), bootstrap (11), containers/CI (12), and documentation/full verification (13).
- Types and names used across tasks are consistent: `RuntimeSettingService`, `AuditService`, `PermissionEvaluator`, `ClientIpResolver`, `AuthenticationService`, and `AnnouncementService` each have one owning task.
- The plan contains no unresolved design decisions. Environment-dependent Docker checks are explicitly conditional because the current execution environment does not provide Docker.
