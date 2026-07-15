# 动态简历模块 Implementation Plan

> For agentic workers: REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** 让 yu-resume 的全部业务内容由后端单份动态文档提供，并让超级管理员通过现有后台管理该文档。

**Architecture:** 后端新增单例 ResumeDocument 聚合，使用 JSON 文本存储可扩展的简历内容；公开接口只读，管理接口受 resume:manage 保护。后台管理端编辑同一份文档并携带乐观锁版本；Nuxt 页面按 sections 类型渲染 API 数据。

**Tech Stack:** Java 21、Spring Boot 4、Spring Security、JPA、Liquibase、Vue 3、Element Plus、Nuxt 4、TypeScript、pnpm。

## Global Constraints

- 仅维护一份简历，不增加用户、简历列表或所有权字段。
- 所有简历业务文本与链接来自 content JSON；不得保留硬编码回退简历。
- 管理权限固定为 resume:manage，且仅启动时授予 SUPER_ADMIN。
- 公开端使用 NUXT_PUBLIC_RESUME_API_BASE，默认 http://localhost:8848/api/v1。
- 保留 yu-resume 当前未提交改动，只作最小必要的定点修改。

---

### Task 1: 建立可迁移的单例简历文档

**Files:**
- Create: src/main/resources/db/changelog/changes/012-resume-document.yaml
- Modify: src/main/resources/db/changelog/db.changelog-master.yaml
- Create: src/main/java/com/yumg/starter/entities/ResumeDocument.java
- Create: src/main/java/com/yumg/starter/modules/resume/infrastructure/ResumeDocumentRepository.java
- Create: src/test/java/com/yumg/starter/persistence/ResumeDocumentMigrationTest.java

**Interfaces:**
- Produces: ResumeDocument(String content, int schemaVersion), getters, and replace(String content, int schemaVersion).
- Produces: Optional<ResumeDocument> findFirstByOrderByCreatedAtAsc().

- [ ] **Step 1: Write the failing migration test**

Create a SQLite Spring Boot test asserting resume_documents has id, content, schema_version, version, created_at, and updated_at:

    @SpringBootTest(properties = "app.jwt.allow-ephemeral-key=true")
    class ResumeDocumentMigrationTest {
        @Autowired JdbcTemplate jdbc;
        @Test void createsResumeDocumentTableWithVersionedJsonContent() {
            assertThat(jdbc.queryForObject("select count(*) from resume_documents", Integer.class)).isZero();
            assertThat(jdbc.queryForList("pragma table_info(resume_documents)").stream()
                .map(row -> row.get("name"))).contains("id", "content", "schema_version", "version", "created_at", "updated_at");
        }
    }

- [ ] **Step 2: Run the test to verify it fails**

Run: ./mvnw -Dtest=ResumeDocumentMigrationTest test

Expected: FAIL because the table does not exist.

- [ ] **Step 3: Implement the persistence layer**

Add Liquibase change 012 after 011. Create a table with primary-key varchar(36) id, non-null text content, non-null integer schema_version default 1, non-null bigint version default 0, and audit timestamps. Implement the JPA entity by extending AuditedEntity. The repository must be:

    public interface ResumeDocumentRepository extends JpaRepository<ResumeDocument, String> {
        Optional<ResumeDocument> findFirstByOrderByCreatedAtAsc();
    }

- [ ] **Step 4: Verify green**

Run: ./mvnw -Dtest=ResumeDocumentMigrationTest test

Expected: PASS.

- [ ] **Step 5: Commit**

    git add src/main/resources/db/changelog src/main/java/com/yumg/starter/entities/ResumeDocument.java src/main/java/com/yumg/starter/modules/resume/infrastructure src/test/java/com/yumg/starter/persistence/ResumeDocumentMigrationTest.java
    git commit -m "feat: add resume document persistence"

### Task 2: 实现可扩展 JSON 文档服务与默认内容

**Files:**
- Create: src/main/java/com/yumg/starter/modules/resume/application/ResumeDocumentContent.java
- Create: src/main/java/com/yumg/starter/modules/resume/application/ResumeDocumentValidator.java
- Create: src/main/java/com/yumg/starter/modules/resume/application/ResumeService.java
- Create: src/main/java/com/yumg/starter/modules/resume/application/ResumeBootstrap.java
- Create: src/test/java/com/yumg/starter/modules/resume/application/ResumeServiceTest.java

**Interfaces:**
- Produces: record ResumeDocumentContent(String content, int schemaVersion, long version).
- Produces: publicDocument(), managedDocument(), and update(String content, int schemaVersion, long version).

- [ ] **Step 1: Write failing service tests**

