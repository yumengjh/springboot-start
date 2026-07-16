# Backend Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Strengthen local administrative safety, request-path performance, GC observability, and management-list scalability.

**Architecture:** Keep existing module boundaries. SQL classification remains in `sqlconsole`; an immutable IP decision snapshot belongs to `security`; scheduler status remains in `maintenance`; pagination stays in the announcements application/API layer.

**Tech Stack:** Java 21, Spring Boot 4.1, Spring Security, Spring Data JPA, Liquibase, JUnit 5, Mockito.

## Global Constraints

- SQL console requires local profile, explicit enablement, RBAC permission, audit event, and no raw SQL in audit metadata.
- IP decisions must perform no repository query in the request filter.
- Existing public API response envelope and error codes remain unchanged.
- Execute `./mvnw test` after all tasks.

### Task 1: Conservative SQL console classification

**Files:** `SqlConsoleService.java`, `SqlConsoleServiceTest.java`.

- [x] Add failing tests that `WITH ... DELETE` and `EXPLAIN ANALYZE DELETE` require confirmation while `SELECT` does not.
- [x] Replace regex-only classification with leading-keyword parsing that rejects write keywords in CTE/EXPLAIN statements from the read-only path.
- [x] Run `./mvnw -Dtest=SqlConsoleServiceTest test`.

### Task 2: IP rule decision snapshot

**Files:** `IpAccessRuleService.java`, `IpAccessFilter.java`, `IpAccessRuleServiceTest.java`.

- [x] Add a failing service test proving a decision uses one immutable snapshot and refreshes after rule mutation.
- [x] Add `IpRuleSnapshot` with active allow/deny networks; rebuild it on startup/create/delete; expose one `decision(ip, now)` method.
- [x] Replace three filter calls (`denies`, `hasActiveAllowRules`, `allows`) with one decision lookup.
- [x] Run targeted tests.

### Task 3: GC scheduler status and health

**Files:** `GcScheduler.java`, `GcScheduleContent.java`, GC controller, new health indicator, scheduler tests.

- [x] Add failing tests for a failed scheduled run increasing the failure count and a succeeding run clearing it.
- [x] Persist scheduler state in-memory, emit WARN logs on failure, return state from `/system/gc/schedule`, and register an Actuator health indicator.
- [x] Run targeted tests.

### Task 4: Production defaults and announcement pagination

**Files:** profile YAML, `AnnouncementService.java`, announcement controller/repository/DTO tests.

- [x] Add failing pagination boundary tests for announcements.
- [x] Disable Springdoc and OSIV by default; enable Springdoc explicitly in local profile. Add page/size validation and `PageResponse` for managed announcements without changing public announcement endpoints.
- [x] Run `./mvnw test` and `git diff --check`.
