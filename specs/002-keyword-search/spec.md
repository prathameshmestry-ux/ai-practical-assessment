# Feature Specification: Ticket Dashboard Keyword Search

**Feature Branch**: `002-keyword-search`

**Created**: 2026-08-27

**Status**: Draft

**Input**: User description: "Create new spec to include a 'Keyword Search' feature on the Ticket Dashboard. Backend GET Servlet with QueryBuilder on /var/ai-practical-assessment/tickets. Dashboard search input, ClientLib async fetch, dynamic list re-render without page reload."

## Clarifications

### Session 2026-08-27

- Q: How should the search servlet be registered? → A: **Path-based servlet** (JSON extension)—not resource-type/selector binding. Path updated to `/bin/ai-practical-assessment/search` (see clarify session).
- Q: Which fields does keyword search match? → A: **Title prefix** when keyword does not start with `ticket-` (title must **begin with** the keyword). **Ticket ID prefix** when keyword starts with `ticket-` (ID must **begin with** the keyword). Partial prefixes allowed (e.g. `permis`, `ticket-8fde`).
- Q: What is the search servlet URL path? → A: **`/bin/ai-practical-assessment/search`** (path servlet with JSON extension)—not under `/var/.../tickets/search`.
- Q: What happens when the search box is empty on submit? → A: **Reset dashboard**—fetch full ticket list and re-render table (same as initial load); no validation error.

### Session 2026-08-27 (d)

- Q: How should keyword matching work (partial words vs full text)? → A: **Prefix (starts-with) matching** only—return tickets whose **title begins with** the keyword (e.g. `permis` matches titles starting with `permis`), or whose **ticket ID begins with** the keyword when the keyword starts with `ticket-` (e.g. `ticket-8fde` matches IDs starting with `ticket-8fde`). No “contains anywhere in title” matching in v1.

### Session 2026-08-27 (e)

- Q: How does status filter combine with keyword search? → A: Optional **status** parameter on the same search endpoint; when **All** is selected, no status predicate is applied. Status can filter alone (no keyword) or together with keyword prefix search.
- Q: What happens on empty keyword with status **All**? → A: Reset to full list via list API (unchanged FR-006).
- Q: What label should the status dropdown use? → A: **Filter By Status** (exact visible label next to the dropdown).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Search Tickets by Keyword (Priority: P1)

As a support team member, I want to search tickets on the dashboard by keyword so that I can find relevant requests quickly without scrolling the full list.

**Why this priority**: Dashboard search is the core value of this feature; without it nothing else matters.

**Independent Test**: Enter a partial prefix such as `permis` that matches the start of a ticket title, press Enter or Search, and confirm only tickets whose titles begin with that prefix appear—without a full page reload.

**Acceptance Scenarios**:

1. **Given** a ticket titled `Permission reset needed`, **When** the user searches `permis` (does not start with `ticket-`), **Then** that ticket appears because its title **starts with** `permis`.
2. **Given** a ticket titled `Access permission issue`, **When** the user searches `permis`, **Then** that ticket does **not** appear (title does not start with `permis`).
3. **Given** the user enters a keyword starting with `ticket-` (e.g. `ticket-8fde`), **When** they submit search, **Then** results include only tickets whose ticket ID **starts with** that prefix—not title matches.
4. **Given** the user submits a keyword with no prefix matches, **When** results return, **Then** the dashboard shows an empty-state message and no matching rows.
5. **Given** search results are displayed, **When** the user clicks a ticket ID link, **Then** they navigate to that ticket's detail page.
6. **Given** the user has filtered the table via search, **When** they clear the search box and submit (Enter or Search), **Then** the dashboard reloads the full ticket list without a page refresh.

---

### User Story 3 - Reset Search to Full List (Priority: P3)

As a dashboard user, I want clearing the search and submitting to show all tickets again so that I can return to the full list without refreshing the page.

**Why this priority**: Reset completes the search loop; without it users are stuck in filtered view until reload.

**Independent Test**: Run a search, clear the input, press Enter, and confirm all tickets reappear.

**Acceptance Scenarios**:

1. **Given** filtered search results are shown, **When** the user clears the search field and submits, **Then** the table shows the full ticket list again.
2. **Given** the search field is already empty, **When** the user submits, **Then** the table shows the full ticket list (no error message).

---

### User Story 4 - Filter by Status (Priority: P2)

As a support team member, I want to filter the dashboard by ticket status so that I can focus on open, in-progress, or closed work.

**Why this priority**: Status filtering complements keyword search and is a common dashboard task.

**Independent Test**: Select **Open** from the status dropdown; confirm only open tickets appear without a page reload.

**Acceptance Scenarios**:

1. **Given** tickets exist in multiple statuses, **When** the user selects **Open** from the status dropdown, **Then** the table shows only open tickets.
2. **Given** the user selects **All**, **When** they submit with an empty keyword, **Then** the full ticket list is shown.
3. **Given** the user selects a status and enters a keyword, **When** they search, **Then** results match **both** the keyword prefix rules and the selected status.
4. **Given** the user changes the status dropdown, **When** the value changes, **Then** the table refreshes asynchronously with the new filter (with or without keyword).

---

### User Story 2 - Submit Search Without Page Reload (Priority: P2)

As a dashboard user, I want search to run asynchronously so that I stay on the same page and see results immediately.