Use mock repository/audit dependencies and a real ObjectMapper. Cover initialization, invalid profile, unsupported section, audit record and version conflict:

    createsTheCurrentResumeAsDefaultWhenNoDocumentExists()
    rejectsContentWithoutProfileNameAndSections()
    rejectsUnknownSectionTypeWithoutCustomPayload()
    updateWritesRESUME_UPDATEDAuditEvent()
    rejectsAStaleDocumentVersion()

The default JSON must contain profile.name, profile.contacts, and bullet-list, timeline, and projects sections matching the current public page.

- [ ] **Step 2: Verify red**

Run: ./mvnw -Dtest=ResumeServiceTest test

Expected: FAIL because the resume application service does not exist.

- [ ] **Step 3: Implement service and validation**

Validate a JSON object with non-blank profile.name, contact label/text, and non-empty sections. Every section requires non-blank id, type, title and an items array. Allow bullet-list, timeline, projects, custom; require an object payload for custom. Seed the current resume data only if the repository has no document. Reject stale updates and emit RESUME_UPDATED audit after save:

    if (document.getVersion() != version) {
        throw ApiException.conflict("简历内容已被其他管理员更新，请重新加载后再保存");
    }

- [ ] **Step 4: Verify green**

Run: ./mvnw -Dtest=ResumeServiceTest test

Expected: PASS.

- [ ] **Step 5: Commit**

    git add src/main/java/com/yumg/starter/modules/resume src/test/java/com/yumg/starter/modules/resume
    git commit -m "feat: add dynamic resume service"

### Task 3: 公开管理接口、权限和菜单

**Files:**
- Create: src/main/java/com/yumg/starter/modules/resume/api/dto/ResumeDocumentRequest.java
- Create: src/main/java/com/yumg/starter/modules/resume/api/dto/ResumeDocumentResponse.java
- Create: src/main/java/com/yumg/starter/modules/resume/api/ResumeController.java
- Modify: src/main/java/com/yumg/starter/modules/rbac/application/RbacBootstrap.java
- Modify: src/main/java/com/yumg/starter/modules/navigation/application/NavigationBootstrap.java
- Create: src/test/java/com/yumg/starter/modules/resume/api/ResumeControllerTest.java
- Modify: src/test/java/com/yumg/starter/modules/rbac/application/RbacBootstrapTest.java

**Interfaces:**
- Produces: GET /api/v1/resume, GET /api/v1/resume/manage, PUT /api/v1/resume/manage.
- Produces: resume:manage (管理公开简历的全部内容) and route /content/resume with key resume-management.

- [ ] **Step 1: Write failing security/controller tests**

Assert anonymous public GET returns $.data.content; anonymous management PUT is denied; management PUT with resume:manage succeeds. Extend RBAC bootstrap tests to assert this permission gets seeded and ADMIN does not receive it.

- [ ] **Step 2: Verify red**

Run: ./mvnw -Dtest=ResumeControllerTest,RbacBootstrapTest test

Expected: FAIL because the controller and permission do not exist.

- [ ] **Step 3: Implement API, RBAC and navigation**

Use request fields content, schemaVersion, version with validation. Add public GET with PublicApi; protect both management mappings with PreAuthorize hasAuthority resume:manage. Add the RBAC seed and this navigation call:

    seedPage(content, "resume-management", "简历管理", "/content/resume",
        "resume-management", "Document", 20, "resume:manage");

- [ ] **Step 4: Verify green**

Run: ./mvnw -Dtest=ResumeControllerTest,RbacBootstrapTest test

Expected: PASS.

- [ ] **Step 5: Commit**

    git add src/main/java/com/yumg/starter/modules/resume/api src/main/java/com/yumg/starter/modules/rbac/application/RbacBootstrap.java src/main/java/com/yumg/starter/modules/navigation/application/NavigationBootstrap.java src/test/java/com/yumg/starter/modules/resume src/test/java/com/yumg/starter/modules/rbac
    git commit -m "feat: expose protected resume management APIs"

### Task 4: 后台 Element Plus 编辑器与动态路由

**Files:**
- Create: /Users/yaopinxin/project/GitHub/springboot-admin/src/api/resume.ts
- Create: /Users/yaopinxin/project/GitHub/springboot-admin/src/views/content/resume/contracts.mjs
- Create: /Users/yaopinxin/project/GitHub/springboot-admin/src/views/content/resume/index.vue
- Modify: /Users/yaopinxin/project/GitHub/springboot-admin/src/api/navigation.ts
- Modify: /Users/yaopinxin/project/GitHub/springboot-admin/src/api/routes.ts
- Modify: /Users/yaopinxin/project/GitHub/springboot-admin/src/router/utils.ts
- Modify: /Users/yaopinxin/project/GitHub/springboot-admin/src/views/system/menu/index.vue
- Modify: /Users/yaopinxin/project/GitHub/springboot-admin/src/views/system/admin-code-presentation.mjs

