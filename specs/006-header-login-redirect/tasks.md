# Tasks: Header Login Post-Redirect

**Input**: `/specs/006-header-login-redirect/`

## Phase 1: Backend

- [x] T001 Add `ServletAuth.buildLoginPath` in `core/.../servlets/ServletAuth.java`
- [x] T002 Add `getCurrentPagePath` + update `getLoginPath` in `HeaderModel.java`
- [x] T003 [P] Update `HeaderModelTest` for resource param

## Phase 2: Frontend

- [x] T004 Login anchor uses `model.loginPath` in `header.html` (dynamic redirect built in model)

## Phase 3: Validation

- [x] T005 Run `mvn clean test -pl core`
- [ ] T006 Manual: login from dashboard → return to dashboard *(manual)*
