# Tasks: Support Ticket Management System

**Input**: Design documents from `/specs/001-support-tickets/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/support-ticket-api.md, quickstart.md

**Tests**: Unit + integration per constitution IV and plan.md (not test-first unless noted).

**Organization**: Tasks grouped by user story for independent delivery.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Parallelizable (different files, no blocking deps)
- **[Story]**: US1–US5 for story phases only

## Path Conventions

AEM modules: `core/`, `ui.apps/`, `ui.content/`, `ui.config/`, `it.tests/`, `ui.frontend/`, `all/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Constants, content roots, package filters

- [x] T001 Create `TicketConstants` (paths, property names, status/priority enums) in `core/src/main/java/com/ttn/ai/core/constants/TicketConstants.java`
- [x] T002 [P] Add ticket data root folder `.content.xml` at `ui.content/src/main/content/jcr_root/var/ai-practical-assessment/tickets/.content.xml`
- [x] T003 [P] Update `ui.content/src/main/content/META-INF/vault/filter.xml` for `support-tickets` and `support` pages

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Service layer, validation, OSGi config, service user — MUST complete before user stories

**⚠️ CRITICAL**: No user story work until this phase completes

- [x] T004 Create `InvalidStatusTransitionException` in `core/src/main/java/com/ttn/ai/core/services/InvalidStatusTransitionException.java`
- [x] T005 Create `TicketStatusTransitionValidator` in `core/src/main/java/com/ttn/ai/core/services/TicketStatusTransitionValidator.java`
- [x] T006 Create `TicketService` interface in `core/src/main/java/com/ttn/ai/core/services/TicketService.java`
- [x] T007 Implement `TicketServiceImpl` (create, get, list, update, addComment, updateStatus) in `core/src/main/java/com/ttn/ai/core/services/impl/TicketServiceImpl.java`
- [x] T008 [P] Create `AssigneeConfig` OSGi component in `core/src/main/java/com/ttn/ai/core/config/AssigneeConfig.java`
- [x] T009 [P] Add `TicketServiceImpl` OSGi config at `ui.config/src/main/content/jcr_root/apps/ai-practical-assessment/osgiconfig/config/com.ttn.ai.core.services.impl.TicketServiceImpl.cfg.json`
- [x] T010 [P] Add service user mapper amended config at `ui.config/src/main/content/jcr_root/apps/ai-practical-assessment/osgiconfig/config/org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended-ai-practical-assessment.cfg.json`
- [x] T011 Add service user `rep:policy` ACL for `/var/ai-practical-assessment` in `ui.content` (per data-model.md service user table)
- [x] T012 [P] Unit tests for `TicketStatusTransitionValidator` in `core/src/test/java/com/ttn/ai/core/services/TicketStatusTransitionValidatorTest.java`
- [x] T013 Unit tests for `TicketServiceImpl` CRUD in `core/src/test/java/com/ttn/ai/core/services/TicketServiceImplTest.java`

**Checkpoint**: Foundation ready — user story implementation can begin

---

## Phase 3: User Story 1 - Create Support Ticket (Priority: P1) 🎯 MVP

**Goal**: Authenticated user submits ticket; persisted at `/var/ai-practical-assessment/tickets/{ticket-id}` with status `open`

**Independent Test**: Submit create form → ticket node exists with title, description, priority, requester, timestamps (quickstart VS-1)

### Implementation for User Story 1

- [x] T014 [US1] Implement `CreateTicketServlet` (`POST .../tickets.ticket.json`) in `core/src/main/java/com/ttn/ai/core/servlets/CreateTicketServlet.java`
- [x] T015 [P] [US1] Create `ticket-create` component (`.content.xml`, HTL, dialog) in `ui.apps/src/main/content/jcr_root/apps/ai-practical-assessment/components/ticket-create/`
- [x] T016 [P] [US1] Add `ticket-create` clientlib (form POST/AJAX + CSRF) in `ui.apps/.../clientlibs/clientlib-ticket-create/`
- [x] T017 [US1] Create page `ui.content/.../support-tickets/create-ticket/.content.xml` with `ticket-create` component
- [x] T018 [US1] Wire `ticket-create` HTL to `CreateTicketServlet` per `contracts/support-ticket-api.md` endpoint 1

