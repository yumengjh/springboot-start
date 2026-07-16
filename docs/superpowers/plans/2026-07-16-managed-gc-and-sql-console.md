# Managed GC and Local SQL Console Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add extensible, policy-driven data cleanup and a local-only audited SQL console.

**Architecture:** `maintenance` owns resource registration, persisted policies, runs and locking. Resources remain in the modules that own their data. SQL execution is a separately guarded local development service and never acts as a production endpoint.

**Tech Stack:** Spring Boot 4, Spring Data JPA, Liquibase, SQLite/PostgreSQL, Spring Security, Element Plus.

## Global Constraints

- SQL execution requires local Profile, explicit enablement and `system:sql:execute`.
- GC works with registered resources, never arbitrary table names.
- Every new behavior starts with a failing test.

### Task 1: Persistence and permissions

**Files:** Liquibase 014 migration, maintenance entities/repositories, RBAC bootstrap, migration tests.

- [x] Add the failing migration assertions for GC tables and Refresh Session indexes; run the migration test and observe failure.
- [x] Create policy/run/lock persistence and indexes; add GC and SQL permissions to the super administrator seed.
- [x] Re-run SQLite migration tests. PostgreSQL coverage remains CI-only because this machine has no Docker test runtime.

### Task 2: Extensible GC core

**Files:** `modules/maintenance/application`, session/audit repositories, rate-limit/brute-force services and unit tests.

- [ ] Add failing tests for registered default policies, dry run, expired in-memory state cleanup and single-run locking.
- [x] Implement the `GcResource` contract, registered resources, policy service, execution service and scheduler.
- [ ] Run the focused unit tests, then the complete backend test suite.

### Task 3: GC management API

**Files:** maintenance controller/DTOs and MVC tests.

- [ ] Add failing MVC tests for read/write permission denial, policy update, dry-run and run history.
- [x] Implement `/api/v1/system/gc` endpoints and audit events.
- [x] Run focused tests and verify response envelopes through the full backend suite.

### Task 4: Local SQL console

**Files:** sql-console API/application/DTOs, local configuration and tests.

- [ ] Add failing tests for local+flag gating, multi-statement rejection, dangerous-operation confirmation and masked result columns.
- [x] Implement one-statement JDBC execution with row limits, result masking and audit events.
- [x] Run focused tests and the complete backend suite.

### Task 5: Admin integration and documentation

**Files:** admin API client, GC/SQL pages, route registry, navigation bootstrap, contract tests and API reference.

- [ ] Add frontend contract tests that fail without policy and SQL console management pages.
- [x] Implement pages behind permission controls and seed navigation entries.
- [x] Run type check/build and full backend tests. PostgreSQL migration execution remains CI-only.
