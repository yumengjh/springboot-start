# Resume Feedback Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add immediate emotion reactions plus separate rating-and-suggestion feedback to the public resume, with protected review tools in the existing admin page.

**Architecture:** Persist feedback independently of the singleton resume JSON document. Public endpoints create either a reaction or a review; protected endpoints list and transition feedback status. The Nuxt page exposes two deliberately separate entry points, and the existing admin resume view gains a feedback tab that consumes the protected endpoint.

**Tech Stack:** Spring Boot, Spring Data JPA, Liquibase, Jakarta Validation, Vue 3, Nuxt 4, Element Plus, TypeScript.

## Global Constraints

- Public feedback is anonymous; persist IP and User-Agent but only expose them through `resume:manage` management APIs.
- Reactions submit immediately; reviews require a rating from 1 through 5 and may include at most 1,000 plain-text characters.
- Keep feedback independent from `resume_documents` and its optimistic-lock version.
- Management access continues to use only `resume:manage`.
- Preserve existing uncommitted edits in `/Users/yaopinxin/project/GitHub/yu-resume`.

---

### Task 1: Feedback persistence and application service

**Files:**
- Create: `src/main/resources/db/changelog/changes/013-resume-feedback.yaml`
- Modify: `src/main/resources/db/changelog/db.changelog-master.yaml`
- Create: `src/main/java/com/yumg/starter/entities/ResumeFeedback.java`
- Create: `src/main/java/com/yumg/starter/modules/resume/infrastructure/ResumeFeedbackRepository.java`
- Create: `src/main/java/com/yumg/starter/modules/resume/application/ResumeFeedbackService.java`
- Create: `src/main/java/com/yumg/starter/modules/resume/application/ResumeFeedbackStatus.java`
- Create: `src/main/java/com/yumg/starter/modules/resume/application/ResumeFeedbackType.java`
- Test: `src/test/java/com/yumg/starter/persistence/SqliteMigrationTest.java`
- Test: `src/test/java/com/yumg/starter/modules/resume/application/ResumeFeedbackServiceTest.java`

**Interfaces:**
- Produces `submitReaction(ResumeReaction, String ip, String userAgent)`, `submitReview(int rating, String suggestion, String ip, String userAgent)`, `list(...)`, and `updateStatus(String id, ResumeFeedbackStatus status)`.
- `REACTION` records contain only a five-value reaction; `REVIEW` records contain rating and optional suggestion.

- [ ] **Step 1: Write the failing persistence and service tests**

```java
@Test void createsResumeFeedbackTable() {
    assertThat(tableNames()).contains("resume_feedbacks");
}

@Test void submitsAReactionWithAnonymousRequestMetadata() {
    var saved = service.submitReaction(ResumeReaction.GREAT, "203.0.113.9", "test-agent");
    assertThat(saved.type()).isEqualTo(ResumeFeedbackType.REACTION);
    assertThat(saved.reaction()).isEqualTo(ResumeReaction.GREAT);
}

@Test void rejectsReviewWithoutRatingOrSuggestionAboveLimit() {
    assertThatThrownBy(() -> service.submitReview(0, "", "203.0.113.9", "agent"));
    assertThatThrownBy(() -> service.submitReview(5, "x".repeat(1001), "203.0.113.9", "agent"));
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./mvnw -Dtest=SqliteMigrationTest,ResumeFeedbackServiceTest test`

Expected: FAIL because feedback persistence and service classes do not exist.

- [ ] **Step 3: Implement persistence and service**

Create `resume_feedbacks` with UUID id, `type`, nullable `reaction`, nullable `rating`, nullable text `suggestion`, `ip_address`, `user_agent`, `status`, and audited timestamps. Use a repository ordered by `createdAt` descending. Validate the type-specific fields in the service, set every new record to `UNREAD`, enforce the reaction/review IP windows, and emit `RESUME_FEEDBACK_RECEIVED` audit events.

- [ ] **Step 4: Run the tests to verify they pass**

Run: `./mvnw -Dtest=SqliteMigrationTest,ResumeFeedbackServiceTest test`

Expected: PASS.

### Task 2: Public and protected feedback API

**Files:**
- Create: `src/main/java/com/yumg/starter/modules/resume/api/dto/ResumeReactionRequest.java`
- Create: `src/main/java/com/yumg/starter/modules/resume/api/dto/ResumeReviewRequest.java`
- Create: `src/main/java/com/yumg/starter/modules/resume/api/dto/ResumeFeedbackResponse.java`
- Create: `src/main/java/com/yumg/starter/modules/resume/api/dto/ResumeFeedbackStatusRequest.java`
- Modify: `src/main/java/com/yumg/starter/modules/resume/api/ResumeController.java`
- Test: `src/test/java/com/yumg/starter/modules/resume/api/ResumeControllerTest.java`

**Interfaces:**
- Produces public `POST /api/v1/resume/feedback/reactions` and `POST /api/v1/resume/feedback/reviews`.
- Produces protected `GET /api/v1/resume/feedback` and `PATCH /api/v1/resume/feedback/{id}/status`.

- [ ] **Step 1: Write the failing controller tests**

