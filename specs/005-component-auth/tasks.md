# Tasks: Component-Level Ticket Authentication

**Input**: `/specs/005-component-auth/`

## Phase 1: Backend helpers

- [x] T001 Add `TicketApiJson.writeUnauthorized` (401 + `{}`) in `core/.../servlets/TicketApiJson.java`
- [x] T002 Add `ServletAuth.requireAuthenticated` in `core/.../servlets/ServletAuth.java`
- [x] T003 [P] Unit test for unauthorized JSON response in `core/.../servlets/TicketApiJsonTest.java`

## Phase 2: User Story 1 — Servlet defense

- [x] T004 [US1] Update all ticket repository GET/POST servlets to use `requireAuthenticated` (`GetTicketServlet`, `TicketListServlet`, `CreateTicketServlet`, `UpdateTicketServlet`, `UpdateTicketStatusServlet`, `AddCommentServlet`, `AssigneeUsersServlet`)
- [x] T005 [US1] Update `SearchTicketsServlet` to use `requireAuthenticated`

## Phase 3: User Story 2 — Component models

- [x] T006 [US2] Add `isLoggedIn` and skip `listTickets` when anonymous in `TicketListModel.java`
- [x] T007 [US2] Add `isLoggedIn` and skip `getTicket` when anonymous in `TicketDetailModel.java`
- [x] T008 [P] [US2] `TicketListModelTest` and `TicketDetailModelTest`

## Phase 4: User Story 3 — Component HTL

- [x] T009 [US3] Gate dashboard UI in `ticket-dashboard.html` with login fallback message
- [x] T010 [US3] Gate detail UI in `ticket-detail.html` with login fallback message

## Phase 4b: Create Ticket gate

- [x] T013 [US3] Add `TicketCreateModel` with `isLoggedIn` in `core/.../models/TicketCreateModel.java`
- [x] T014 [US3] Gate create UI in `ticket-create.html` with login fallback message
- [x] T015 [P] [US3] `TicketCreateModelTest`

## Phase 5: Validation

- [x] T011 Run `mvn clean test -pl core`
- [ ] T012 Manual: anonymous dashboard/create/detail show login message; Header Login visible; API returns 401 `{}` *(manual)*

## Notes

- No OSGi auth redirect configs added or changed; pages remain public.
- Create ticket page gated same as dashboard and detail.
