# Feature Specification: Support Ticket Management System

**Feature Branch**: `001-support-tickets`

**Created**: 2026-08-12

**Status**: Draft

**Input**: User description: "I am building a Support Ticket Management System. A user can create a ticket via the UI. The user can view all tickets from the JCR on the UI Dashboard. From that dashboard UI, user can open a ticket in detail view. user can update ticket fields and reassign. user can also add comments. User can change status from Open to In Progress, In Progress to Resolved, Resolved to Closed, Open to Cancelled, and In Progress to Cancelled. The project is based on aem, so pages should be divided into components."

## Clarifications

### Session 2026-08-12

- Q: Where are tickets and comments stored in the repository? → A: Tickets at `/content/ai-practical-assessment/support-tickets/{ticket-id}`; comments at `/content/ai-practical-assessment/support-tickets/{ticket-id}/comments/{comment-id}`.
- Q: How are comments linked to their ticket? → A: Parent-child JCR structure—each comment is a child node under its ticket's `comments` folder; the ticket node path is the sole linkage (no separate reference property).

### Session 2026-08-14

- Q: Where should support pages live? → A: Under `/content/ai-practical-assessment/support/` (no `us/en` locale segment). Site root `/content/ai-practical-assessment` is `cq:Page`.
- Q: How should ticket UI look? → A: Material Design styling via shared `clientlib-ticket-material` (Roboto, cards, elevated buttons, data table).
- Q: Why are ticket components missing in the editor? → A: Each ticket component has `_cq_dialog`; `componentGroup` is **AI Capability Project - Content**; allowed in `page-content` container policies.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create Support Ticket (Priority: P1)

As a support requester, I want to submit a new support ticket through a form so that my issue is recorded and can be tracked.

**Why this priority**: Ticket creation is the entry point for the entire system. Without it, no other workflows have data to operate on.

**Independent Test**: Can be fully tested by submitting a ticket with required fields and confirming it appears as a new record with status **Open** and a unique identifier.

**Acceptance Scenarios**:

1. **Given** a user is on the create-ticket screen, **When** they enter a title, description, and priority and submit, **Then** a new ticket is created with status **Open**, a creation timestamp, and the user as requester.
2. **Given** a user submits a ticket without required fields, **When** they attempt to save, **Then** the system shows validation errors and does not create the ticket.
3. **Given** a ticket is successfully created, **When** the user is redirected or shown confirmation, **Then** they can see the new ticket identifier and current status **Open**.

---

### User Story 2 - View Tickets Dashboard (Priority: P2)

As a support team member or requester, I want to see all tickets listed on a dashboard so that I can monitor workload and find tickets quickly.

**Why this priority**: Visibility into existing tickets is required before users can open, update, or resolve any item.

**Independent Test**: Can be tested by loading the dashboard and verifying that all persisted tickets appear with key summary fields (identifier, title, status, priority, assignee, last updated).

**Acceptance Scenarios**:

1. **Given** multiple tickets exist, **When** a user opens the dashboard, **Then** all tickets are listed with at least title, status, priority, assignee, and last-updated information.
2. **Given** no tickets exist, **When** a user opens the dashboard, **Then** an empty state message is shown instead of an error.
3. **Given** the dashboard is displayed, **When** a user selects a ticket row or link, **Then** they navigate to that ticket's detail view.

---

### User Story 3 - View and Update Ticket Detail (Priority: P3)

As a support agent, I want to open a ticket's detail view and update its fields or reassign it so that ticket information stays accurate and ownership is clear.

**Why this priority**: Keeping ticket data current is essential for resolution; reassignment enables workload distribution.

**Independent Test**: Can be tested by opening a ticket, changing editable fields (e.g., title, description, priority, assignee), saving, and confirming persisted values on reload.

**Acceptance Scenarios**:

1. **Given** a ticket exists, **When** a user opens it from the dashboard, **Then** the detail view shows all ticket fields, current status, assignee, requester, and timestamps.
2. **Given** a user is on the detail view, **When** they update editable fields and save, **Then** changes are persisted and reflected on subsequent views.
3. **Given** a user reassigns a ticket to another valid assignee, **When** they save, **Then** the assignee field updates and remains visible on the dashboard and detail view.
4. **Given** a user enters invalid data (e.g., empty title), **When** they attempt to save, **Then** validation errors are shown and no partial save occurs.

---