```java
@Test void permitsAnonymousReactionAndReviewSubmission() throws Exception {
    mvc.perform(post("/api/v1/resume/feedback/reactions").contentType(APPLICATION_JSON)
        .content("{\"reaction\":\"GREAT\"}")).andExpect(status().isOk());
}

@Test void protectsFeedbackManagementEndpoints() throws Exception {
    assertThat(preAuthorize(ResumeController.class.getMethod("feedback", int.class, int.class, String.class, String.class, Integer.class)))
        .isEqualTo("hasAuthority('resume:manage')");
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./mvnw -Dtest=ResumeControllerTest test`

Expected: FAIL because the mappings and DTOs do not exist.

- [ ] **Step 3: Implement DTO validation and endpoints**

Read the request IP from the existing trusted client-address helper if present, otherwise `HttpServletRequest.getRemoteAddr()`. Mark both create endpoints with `@PublicApi`; protect only list/status mappings with `@PreAuthorize("hasAuthority('resume:manage')")`. Return existing API envelopes and `PageResponse` for pagination.

- [ ] **Step 4: Run the tests to verify they pass**

Run: `./mvnw -Dtest=ResumeControllerTest,ResumeFeedbackServiceTest test`

Expected: PASS.

### Task 3: Resume Nuxt feedback experience

**Files:**
- Create: `/Users/yaopinxin/project/GitHub/yu-resume/app/composables/useResumeFeedback.ts`
- Create: `/Users/yaopinxin/project/GitHub/yu-resume/app/components/ResumeReaction.vue`
- Create: `/Users/yaopinxin/project/GitHub/yu-resume/app/components/ResumeReviewDialog.vue`
- Modify: `/Users/yaopinxin/project/GitHub/yu-resume/app/app.vue`
- Modify: `/Users/yaopinxin/project/GitHub/yu-resume/app/utils/resume-contract.test.mjs`

**Interfaces:**
- Consumes `POST /resume/feedback/reactions` and `POST /resume/feedback/reviews` through public Nuxt runtime configuration.
- Produces `ResumeReaction` immediate state and `ResumeReviewDialog` `submitted` event.

- [ ] **Step 1: Write the failing frontend contract tests**

```js
assert.match(feedbackComposable, /feedback\/reactions/)
assert.match(feedbackComposable, /feedback\/reviews/)
assert.match(app, /<ResumeReaction/)
assert.match(app, /<ResumeReviewDialog/)
assert.match(reaction, /感谢你的反馈/)
assert.match(dialog, /评分/)
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `pnpm test:contracts`

Expected: FAIL because the feedback components do not exist.

- [ ] **Step 3: Implement the feedback UI**

Use five button-sized emoji choices outside the article at the lower-left on desktop, with an in-flow compact row after the article below 640px. Immediately post a reaction and replace the row with “感谢你的反馈”; retain an error/retry state without silently losing the choice. Add a right-top textual entry outside the article that opens an accessible native Vue dialog-style overlay; require 1–5 rating, allow optional copied/pasted suggestion, preserve input after errors, and replace only the entry text with “感谢你的反馈” after success.

- [ ] **Step 4: Run frontend verification**

Run: `pnpm test:contracts && pnpm build`

Expected: PASS.

### Task 4: Admin feedback tab

**Files:**
- Modify: `/Users/yaopinxin/project/GitHub/springboot-admin/src/api/resume.ts`
- Modify: `/Users/yaopinxin/project/GitHub/springboot-admin/src/views/content/resume/index.vue`
- Modify: `/Users/yaopinxin/project/GitHub/springboot-admin/src/views/content/resume/contracts.mjs`

**Interfaces:**
- Consumes protected feedback pagination and status endpoints.
- Keeps the existing JSON editor in its own “简历内容” tab and adds a “反馈” tab.

- [ ] **Step 1: Write the failing admin contract test**

```js
assert.match(api, /getResumeFeedback/)
assert.match(api, /updateResumeFeedbackStatus/)
assert.match(view, /el-tabs/)
assert.match(view, /反馈/)
assert.match(view, /标记已读/)
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `node src/views/content/resume/contracts.mjs`

Expected: FAIL because feedback API helpers and tab UI do not exist.

- [ ] **Step 3: Implement the management tab**

Add typed API helpers and render Element Plus tabs. The feedback tab loads paginated data lazily, displays average rating and counts, filters by type/status/rating, expands/copies suggestions, and provides “标记已读” / “归档” actions. Preserve the current resume editor behavior and permissions.

- [ ] **Step 4: Run admin verification**

Run: `node src/views/content/resume/contracts.mjs && pnpm typecheck`

Expected: PASS.

### Task 5: End-to-end regression verification

**Files:**
- Modify: relevant tests only if uncovered behavior is found.

- [ ] **Step 1: Run backend suite**

Run: `./mvnw test`

Expected: PASS.

- [ ] **Step 2: Run all frontend builds**

Run: `pnpm test:contracts && pnpm build` in `yu-resume`, then `pnpm typecheck` in `springboot-admin`.

Expected: PASS.

- [ ] **Step 3: Verify public/management boundary manually**

Run: submit one anonymous reaction and one review to the local backend; verify unauthenticated management list is denied and a super-administrator can view both records.

Expected: public creates succeed; management remains protected; records are visible in the admin feedback tab.