**Checkpoint**: User Story 1 complete — ticket creation works standalone

---

## Phase 4: User Story 2 - View Tickets Dashboard (Priority: P2)

**Goal**: List all tickets with summary fields; empty state when none; link to detail

**Independent Test**: Dashboard shows all tickets; empty state; row navigates to detail (quickstart VS-2)

### Implementation for User Story 2

- [x] T019 [US2] Implement `TicketListServlet` (`GET .../tickets.list.json`) in `core/src/main/java/com/ttn/ai/core/servlets/TicketListServlet.java`
- [x] T020 [P] [US2] Create `TicketListModel` in `core/src/main/java/com/ttn/ai/core/models/TicketListModel.java`
- [x] T021 [P] [US2] Create `ticket-dashboard` component in `ui.apps/src/main/content/jcr_root/apps/ai-practical-assessment/components/ticket-dashboard/`
- [x] T022 [US2] Create page `ui.content/.../support-tickets/dashboard/.content.xml` with `ticket-dashboard` component
- [x] T023 [US2] Implement dashboard HTL: ticket table, empty state, links to `ticket.html?ticketId={id}`

**Checkpoint**: User Stories 1 + 2 both independently functional

---

## Phase 5: User Story 3 - View and Update Ticket Detail (Priority: P3)

**Goal**: Open ticket detail; update title, description, priority, assignee

**Independent Test**: Open detail, edit fields, save, reload confirms persistence (quickstart VS-3)

### Implementation for User Story 3

- [x] T024 [P] [US3] Create `TicketDetailModel` in `core/src/main/java/com/ttn/ai/core/models/TicketDetailModel.java`
- [x] T025 [US3] Implement `UpdateTicketServlet` (`POST .../{ticket-id}.update.json`) in `core/src/main/java/com/ttn/ai/core/servlets/UpdateTicketServlet.java`
- [x] T026 [P] [US3] Create `ticket-detail` component in `ui.apps/.../components/ticket-detail/`
- [x] T027 [US3] Create page `ui.content/.../support-tickets/ticket/.content.xml` with `ticket-detail` (reads `ticketId` query param)
- [x] T028 [US3] Implement detail HTL: display all fields, edit form, assignee dropdown from `AssigneeConfig`, validation errors

**Checkpoint**: Create → dashboard → detail → update flow works

---

## Phase 6: User Story 4 - Add Comments to Tickets (Priority: P4)

**Goal**: Post comments as child nodes under `comments/{comment-id}`; display oldest-first

**Independent Test**: Post comment → node under ticket; chronological display (quickstart VS-4)

### Implementation for User Story 4

- [x] T029 [P] [US4] Create `CommentModel` in `core/src/main/java/com/ttn/ai/core/models/CommentModel.java`
- [x] T030 [US4] Implement `AddCommentServlet` (`POST .../{ticket-id}/comments.comment.json`) in `core/src/main/java/com/ttn/ai/core/servlets/AddCommentServlet.java`
- [x] T031 [P] [US4] Create `ticket-comments` component in `ui.apps/.../components/ticket-comments/`
- [x] T032 [US4] Add `ticket-comments` to ticket detail page; list comments oldest-first; comment form + validation

**Checkpoint**: Comments persist and display on detail view

---

## Phase 7: User Story 5 - Manage Ticket Status Workflow (Priority: P5)

**Goal**: Enforce FR-010 transitions; single status dropdown in meta field (FR-010a); block invalid and terminal-state changes

**Independent Test**: All allowed transitions succeed; invalid blocked with message (quickstart VS-5)

### Implementation for User Story 5

- [x] T033 [US5] Implement `UpdateTicketStatusServlet` (`POST .../{ticket-id}/status.status.json`) in `core/src/main/java/com/ttn/ai/core/servlets/UpdateTicketStatusServlet.java`
- [x] T034 [US5] Add status dropdown/actions to `ticket-detail` HTL (allowed targets from current status only) — superseded by T041–T042 (FR-010a)
- [x] T035 [US5] Show user-visible error on `INVALID_STATUS_TRANSITION`; disable changes when `closed` or `cancelled`

**Checkpoint**: Full ticket lifecycle operational

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Integration tests, build validation, quickstart