### User Story 4 - Add Comments to Tickets (Priority: P4)

As a support agent or requester, I want to add comments on a ticket so that communication and investigation history are captured in one place.

**Why this priority**: Comments document progress and decisions; they support collaboration without changing core ticket fields.

**Independent Test**: Can be tested by opening a ticket, posting a comment, and verifying it appears in chronological order with author and timestamp.

**Acceptance Scenarios**:

1. **Given** a user is on a ticket detail view, **When** they enter comment text and submit, **Then** the comment is saved as a child node under that ticket's `comments` folder and displayed with author name and timestamp.
2. **Given** multiple comments exist on a ticket, **When** the detail view loads, **Then** comments are shown in chronological order (oldest first or newest first—consistently applied).
3. **Given** a user submits an empty comment, **When** they attempt to post, **Then** the system rejects the submission with a validation message.

---

### User Story 5 - Manage Ticket Status Workflow (Priority: P5)

As a support agent, I want to change a ticket's status through allowed transitions so that ticket lifecycle accurately reflects progress from open through closure or cancellation.

**Why this priority**: Status drives reporting and workflow; enforcing valid transitions prevents inconsistent ticket states.

**Independent Test**: Can be tested by attempting each allowed transition and verifying disallowed transitions are blocked with clear feedback.

**Acceptance Scenarios**:

1. **Given** a ticket with status **Open**, **When** an agent changes status to **In Progress**, **Then** the new status is saved and shown on dashboard and detail views.
2. **Given** a ticket with status **In Progress**, **When** an agent changes status to **Resolved**, **Then** the status updates successfully.
3. **Given** a ticket with status **Resolved**, **When** an agent changes status to **Closed**, **Then** the status updates successfully and the ticket is treated as closed for active work.
4. **Given** a ticket with status **Open**, **When** an agent changes status to **Cancelled**, **Then** the status updates successfully.
5. **Given** a ticket with status **In Progress**, **When** an agent changes status to **Cancelled**, **Then** the status updates successfully.
6. **Given** a ticket with status **Closed** or **Cancelled**, **When** an agent attempts any status change, **Then** the system blocks the transition and explains that the ticket is in a terminal state.
7. **Given** a ticket with status **Open**, **When** an agent attempts to change directly to **Resolved** or **Closed**, **Then** the transition is blocked with a clear message (only **In Progress** and **Cancelled** are valid targets from **Open**).

---

### Edge Cases

- What happens when a user opens a ticket that no longer exists or was removed? System shows a not-found message and offers return to dashboard.
- How does the system handle invalid status transitions (e.g., Resolved → Open, Closed → In Progress)? Transition is rejected; current status unchanged; user sees reason.
- What happens when required fields are cleared on update? Save is blocked with field-level validation errors.
- How does the system behave when the dashboard contains a large number of tickets? List remains usable (pagination or reasonable default page size); user can still open any visible ticket.
- What happens when two users update the same ticket concurrently? Last successful save wins for non-status fields, or user is notified if their save conflicts (either approach is acceptable if documented and consistent).
- Can comments be added on **Closed** or **Cancelled** tickets? Comments remain allowed for audit trail unless ticket is archived (out of scope for v1—assume comments allowed on all non-deleted tickets).
- What happens if a ticket node is removed? All child comment nodes under that ticket's `comments` folder are removed with it; no orphan comments remain.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST allow authenticated users to create a support ticket with at minimum title, description, and priority.
- **FR-002**: System MUST assign each new ticket status **Open**, a unique identifier, requester, created timestamp, and last-updated timestamp.
- **FR-003**: System MUST validate required fields on create and update; invalid submissions MUST NOT persist partial data.
- **FR-004**: System MUST display a dashboard listing all tickets with summary fields: identifier, title, status, priority, assignee, and last updated.
- **FR-005**: System MUST allow users to open any listed ticket in a detail view showing full ticket data, status, assignee, requester, and timestamps.
- **FR-006**: System MUST allow authorized users to update editable ticket fields (title, description, priority) from the detail view.
- **FR-007**: System MUST allow authorized users to reassign a ticket to a valid assignee from the detail view.
- **FR-008**: System MUST allow users to add text comments on a ticket; each comment MUST record author and timestamp.
- **FR-009**: System MUST display comments on the ticket detail view in a consistent chronological order.
- **FR-010**: System MUST support only these status transitions:
  - **Open** → **In Progress**
  - **Open** → **Cancelled**
  - **In Progress** → **Resolved**
  - **In Progress** → **Cancelled**
  - **Resolved** → **Closed**
