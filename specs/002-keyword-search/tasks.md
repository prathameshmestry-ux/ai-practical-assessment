# Tasks: Ticket Dashboard Keyword Search

**Input**: Design documents from `/specs/002-keyword-search/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/support-ticket-search-api.md (v1.2), quickstart.md

**Tests**: Unit + IT per NFR-003 (constitution IV)

**Organization**: Phases by user story priority — US1 (P1) → US4/US2 (P2) → US3 (P3); Phases 7–8 spec refinements (prefix + status)

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Parallel-safe (different files, no blocking deps)
- **[Story]**: US1, US2, US3, US4 per `spec.md`

## Path Conventions

- Java/OSGi → `core/`
- HTL / ClientLibs → `ui.apps/src/main/content/jcr_root/apps/ai-practical-assessment/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm modules and 001 dependencies

- [x] T001 Confirm target modules (`core`, `ui.apps`) per `specs/002-keyword-search/plan.md` Constitution Check
- [x] T002 [P] Verify ticket data root `/var/ai-practical-assessment/tickets` and list servlet in `core/src/main/java/com/ttn/ai/core/servlets/TicketListServlet.java`
- [x] T003 [P] Confirm `mvn clean test -pl core` passes on JDK 21

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared API paths and service contract

**⚠️ CRITICAL**: Block user stories until complete

- [x] T004 Add `TICKET_SEARCH_SERVLET_PATH` in `core/src/main/java/com/ttn/ai/core/constants/TicketConstants.java`
- [x] T005 Add `searchTickets(...)` to `core/src/main/java/com/ttn/ai/core/services/TicketService.java` (extended in T030 with `status`)
- [x] T006 [P] Add `getSearchApiPath()` and `getTicketsApiPath()` in `core/src/main/java/com/ttn/ai/core/models/TicketListModel.java`

**Checkpoint**: Foundation ready

---

## Phase 3: User Story 1 — Search Tickets by Keyword (Priority: P1) 🎯 MVP

**Goal**: Prefix search by title or ticket ID (`ticket-` prefix routes to ID)

**Independent Test**: quickstart VS-1, VS-2 — `permis` matches title start; `ticket-8fde` matches ID start; no reload

### Tests for User Story 1

- [x] T007 [P] [US1] Unit test blank keyword validation in `core/src/test/java/com/ttn/ai/core/services/TicketServiceImplTest.java`

### Implementation for User Story 1

- [x] T008 [US1] Implement `buildSearchPredicates()` and `searchTickets()` in `core/src/main/java/com/ttn/ai/core/services/impl/TicketServiceImpl.java`
- [x] T009 [US1] Create `SearchTicketsServlet` in `core/src/main/java/com/ttn/ai/core/servlets/SearchTicketsServlet.java`
- [x] T010 [P] [US1] Add search input, button, `data-search-api-url` in `ui.apps/src/main/content/jcr_root/apps/ai-practical-assessment/components/ticket-dashboard/ticket-dashboard.html`
- [x] T011 [US1] Keyword search fetch + table re-render in `ui.apps/src/main/content/jcr_root/apps/ai-practical-assessment/clientlibs/clientlib-ticket-dashboard/js/ticket-dashboard.js`

**Checkpoint**: US1 baseline — Phase 7 refines prefix semantics

---

## Phase 4: User Story 2 — Submit Search Without Page Reload (Priority: P2)

**Goal**: Async UX — Enter/button; only `#ticket-dashboard-body` updates

**Independent Test**: quickstart VS-3 — URL unchanged after search

### Implementation for User Story 2

- [x] T012 [US2] Wire Enter + Search button without form navigation in `ui.apps/.../clientlib-ticket-dashboard/js/ticket-dashboard.js`
- [x] T013 [P] [US2] Implement `renderTickets()` and empty-state toggle in `ui.apps/.../clientlib-ticket-dashboard/js/ticket-dashboard.js`
- [x] T014 [P] [US2] Style search row in `ui.apps/.../clientlib-ticket-dashboard/css/ticket-dashboard.css`
- [x] T015 [P] [US2] Register JS in `ui.apps/.../clientlib-ticket-dashboard/js.txt`

**Checkpoint**: US1 + US2 async complete

---

## Phase 5: User Story 3 — Reset Search to Full List (Priority: P3)

**Goal**: Empty keyword + status **All** → full list via list API

**Independent Test**: quickstart VS-4

### Implementation for User Story 3

- [x] T016 [US3] Add `data-tickets-api-url` and `data-empty-message` in `ui.apps/.../components/ticket-dashboard/ticket-dashboard.html`
- [x] T017 [US3] Implement `resetToAllTickets()` in `ui.apps/.../clientlib-ticket-dashboard/js/ticket-dashboard.js`

**Checkpoint**: Search + reset loop complete

---

## Phase 6: Polish & Cross-Cutting (baseline)

**Purpose**: IT coverage, doc alignment