- [x] T036 [P] Integration test `SupportTicketServletIT` in `it.tests/src/main/java/com/ttn/ai/it/tests/SupportTicketServletIT.java` (create, list, update, comment, status)
- [x] T037 Run `mvn clean test -pl core` and fix failures
- [x] T038 Run `mvn clean install` full build (Java 21)
- [x] T039 Execute quickstart.md VS-1 through VS-6 on local AEM SDK — re-validate VS-5 after T055 (FR-010c no-reload)
- [x] T040 [P] Verify no PII in servlet logs; CSRF on Author POST endpoints

---

## Phase 9: FR-010a — Unified Status Dropdown (Spec 2026-08-20)

**Purpose**: Replace separate "Change status" section with one status dropdown in meta field (plan.md, research.md §11)

**Independent Test**: quickstart VS-5 — one dropdown in meta area; terminal tickets read-only; allowed transitions work

### Implementation

- [x] T041 [US5] Refactor `ui.apps/src/main/content/jcr_root/apps/ai-practical-assessment/components/ticket-detail/ticket-detail.html`: status `<select>` in meta area (current + `allowedStatusTargets`); remove "Change status" card; terminal → read-only text
- [x] T042 [P] [US5] Update `ui.apps/src/main/content/jcr_root/apps/ai-practical-assessment/clientlibs/clientlib-ticket-detail/js/ticket-detail.js`: bind unified status dropdown to POST `status.status.json`; remove `#ticket-status-form` handler
- [x] T043 [P] [US5] Style status meta control in `ui.apps/src/main/content/jcr_root/apps/ai-practical-assessment/clientlibs/clientlib-ticket-material/css/ticket-material.css` (dropdown in meta grid)
- [x] T044 [US5] Re-run quickstart VS-5 on Author: dropdown-only UX, terminal read-only, invalid transition blocked — superseded by T052 (Phase 11)

**Checkpoint**: FR-010a v1 done — superseded by Phase 10 → Phase 11

---

## Phase 10: FR-010a/b — Status Chip + Material Select (Spec 2026-08-20 b)

**Purpose**: Fix status UX—Material chip for current status; outlined select with one option per allowed target; no merged HTL options (plan.md, research.md §11)

**Independent Test**: quickstart VS-5 — chip + outlined select; each allowed status separate option; terminal read-only

### Implementation

- [x] T045 [US5] Refactor `ui.apps/src/main/content/jcr_root/apps/ai-practical-assessment/components/ticket-detail/ticket-detail.html`: show current status as `ticket-mui-chip`; add `ticket-mui-field` outlined `<select>` with placeholder only + `data-sly-list` on `<select>` (one `<option>` per `allowedStatusTargets`); remove current status from select options
- [x] T046 [P] [US5] Update `ui.apps/src/main/content/jcr_root/apps/ai-practical-assessment/clientlibs/clientlib-ticket-material/css/ticket-material.css`: status select uses `ticket-mui-field` outlined styling (match priority/assignee); remove plain `.ticket-mui-meta__select` if unused
- [x] T047 [P] [US5] Update `ui.apps/src/main/content/jcr_root/apps/ai-practical-assessment/clientlibs/clientlib-ticket-detail/js/ticket-detail.js`: skip POST on placeholder/empty value; POST only when user picks allowed next status
- [x] T048 [US5] Re-run quickstart VS-5 on Author: chip shows current status; select has separate options; MUI styling; terminal read-only; invalid transition blocked — superseded by T052 (Phase 11)

**Checkpoint**: Phase 10 done — superseded by Phase 11 (Jira-style badge menu)

---

## Phase 11: FR-010a/b — Jira-Style Status Badge Menu (Spec 2026-08-21)

**Purpose**: Replace chip + separate select with unified status badge trigger + dropdown menu (plan.md, research.md §11, quickstart VS-5)

**Independent Test**: quickstart VS-5 — colored badge with chevron opens menu; one menu item per allowed target; terminal read-only badge; invalid transition blocked

### Implementation