- **FR-011**: System MUST reject any status transition not listed in FR-010 and leave the current status unchanged.
- **FR-012**: System MUST treat **Closed** and **Cancelled** as terminal states where further status changes are not permitted.
- **FR-013**: System MUST persist all tickets and comments so they survive session end and are available on subsequent visits.
- **FR-014**: System MUST provide navigation from dashboard to detail view and a path back to the dashboard.
- **FR-015**: System MUST create each new ticket at repository path `/content/ai-practical-assessment/support-tickets/{ticket-id}`, where `{ticket-id}` is a unique identifier for that ticket.
- **FR-016**: System MUST create each new comment at repository path `/content/ai-practical-assessment/support-tickets/{ticket-id}/comments/{comment-id}`, where `{comment-id}` is a unique identifier within that ticket.
- **FR-017**: System MUST link each comment to exactly one ticket via parent-child repository structure; comments MUST NOT be stored outside their ticket's `comments` folder or require a separate cross-reference property to locate the parent ticket.

### Non-Functional Requirements *(constitution-aligned)*

- **NFR-001**: Solution MUST be deployable on AEM as a Cloud Service (no removed/incompatible APIs or on-prem-only assumptions).
- **NFR-002**: Secrets and credentials MUST NOT appear in code, content packages, tests, or docs; ticket content and personally identifiable information MUST NOT be logged in full.
- **NFR-003**: Behavior changes MUST identify required unit, integration, and/or UI test coverage (or an explicit deferred-test rationale).
- **NFR-004**: User-facing pages MUST be composed of discrete, reusable UI regions so create, list, detail, and comment interactions can be maintained independently.

### Key Entities

- **Ticket**: A support request record stored at `/content/ai-practical-assessment/support-tickets/{ticket-id}`. Key attributes: unique identifier (`{ticket-id}`), title, description, priority (e.g., Low/Medium/High), status (Open, In Progress, Resolved, Closed, Cancelled), requester, assignee (optional until assigned), created timestamp, last-updated timestamp. Parent of zero or more comment child nodes.
- **Comment**: A user-authored note stored at `/content/ai-practical-assessment/support-tickets/{ticket-id}/comments/{comment-id}`. Key attributes: unique identifier (`{comment-id}`), comment text, author, timestamp. Belongs to exactly one ticket via parent-child repository structure under that ticket's `comments` folder.
- **User**: An authenticated person who can create tickets, view the dashboard, update tickets, reassign, comment, and change status (role nuances may be refined in planning; v1 assumes all authenticated users can perform agent actions unless restricted later).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can create a complete support ticket (title, description, priority) in under 2 minutes on first attempt.
- **SC-002**: Dashboard displays all existing tickets within 3 seconds for up to 100 tickets under normal load.
- **SC-003**: 100% of allowed status transitions (per FR-010) complete successfully when attempted by an authorized user.
- **SC-004**: 100% of disallowed status transitions are blocked with a user-visible explanation and no status change.
- **SC-005**: 95% of users can locate and open a specific ticket from the dashboard within 30 seconds.
- **SC-006**: After posting a comment, the comment appears on the detail view within 1 second without a full page reload (or with clear refresh feedback).

## Assumptions

- Users are authenticated before accessing ticket features; standard platform login applies.
- Tickets are stored at `/content/ai-practical-assessment/support-tickets/{ticket-id}`; comments are child nodes at `.../comments/{comment-id}`. The dashboard queries ticket nodes under the support-tickets folder; the detail view loads a ticket node and its `comments` child nodes.
- v1 includes a single support queue (no multi-department or multi-project ticket pools).
- Assignee is selected from a predefined list of support agents (sourced from platform user directory or a configured list).
- Priority values are a fixed set: Low, Medium, High (default Medium if not specified on create).
- Email or push notifications on create, assign, or status change are out of scope for v1.
- Ticket deletion and bulk operations are out of scope for v1.
- Search, filtering, and sorting on the dashboard are desirable but optional for v1; a flat list of all tickets satisfies the minimum requirement.
- Page layout will use modular UI regions (e.g., separate areas for ticket form, ticket list, ticket detail, and comments) to align with component-based authoring practices on the target platform.