**Interfaces:**
- Produces: getManagedResume(), updateManagedResume(document), parseResumeDocument(content), and toSavePayload(document).
- Produces: route component key resume-management.

- [ ] **Step 1: Write failing document-contract test**

Create a dependency-free Node test for API helpers. It must preserve contacts, known sections, a custom section, schema version and document version through toSavePayload; it must reject a parsed document lacking sections.

- [ ] **Step 2: Verify red**

Run: node src/views/content/resume/contracts.mjs in springboot-admin.

Expected: FAIL because helpers are absent.

- [ ] **Step 3: Implement API and editor**

Define discriminated TypeScript section types. The editor has form controls for SEO, profile, contacts, section add/delete/up/down, bullet list, timeline, projects, and custom JSON. Parse custom JSON before save and block invalid content. On stale-version errors retain edits and show “内容已被其他管理员更新，请重新加载后再保存”. Add resume-management to both API component-key unions, lazy route registry, menu selector, and Chinese component label mapping.

- [ ] **Step 4: Verify green**

Run: node src/views/content/resume/contracts.mjs && pnpm typecheck in springboot-admin.

Expected: both exit 0.

- [ ] **Step 5: Commit**

    cd /Users/yaopinxin/project/GitHub/springboot-admin
    git add src/api/resume.ts src/views/content/resume src/api/navigation.ts src/api/routes.ts src/router/utils.ts src/views/system/menu/index.vue src/views/system/admin-code-presentation.mjs
    git commit -m "feat: add resume management page"

### Task 5: 修复 Nuxt 启动并使公开页动态渲染

**Files:**
- Modify: /Users/yaopinxin/project/GitHub/yu-resume/package.json
- Modify: /Users/yaopinxin/project/GitHub/yu-resume/nuxt.config.ts
- Create: /Users/yaopinxin/project/GitHub/yu-resume/app/types/resume.ts
- Create: /Users/yaopinxin/project/GitHub/yu-resume/app/composables/useResume.ts
- Create: /Users/yaopinxin/project/GitHub/yu-resume/app/utils/resume-contract.mjs
- Modify: /Users/yaopinxin/project/GitHub/yu-resume/app/app.vue
- Modify: /Users/yaopinxin/project/GitHub/yu-resume/README.md

**Interfaces:**
- Consumes: public GET {NUXT_PUBLIC_RESUME_API_BASE}/resume.
- Produces: useResume() returning resume, pending, error, refresh.

- [ ] **Step 1: Write failing public document contract test**

Create a Node test accepting a valid API envelope and rejecting malformed content and unknown non-custom section types. Add test:contracts script.

- [ ] **Step 2: Verify red**

Run: pnpm test:contracts in yu-resume.

Expected: FAIL because parse helper and script are absent.

- [ ] **Step 3: Implement cross-platform API-driven page**

Replace scripts with nuxt dev --open, nuxt build, nuxt generate, and nuxt preview. Set public runtime config from NUXT_PUBLIC_RESUME_API_BASE or http://localhost:8848/api/v1. Fetch only the public endpoint, parse the document, render profile contacts plus bullet-list, timeline and projects through current components, set SEO from data, and show loading/API-error states. Render only safe known-shape custom sections and do not preserve static resume fallback. Update README with variable and startup steps.

- [ ] **Step 4: Verify green**

Run: pnpm test:contracts && pnpm build && pnpm generate in yu-resume.

Expected: all exit 0 on macOS.

- [ ] **Step 5: Commit**

    cd /Users/yaopinxin/project/GitHub/yu-resume
    git add package.json nuxt.config.ts app README.md
    git commit -m "feat: load resume content from backend"

### Task 6: 完整验证与交付

**Files:**
- Modify only if an actual verification failure needs a focused correction.

- [ ] **Step 1: Run backend suite**

Run: ./mvnw test in springboot-start.

Expected: Maven exits 0, including migration and architecture tests.

- [ ] **Step 2: Run both frontend builds**

Run: pnpm typecheck && pnpm build in springboot-admin.

Run: pnpm test:contracts && pnpm build && pnpm generate in yu-resume.

Expected: every command exits 0.

- [ ] **Step 3: API and UI smoke test**

Verify anonymous GET /api/v1/resume returns data.content; anonymous management GET is denied; a SUPER_ADMIN can load and save management content; a non-super-admin gets 403. Edit a visible profile value in the admin and verify a refreshed public Nuxt page shows it; restore the default value afterward.

- [ ] **Step 4: Final evidence**

Run git diff --check and git status --short in all three repositories. Report only fresh test output and exact commit hashes.