- [x] T018 Run `mvn clean test -pl core`
- [x] T020 [P] Search GET ITs in `it.tests/src/main/java/com/ttn/ai/it/tests/SupportTicketServletIT.java`
- [x] T021 [P] Align spec/plan/contracts in `specs/002-keyword-search/`

---

## Phase 7: Prefix Matching — Session 2026-08-27 d

**Purpose**: FR-003 starts-with only (`{keyword}%`, not `%keyword%`)

**Independent Test**: quickstart VS-1 negative case; VS-2 ID prefix

### Tests for User Story 1

- [x] T022 [P] [US1] Unit tests for prefix predicate values in `core/src/test/java/com/ttn/ai/core/services/TicketServiceImplTest.java`
- [x] T023 [P] [US1] Update title prefix IT in `it.tests/src/main/java/com/ttn/ai/it/tests/SupportTicketServletIT.java`

### Implementation for User Story 1

- [x] T024 [US1] Fix `buildSearchPredicates()` to `{keyword}%` in `core/src/main/java/com/ttn/ai/core/services/impl/TicketServiceImpl.java`

### Validation

- [x] T025 Run `mvn clean test -pl core`
- [x] T026 [P] Compile `it.tests` — `mvn clean compile -pl it.tests`
- [x] T027 Run quickstart VS-1–VS-6 on local AEM SDK *(manual)*

**Checkpoint**: Prefix search matches contract v1.1

---

## Phase 8: Filter by Status — Session 2026-08-27 e

**Purpose**: FR-010–FR-012 status dropdown + servlet `status` param

**Independent Test**: quickstart VS-5 — Open only; keyword + status; All + empty → full list

### Tests for User Story 4

- [x] T028 [P] [US4] Unit tests for status predicate in `core/src/test/java/com/ttn/ai/core/services/TicketServiceImplTest.java`
- [x] T029 [P] [US4] IT `testSearchByStatusOnly` in `it.tests/src/main/java/com/ttn/ai/it/tests/SupportTicketServletIT.java`

### Implementation for User Story 4

- [x] T030 [US4] `searchTickets(keyword, status, offset, limit)` + status predicate in `core/src/main/java/com/ttn/ai/core/services/impl/TicketServiceImpl.java`
- [x] T031 [US4] Pass `status` in `core/src/main/java/com/ttn/ai/core/servlets/SearchTicketsServlet.java`
- [x] T032 [P] [US4] Status `<select>` in `ui.apps/.../components/ticket-dashboard/ticket-dashboard.html`
- [x] T033 [US4] `runFilter()` in `ui.apps/.../clientlib-ticket-dashboard/js/ticket-dashboard.js`
- [x] T034 [P] [US4] Status dropdown CSS in `ui.apps/.../clientlib-ticket-dashboard/css/ticket-dashboard.css`

### Validation

- [x] T035 Run `mvn clean test -pl core`
- [x] T036 [P] Compile `it.tests` — `mvn clean compile -pl it.tests`
- [x] T037 [P] Update `spec.md`, `plan.md`, `research.md`, `data-model.md`, `contracts/support-ticket-search-api.md` (v1.2), `quickstart.md`
- [ ] T038 Run quickstart VS-5 + VS-7 on local AEM SDK *(manual — deploy with `mvn clean install -PautoInstallSinglePackage`, then browser)*

**Checkpoint**: Status filter integrated — T038 manual sign-off pending

---

## Dependencies & Execution Order

### Phase Dependencies

| Phase | Depends on |
|-------|------------|
| 1–2 | — |
| 3 (US1) | Phase 2 |
| 4 (US2) | Phase 3 ClientLib |
| 5 (US3) | Phase 4 |
| 6 | Phases 3–5 |
| 7 | Phase 3 T008 |
| 8 | Phase 7 (shared predicates) |

### User Story Map

| Story | Priority | Tasks | Independent test |
|-------|----------|-------|------------------|
| US1 | P1 | T007–T011, T022–T024 | VS-1, VS-2 |
| US2 | P2 | T012–T015 | VS-3 |
| US3 | P3 | T016–T017 | VS-4 |
| US4 | P2 | T028–T034 | VS-5 |

### Parallel Examples

- **Phase 8 tests**: T028 ∥ T029 after T030
- **Phase 8 UI**: T032 ∥ T034 while T033 depends on T030–T031
- **Validation**: T035 ∥ T036

---

## Implementation Strategy

### MVP

Phase 1 → 2 → 3 → 7 = prefix keyword search (SC-001).

### Full feature

Add Phases 4–5 (async + reset), Phase 8 (status), then **T038** manual sign-off.

### Current focus

**T038** — browser quickstart VS-5/VS-7 after deploy.

---

## Notes

- Search: `/bin/ai-practical-assessment/search.json`
- Reset: blank keyword + **All** status → `tickets.list.json`
- T019 superseded by T027
- **37/38 complete** (T038 manual)
