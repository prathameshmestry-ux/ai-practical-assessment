---

description: "Task list template for feature implementation"
---

# Tasks: [FEATURE NAME]

**Input**: Design documents from `/specs/[###-feature-name]/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Include test tasks when the feature changes behavior/logic (constitution
Principle IV). Omit only with an explicit deferred-test rationale in the plan.
If the spec requests test-first, write failing tests before implementation.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **AEM CS (this repo)**: `core/`, `ui.apps/`, `ui.frontend/`, `ui.config/`,
  `ui.content/`, `dispatcher/`, `it.tests/`, `ui.tests/`, `all/`
- Place Java/OSGi in `core`; HTL/components in `ui.apps`; frontend sources in
  `ui.frontend`; OSGi configs in `ui.config`; ITs in `it.tests` (`*IT.java`);
  UI tests in `ui.tests`
- Paths in sample tasks below are illustrative — replace with real module paths
  from plan.md (do not invent `src/` at repo root)

<!--
  ============================================================================
  IMPORTANT: The tasks below are SAMPLE TASKS for illustration purposes only.

  The /speckit-tasks command MUST replace these with actual tasks based on:
  - User stories from spec.md (with their priorities P1, P2, P3...)
  - Feature requirements from plan.md
  - Entities from data-model.md
  - Endpoints from contracts/

  Tasks MUST be organized by user story so each story can be:
  - Implemented independently
  - Tested independently
  - Delivered as an MVP increment

  DO NOT keep these sample tasks in the generated tasks.md file.
  ============================================================================
-->

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm modules, packages, and tooling for this feature (repo already exists)

- [ ] T001 Identify target Maven modules per plan Constitution Check
- [ ] T002 [P] Add/adjust package folders under the correct modules only
- [ ] T003 [P] Confirm build/test commands (`mvn clean test`, module profiles)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

Examples of foundational tasks (adjust based on your feature):

- [ ] T004 Create shared Sling Models / OSGi service interfaces in `core`
- [ ] T005 [P] Add baseline OSGi configs in `ui.config` (no secrets)
- [ ] T006 [P] Register component structure / dialog scaffolding in `ui.apps`
- [ ] T007 Wire frontend entry points in `ui.frontend` if clientlibs change
- [ ] T008 Configure error handling and logging in affected services (no full PII)
- [ ] T009 [P] Add Dispatcher allow/cache rules only if publish URLs change

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - [Title] (Priority: P1) 🎯 MVP

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 1 (OPTIONAL - only if tests requested) ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T010 [P] [US1] Unit test for [model/service] in `core/src/test/java/.../[Name]Test.java`
- [ ] T011 [P] [US1] Integration test for [journey] in `it.tests/src/main/java/.../[Name]IT.java`

### Implementation for User Story 1

- [ ] T012 [P] [US1] Create Sling Model in `core/src/main/java/.../models/[Model].java`
- [ ] T013 [P] [US1] Create OSGi service in `core/src/main/java/.../services/[Service].java`
- [ ] T014 [US1] Implement HTL/dialog in `ui.apps/.../components/[name]/` (depends on T012)
- [ ] T015 [US1] Implement frontend/clientlib updates in `ui.frontend/` if needed
- [ ] T016 [US1] Add validation and error handling
- [ ] T017 [US1] Add logging for user story 1 operations (no full ticket/PII payloads)

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - [Title] (Priority: P2)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 2 (OPTIONAL - only if tests requested) ⚠️

- [ ] T018 [P] [US2] Unit test for [model/service] in `core/src/test/java/.../[Name]Test.java`
- [ ] T019 [P] [US2] Integration test for [journey] in `it.tests/src/main/java/.../[Name]IT.java`

### Implementation for User Story 2

- [ ] T020 [P] [US2] Create Sling Model in `core/src/main/java/.../models/[Model].java`
- [ ] T021 [US2] Implement OSGi service in `core/src/main/java/.../services/[Service].java`
- [ ] T022 [US2] Implement HTL/component updates in `ui.apps/.../`
- [ ] T023 [US2] Integrate with User Story 1 components (if needed)

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - [Title] (Priority: P3)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 3 (OPTIONAL - only if tests requested) ⚠️

- [ ] T024 [P] [US3] Unit test for [model/service] in `core/src/test/java/.../[Name]Test.java`
- [ ] T025 [P] [US3] UI test for [journey] in `ui.tests/` (if author/publish UX changes)

### Implementation for User Story 3

- [ ] T026 [P] [US3] Create Sling Model in `core/src/main/java/.../models/[Model].java`
- [ ] T027 [US3] Implement OSGi service in `core/src/main/java/.../services/[Service].java`
- [ ] T028 [US3] Implement HTL/component updates in `ui.apps/.../`

**Checkpoint**: All user stories should now be independently functional

---

[Add more user story phases as needed, following the same pattern]

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] TXXX [P] Documentation / quickstart updates under `specs/[###-feature]/`
- [ ] TXXX Code cleanup within touched modules only (no drive-by refactors)
- [ ] TXXX [P] Additional unit tests in `core/src/test/java/` (if requested)
- [ ] TXXX Security review: no secrets, timeouts set, Dispatcher rules reviewed
- [ ] TXXX Run `mvn clean test` (and Dispatcher validate if `dispatcher/` changed)
- [ ] TXXX Run quickstart.md validation

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - May integrate with US1 but should be independently testable
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - May integrate with US1/US2 but should be independently testable

### Within Each User Story

- Tests (if included) MUST be written and FAIL before implementation
- Sling Models / service APIs before HTL and clientlibs
- `core` before `ui.apps` consumers; `ui.frontend` before clientlib sync expectations
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Models/services in different packages marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together (if tests requested):
Task: "Unit test for [model/service] in core/src/test/java/.../[Name]Test.java"
Task: "Integration test for [journey] in it.tests/src/main/java/.../[Name]IT.java"

# Launch independent core types together:
Task: "Create Sling Model in core/src/main/java/.../models/[Model].java"
Task: "Create OSGi service in core/src/main/java/.../services/[Service].java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1
   - Developer B: User Story 2
   - Developer C: User Story 3
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
