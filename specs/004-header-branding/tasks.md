# Tasks: Header Branding and User Profile

**Input**: `/specs/004-header-branding/`

## Phase 1: Component scaffold

- [x] T001 Create `ui.apps/.../components/header/.content.xml`
- [x] T002 [P] Create `ui.apps/.../components/header/_cq_dialog/.content.xml`

## Phase 2: User Story 1 — Branding

- [x] T003 [US1] `HeaderModel` logo/name in `core/.../models/HeaderModel.java`
- [x] T004 [US1] Branding HTL in `ui.apps/.../components/header/header.html`
- [x] T005 [P] [US1] `HeaderModelTest` branding cases

## Phase 3: User Story 2 — User Profile & Authentication

- [x] T008 [US2] Extend `ServletAuth` with UserManager profile resolution in `core/.../servlets/ServletAuth.java`
- [x] T009 [US2] Add `isLoggedIn`, `userFullName`, `userId` to `HeaderModel.java`
- [x] T010 [US2] Profile block in `header.html` (Welcome/Logout vs Login)
- [x] T011 [P] [US2] `ServletAuthTest` + update `HeaderModelTest`

## Phase 4: Validation

- [x] T006 Run `mvn clean test -pl core`
- [ ] T007 Manual: verify Login/Logout + branding on header XF *(manual)*

## Notes

- Profile section always in `<header>`; branding sub-block conditional on logo/name.