- [x] T049 [US5] Refactor `ui.apps/src/main/content/jcr_root/apps/ai-practical-assessment/components/ticket-detail/ticket-detail.html`: unified `#ticket-status-trigger` badge (status label + chevron); `#ticket-status-menu` with `<sly data-sly-list>` one `<button data-status>` per `allowedStatusTargets`; terminal → read-only badge only
- [x] T050 [P] [US5] Add Jira-style status styles in `ui.apps/src/main/content/jcr_root/apps/ai-practical-assessment/clientlibs/clientlib-ticket-material/css/ticket-material.css`: `ticket-mui-status-btn` color modifiers, `ticket-mui-status-menu` elevation, chevron rotation on open
- [x] T051 [P] [US5] Update `ui.apps/src/main/content/jcr_root/apps/ai-practical-assessment/clientlibs/clientlib-ticket-detail/js/ticket-detail.js`: toggle menu on badge click; menu item POST to `status.status.json`; close on outside click/Esc
- [x] T052 [US5] Re-run quickstart VS-5 on Author: badge + menu UX; one item per allowed status; terminal read-only; invalid transition blocked

**Checkpoint**: FR-010a/b UI done — extended by Phase 12 (FR-010c async update)

---

## Phase 12: FR-010c — Async Status Update (Spec 2026-08-22)

**Purpose**: Status change without full page reload; Fetch + in-place DOM update (plan.md, research.md §12, quickstart VS-5)

**Independent Test**: quickstart VS-5 steps 5–6 — badge updates in place; no reload; terminal conversion without reload

### Implementation

- [x] T053 [US5] Add HTL anchors in `ui.apps/src/main/content/jcr_root/apps/ai-practical-assessment/components/ticket-detail/ticket-detail.html`: `#ticket-status-root` (`data-current-status`), `#ticket-last-modified`, `#ticket-terminal-notice` (hidden until terminal)
- [x] T054 [US5] Update `ui.apps/src/main/content/jcr_root/apps/ai-practical-assessment/clientlibs/clientlib-ticket-detail/js/ticket-detail.js`: Fetch POST `status.status.json`; remove `window.location.reload()`; `applyStatusToDom()` updates badge, menu targets, `lastModified`, terminal read-only state
- [x] T055 [US5] Re-run quickstart VS-5 on Author: status saves without page reload; badge/menu update in place; terminal transition shows read-only badge + notice; invalid transition blocked

**Checkpoint**: FR-010a/b/c complete — unified badge menu + async DOM update

---

## Phase 13: Data Persistence Path Migration (Spec 2026-08-26)

**Purpose**: Move ticket/comment data from `/content/.../support-tickets` to `/var/ai-practical-assessment/tickets`; align servlets, OSGi, repoinit, ClientLibs, and tests (FR-015–FR-021)

**Independent Test**: Create ticket → node under `/var/ai-practical-assessment/tickets/{id}`; post comment → node under `.../comments/{comment-id}`; dashboard lists new tickets; no new data nodes under old content path

### Backend & config

- [x] T056 Update `TicketConstants.TICKET_ROOT_PATH` to `/var/ai-practical-assessment/tickets` in `core/src/main/java/com/ttn/ai/core/constants/TicketConstants.java`
- [x] T057 Update `TicketServiceImpl` create/list/get/update/status/comment methods to resolve tickets under `/var/ai-practical-assessment/tickets` in `core/src/main/java/com/ttn/ai/core/services/impl/TicketServiceImpl.java`
- [x] T058 [P] Update `TicketServiceImpl.cfg.json` `ticketRootPath` to `/var/ai-practical-assessment/tickets` in `ui.config/.../com.ttn.ai.core.services.impl.TicketServiceImpl.cfg.json`
- [x] T059 [P] Update repoinit: create `/var/ai-practical-assessment/tickets` + service-user ACLs; remove content-path ticket data root init in `ui.config/.../org.apache.sling.jcr.repoinit.RepositoryInitializer~ai-practical-assessment.cfg.json`
- [x] T060 [P] Update service user `rep:policy` ACL for `/var/ai-practical-assessment/tickets` in `ui.content` (replace or add alongside content ACL per data-model.md)

### Servlets