**Why this priority**: Async UX keeps context and aligns with other ticket UI patterns (status, assignee).

**Independent Test**: Submit search and confirm the browser does not navigate or reload; only the ticket table body updates.

**Acceptance Scenarios**:

1. **Given** the user is on the dashboard, **When** they press Enter in the search field or click the search button, **Then** an async request fetches results and re-renders the ticket list in place.
2. **Given** a search is in progress, **When** results arrive, **Then** prior table rows are replaced by the new result set (or empty state).

---

### Edge Cases

- What happens when the keyword is a partial word (e.g. `permis`)? Only tickets whose **title starts with** that text are returned (not titles where the word appears later).
- What happens when the keyword starts with `ticket-` but no ticket ID shares that prefix? Empty result set; empty-state message shown.
- What happens when the keyword is blank on submit with status **All**? Dashboard fetches and displays the **full ticket list** again—no validation error.
- What happens when the keyword is blank but a specific status is selected? Dashboard shows tickets with that status only.
- What happens when status is **All** and keyword is empty? Full list is loaded (reset behavior).
- What happens when status is set but keyword is empty? Tickets matching that status only are returned.
- What happens when an invalid status value is sent? System rejects the request with a validation error.
- What happens when the keyword contains special characters? Search treats input safely; no script execution; server returns matches or empty set.
- What happens when the user is not authenticated? Search request is rejected with an unauthorized response and the UI shows an error message.
- What happens when many tickets match? Results respect the same bounded page limit as the dashboard list (default 25, max 100).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Dashboard MUST expose a keyword search text input above the ticket list with an explicit search action (Enter key or button).
- **FR-002**: System MUST provide an authenticated GET search endpoint returning JSON for dashboard use.
- **FR-003**: Search MUST restrict queries to the ticket data store and apply **prefix (starts-with) matching**: when keyword does **not** start with `ticket-`, return tickets whose **title begins with** the keyword; when keyword **starts with `ticket-`**, return tickets whose **ticket ID begins with** the keyword (title is not searched in that mode).
- **FR-004**: Search response MUST return matching tickets as a JSON array (within the standard success envelope) including fields needed for dashboard columns: ticket ID, title, status, priority, assignee, last updated.
- **FR-005**: Dashboard MUST call the search endpoint asynchronously with keyword and optional status parameters and re-render the ticket table body from JSON without a full page reload.
- **FR-006**: When the user submits an **empty** keyword (blank after trim) **and** status is **All**, the dashboard MUST reset the table to the **full ticket list** via async fetch—no validation error.
- **FR-007**: Search MUST enforce the same result limit bounds as the existing dashboard list (default 25, maximum 100).
- **FR-008**: Unauthenticated search requests MUST be rejected.
- **FR-009**: Reset-to-full-list MUST use the same list data source as the dashboard initial load and MUST NOT require a full page reload.
- **FR-010**: Dashboard MUST expose a dropdown labeled **Filter By Status** next to the keyword search with options: **All**, **Open**, **In Progress**, **Resolved**, **Closed**, **Cancelled**.
- **FR-011**: Search MUST accept an optional **status** parameter; when provided (not All), results MUST include only tickets with that status.
- **FR-012**: Changing the status dropdown MUST refresh the ticket table asynchronously (same as clicking Search), sending the current keyword and selected status.

### Non-Functional Requirements *(constitution-aligned)*

- **NFR-001**: Solution MUST remain deployable on AEM as a Cloud Service; QueryBuilder queries MUST use bounded `p.limit` (no unbounded queries).
- **NFR-002**: Search keywords and ticket payloads MUST NOT be logged in full.
- **NFR-003**: Behavior MUST include unit tests for search service logic and servlet contract coverage or explicit test follow-up.

### Key Entities

- **Search Query**: User-supplied keyword and optional status filter from the dashboard.
- **Search Result**: Subset of ticket records matching keyword and/or status under the ticket data store, returned as JSON for client rendering.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can find a ticket by typing a **prefix** of the title (e.g. `permis`) or a **prefix** of the ticket ID (e.g. `ticket-8fde`) in under 10 seconds from the dashboard.
- **SC-002**: 100% of search submissions complete without full page reload in acceptance testing.
- **SC-003**: Search returns only tickets from the support ticket data store (no results from unrelated site pages or folders).
- **SC-004**: Empty keyword with status **All** restores full list in 100% of test cases without page reload or error message.
- **SC-005**: Users can filter to a single status (e.g. Open) and see results in under 10 seconds without page reload.

## Assumptions

- Builds on Support Ticket Management System (`specs/001-support-tickets`); ticket data lives at `/var/ai-practical-assessment/tickets`.
- Search matches **title prefix** or **ticket ID prefix** only—per keyword routing rule above—not description, substring-in-middle-of-title, or other fields in v1.
- Status filter values: **open**, **in-progress**, **resolved**, **closed**, **cancelled** (plus **All** = no status filter).
- Dashboard server-renders default list on first load; search/filter replaces rows client-side; **empty keyword + All status** resets to full list via list API.
- Search uses the same authenticated session as other ticket JSON endpoints.

## Dependencies

- `001-support-tickets`: ticket dashboard component, list API pattern, ticket data path, Material UI clientlibs.
- Ticket service layer and QueryBuilder infrastructure in `core`.
