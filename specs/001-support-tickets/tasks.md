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
- [x] T002 [P] Add ticket data root folder `.content.xml` at `ui.content/src/main/content/jcr_root/content/ai-practical-assessment/support-tickets/.content.xml`
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
- [x] T011 Add service user `rep:policy` ACL for `/content/ai-practical-assessment/support-tickets` in `ui.content` (per data-model.md service user table)
- [x] T012 [P] Unit tests for `TicketStatusTransitionValidator` in `core/src/test/java/com/ttn/ai/core/services/TicketStatusTransitionValidatorTest.java`
- [x] T013 Unit tests for `TicketServiceImpl` CRUD in `core/src/test/java/com/ttn/ai/core/services/TicketServiceImplTest.java`

**Checkpoint**: Foundation ready — user story implementation can begin

---

## Phase 3: User Story 1 - Create Support Ticket (Priority: P1) 🎯 MVP

**Goal**: Authenticated user submits ticket; persisted at `/content/ai-practical-assessment/support-tickets/{ticket-id}` with status `open`

**Independent Test**: Submit create form → ticket node exists with title, description, priority, requester, timestamps (quickstart VS-1)

### Implementation for User Story 1

- [x] T014 [US1] Implement `CreateTicketServlet` (`POST .../support-tickets.ticket.json`) in `core/src/main/java/com/ttn/ai/core/servlets/CreateTicketServlet.java`
- [x] T015 [P] [US1] Create `ticket-create` component (`.content.xml`, HTL, dialog) in `ui.apps/src/main/content/jcr_root/apps/ai-practical-assessment/components/ticket-create/`
- [x] T016 [P] [US1] Add `ticket-create` clientlib (form POST/AJAX + CSRF) in `ui.apps/.../clientlibs/clientlib-ticket-create/`
- [x] T017 [US1] Create page `ui.content/src/main/content/jcr_root/content/ai-practical-assessment/support/create/.content.xml` with `ticket-create` component
- [x] T018 [US1] Wire `ticket-create` HTL to `CreateTicketServlet` per `contracts/support-ticket-api.md` endpoint 1

**Checkpoint**: User Story 1 complete — ticket creation works standalone

---

## Phase 4: User Story 2 - View Tickets Dashboard (Priority: P2)

**Goal**: List all tickets with summary fields; empty state when none; link to detail

**Independent Test**: Dashboard shows all tickets; empty state; row navigates to detail (quickstart VS-2)

### Implementation for User Story 2

- [x] T019 [US2] Implement `TicketListServlet` (`GET .../support-tickets.list.json`) in `core/src/main/java/com/ttn/ai/core/servlets/TicketListServlet.java`
- [x] T020 [P] [US2] Create `TicketListModel` in `core/src/main/java/com/ttn/ai/core/models/TicketListModel.java`
- [x] T021 [P] [US2] Create `ticket-dashboard` component in `ui.apps/src/main/content/jcr_root/apps/ai-practical-assessment/components/ticket-dashboard/`
- [x] T022 [US2] Create page `ui.content/.../support/dashboard/.content.xml` with `ticket-dashboard` component
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
- [x] T027 [US3] Create page `ui.content/.../support/ticket/.content.xml` with `ticket-detail` (reads `ticketId` query param)
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

**Goal**: Enforce FR-010 transitions; block invalid and terminal-state changes

**Independent Test**: All allowed transitions succeed; invalid blocked with message (quickstart VS-5)

### Implementation for User Story 5

- [x] T033 [US5] Implement `UpdateTicketStatusServlet` (`POST .../{ticket-id}/status.status.json`) in `core/src/main/java/com/ttn/ai/core/servlets/UpdateTicketStatusServlet.java`
- [x] T034 [US5] Add status dropdown/actions to `ticket-detail` HTL (allowed targets from current status only)
- [x] T035 [US5] Show user-visible error on `INVALID_STATUS_TRANSITION`; disable changes when `closed` or `cancelled`

**Checkpoint**: Full ticket lifecycle operational

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Integration tests, build validation, quickstart

- [x] T036 [P] Integration test `SupportTicketServletIT` in `it.tests/src/main/java/com/ttn/ai/it/tests/SupportTicketServletIT.java` (create, list, update, comment, status)
- [x] T037 Run `mvn clean test -pl core` and fix failures
- [x] T038 Run `mvn clean install` full build (Java 17)
- [ ] T039 Execute quickstart.md VS-1 through VS-6 on local AEM SDK
- [x] T040 [P] Verify no PII in servlet logs; CSRF on Author POST endpoints

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
7. Polish → IT + full build

### Suggested MVP Scope

**User Story 1 only** (Phases 1–3): user can create a ticket with validated persistence.

---

## Notes

- Use Adobe `create-component` skill for HTL components (constitution VI)
- All Java targets Java 17 / `JavaSE-17` (constitution v1.0.1)
- Bounded queries: `p.limit` max 100 on list (data-model.md)
- Ticket paths: `/content/ai-practical-assessment/support-tickets/{ticket-id}/comments/{comment-id}`
- Page paths: `/content/ai-practical-assessment/support/{create|dashboard|ticket}`
- UI: Material Design via `clientlib-ticket-material`; ticket components in **AI Capability Project - Content** with `_cq_dialog`
- Contract reference: `specs/001-support-tickets/contracts/support-ticket-api.md`