- [x] T061 Update `CreateTicketServlet` resource type/path binding to `/var/ai-practical-assessment/tickets` in `core/src/main/java/com/ttn/ai/core/servlets/CreateTicketServlet.java`
- [x] T062 [P] Update `TicketListServlet` query root to `/var/ai-practical-assessment/tickets` in `core/src/main/java/com/ttn/ai/core/servlets/TicketListServlet.java`
- [x] T063 [P] Update `UpdateTicketServlet`, `UpdateTicketStatusServlet`, `AssigneeUsersServlet` ticket path resolution in `core/src/main/java/com/ttn/ai/core/servlets/`
- [x] T064 Update `AddCommentServlet` to read/write comments at `/var/ai-practical-assessment/tickets/{ticket-id}/comments` in `core/src/main/java/com/ttn/ai/core/servlets/AddCommentServlet.java`

### Models & HTL

- [x] T065 Update `TicketDetailModel`, `TicketListModel`, `CommentModel` API paths (`data-ticket-path`, list URL, comment URL) to `/var/ai-practical-assessment/tickets` in `core/src/main/java/com/ttn/ai/core/models/`
- [x] T066 [P] Update `ticket-detail.html` and `ticket-comments` HTL `data-*` attributes to expose new ticket/comment resource paths in `ui.apps/.../components/`

### ClientLibs

- [x] T067 Update `clientlib-ticket-create/js/ticket-create.js` fetch to `POST /var/ai-practical-assessment/tickets.ticket.json`
- [x] T068 [P] Update `clientlib-ticket-detail/js/ticket-detail.js` status/update/assignee fetch URLs to use `/var/ai-practical-assessment/tickets/{ticket-id}` from HTL `data-ticket-path`
- [x] T069 [P] Update `clientlib-ticket-comments` (or ticket-comments component JS) comment submit fetch to `/var/ai-practical-assessment/tickets/{ticket-id}/comments.comment.json`

### Tests & validation

- [x] T070 [P] Update `TicketServiceImplTest` fixture paths to `/var/ai-practical-assessment/tickets` in `core/src/test/java/com/ttn/ai/core/services/TicketServiceImplTest.java`
- [x] T071 Update `SupportTicketServletIT` `TICKETS_ROOT` and comment paths in `it.tests/src/main/java/com/ttn/ai/it/tests/SupportTicketServletIT.java`
- [x] T072 Re-run `mvn clean test -pl core,it.tests` and quickstart VS-1 through VS-4 against new paths

**Checkpoint**: All ticket/comment persistence and ClientLib fetch calls use `/var/ai-practical-assessment/tickets`

---

## Phase 14: Var Folder Structure, ACL & ClientLib Proxy (Spec 2026-08-26 b)

**Purpose**: Ship `/var/ai-practical-assessment/tickets` as `sling:Folder` in `ui.content`; `rep:policy` for `ticket-service`; `allowProxy` on all ClientLibs (FR-022–FR-024)

**Independent Test**: After deploy, CRXDE shows `sling:Folder` at `/var/ai-practical-assessment` and `.../tickets` with tickets-root RT; ticket create succeeds; ClientLib URLs use `/etc.clientlibs/...` on publish

### Implementation

- [x] T073 Add `sling:Folder` content nodes at `ui.content/.../var/ai-practical-assessment/.content.xml` and `.../tickets/.content.xml` (tickets-root RT on tickets folder)
- [x] T074 [P] Add `_rep_policy.xml` for `ticket-service` on `/var/ai-practical-assessment` in `ui.content/.../var/ai-practical-assessment/_rep_policy.xml`
- [x] T075 [P] Add `/var/ai-practical-assessment` to `ui.content/.../vault/filter.xml` and `ui.apps.structure/pom.xml` filters
- [x] T076 Remove duplicate `/var` path creation from repoinit (folders now in `ui.content`)
- [x] T077 [P] Set `allowProxy="{Boolean}true"` on all ticket ClientLibs (`clientlib-ticket-create`, `-detail`, `-dashboard`, `-comments`); verify base/grid/material already proxied
- [x] T078 Update `TicketServiceImpl.ensureTicketRoot()` to create `sling:Folder` nodes when bootstrapping in tests

**Checkpoint**: Var hierarchy + ACL in content package; all ClientLibs proxy-enabled

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Setup — **BLOCKS all user stories**
- **US1 (Phase 3)**: Depends on Foundational — MVP entry point
- **US2 (Phase 4)**: Depends on Foundational; benefits from US1 data but can seed test tickets via service
- **US3 (Phase 5)**: Depends on Foundational + US2 navigation (or direct URL with `ticketId`)
- **US4 (Phase 6)**: Depends on US3 detail page shell
- **US5 (Phase 7)**: Depends on US3 detail component
- **Polish (Phase 8)**: Depends on US1–US5
- **FR-010a (Phase 9)**: Depends on US5 servlet (T033); updates T034 UI only
- **FR-010a/b (Phase 10)**: Depends on Phase 9; chip + MUI select (superseded)
- **FR-010a/b (Phase 11)**: Depends on Phase 10; Jira-style badge menu (plan 2026-08-21)
- **FR-010c (Phase 12)**: Depends on Phase 11; async status DOM update, no reload (plan 2026-08-22)
- **Path migration (Phase 13)**: Depends on US1–US4 servlets + ClientLibs; storage root → `/var/ai-practical-assessment/tickets` (spec 2026-08-26)

### User Story Dependencies

| Story | Depends on | Independent test |
|-------|------------|------------------|
| US1 | Foundational | Create ticket only |
| US2 | Foundational | List tickets (seed via service or US1) |
| US3 | Foundational, US2 nav optional | Update via detail URL + `ticketId` |
| US4 | US3 detail page | Comments on existing ticket |
| US5 | US3 detail UI | Status changes on existing ticket |

### Parallel Opportunities

- **Phase 1**: T002, T003 parallel after T001
- **Phase 2**: T008–T012 parallel after T004–T007 started
- **Per story**: Servlet + component + model tasks marked [P] within same phase
- **Polish**: T036, T040 parallel
- **Phase 9**: T042, T043 parallel after T041
- **Phase 10**: T046, T047 parallel after T045
- **Phase 11**: T050, T051 parallel after T049
- **Phase 12**: T054 after T053 (same clientlib; sequential with HTL)

---

## Parallel Example: User Story 1

```bash
# After T014 starts, launch in parallel:
T015 ticket-create component
T016 ticket-create clientlib
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 + Phase 2
2. Complete Phase 3 (US1)
3. **STOP and VALIDATE** quickstart VS-1
4. Demo ticket creation

### Incremental Delivery

1. Setup + Foundational → foundation ready
2. US1 → test → demo (MVP)
3. US2 → dashboard visibility
4. US3 → edit/reassign
5. US4 → comments
6. US5 → status workflow
7. **Phase 9 (FR-010a)** → unified status dropdown (done)
8. **Phase 10** → chip + select (done, superseded)
9. **Phase 11 (FR-010a/b)** → Jira-style badge menu (done)
10. **Phase 12 (FR-010c)** → async status, no reload
11. Polish → IT + full build + T039/T055 validation

### Suggested MVP Scope

**User Story 1 only** (Phases 1–3): user can create a ticket with validated persistence.

---

## Notes

- Use Adobe `create-component` skill for HTL components (constitution VI)
- All Java targets Java 21 / `JavaSE-21` (constitution v1.0.2)
- Bounded queries: `p.limit` max 100 on list (data-model.md)
- Ticket data paths: `/var/ai-practical-assessment/tickets/{ticket-id}/comments/{comment-id}`
- Page paths: `/content/ai-practical-assessment/support-tickets/dashboard`, `create-ticket`, `ticket`
- UI: Material Design via `clientlib-ticket-material`; ticket components in **AI Capability Project - Content** with `_cq_dialog`
- Contract reference: `specs/001-support-tickets/contracts/support-ticket-api.md`
- **FR-010a** (2026-08-20): one status control in detail meta — Phase 9 tasks T041–T043 (done)
- **FR-010a/b** (2026-08-20 b): chip + outlined MUI select — Phase 10 tasks T045–T047 (done, superseded)
- **FR-010a/b** (2026-08-21): Jira-style badge trigger + dropdown menu — Phase 11 tasks T049–T052 (done)
- **FR-010c** (2026-08-22): async Fetch status update, in-place DOM — Phase 12 tasks T053–T055
- **FR-015–FR-021** (2026-08-26): ticket/comment data under `/var/ai-practical-assessment/tickets` — Phase 13 tasks T056–T072
- **FR-022–FR-024** (2026-08-26 b): var `sling:Folder` in ui.content, rep:policy, ClientLib allowProxy — Phase 14 tasks T073–T078
