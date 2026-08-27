# Feature Specification: IST Timestamp Display

**Feature Branch**: `003-ist-date-format`

**Created**: 2026-08-27

**Status**: Draft

**Input**: Global UI date formatting — `DD/MM/YY HH:MM` in `Asia/Kolkata` (IST) for ticket creation, last modification, and comment creation across dashboard, detail, comments, and JSON APIs.

## Clarifications

### Session 2026-08-27

- Q: Which display format and timezone? → A: **`dd/MM/yy HH:mm`** pattern, **`Asia/Kolkata`** timezone, shown to users as **DD/MM/YY HH:MM** (e.g. `27/08/26 11:00`).
- Q: Where must formatting happen? → A: All Java paths that expose dates to UI (Sling Models via `TicketService`, GET servlet JSON). HTL uses pre-formatted strings from models; if HTL ever binds raw `Calendar`/`Date`, use `@ format='dd/MM/yy HH:mm', timezone='Asia/Kolkata'`.
- Q: Client-side async updates? → A: ClientLib displays date strings from API responses; no client-side reformatting—server returns IST strings.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consistent Ticket Timestamps (Priority: P1)

As a support user, I want ticket created and last-modified times shown in IST with a consistent format so I can read dates quickly across pages.

**Why this priority**: Core visibility on dashboard and detail pages.

**Independent Test**: Open dashboard and ticket detail; verify **Created** and **Last Modified** match `DD/MM/YY HH:MM` IST pattern.

**Acceptance Scenarios**:

1. **Given** a ticket exists, **When** the user views the dashboard **Last Updated** column, **Then** the value uses `dd/MM/yy HH:mm` in IST.
2. **Given** a ticket detail page, **When** the user views metadata, **Then** **Created** and **Last Modified** use the same IST format.
3. **Given** a status or assignee update via async JSON, **When** **Last Modified** refreshes in the UI, **Then** the new value remains in IST format.

---

### User Story 2 - Comment Timestamps (Priority: P2)

As a support user, I want comment times in IST so discussion order and timing are clear.

**Independent Test**: View comments on a ticket; each comment timestamp matches IST format.

**Acceptance Scenarios**:

1. **Given** comments on a ticket, **When** the comments list renders, **Then** each comment **created** time uses `dd/MM/yy HH:mm` IST.
2. **Given** a new comment is posted, **When** the page reloads or list updates, **Then** the new comment timestamp uses IST format.

---

### User Story 3 - API JSON Consistency (Priority: P2)

As an integrator, I want list/search/detail JSON to return the same IST date strings as the UI.

**Independent Test**: Call list or search JSON; `created` and `lastModified` fields match UI format.

**Acceptance Scenarios**:

1. **Given** authenticated list API, **When** tickets are returned, **Then** date fields are IST-formatted strings—not ISO-8601 instant strings.

---

### Edge Cases

- Null or missing JCR date property → empty or omitted display; no error.
- Server runs in non-IST JVM timezone → display still IST (formatter timezone explicit).
- Midnight boundary UTC vs IST → user sees IST calendar date/time.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: All user-visible ticket **created** timestamps MUST display as `dd/MM/yy HH:mm` in **Asia/Kolkata**.
- **FR-002**: All user-visible ticket **last modified** timestamps MUST use the same format and timezone.
- **FR-003**: All user-visible comment **created** timestamps MUST use the same format and timezone.
- **FR-004**: Java code mapping JCR `Calendar`/date properties to UI or JSON strings MUST use a formatter with pattern `dd/MM/yy HH:mm` and timezone `Asia/Kolkata`.
- **FR-005**: GET servlet JSON responses MUST return the same formatted strings for date fields.
- **FR-006**: HTL templates that format raw date properties directly MUST use `@ format='dd/MM/yy HH:mm', timezone='Asia/Kolkata'` (current templates use service-formatted strings).
- **FR-007**: ClientLib code MUST display server-provided date strings without converting to another format.

### Non-Functional Requirements

- **NFR-001**: Single shared formatter utility in `core`—no duplicate patterns in servlets/models.
- **NFR-002**: Unit tests MUST verify sample UTC instant maps to expected IST string.

### Key Entities

- **Display Timestamp**: Transient string derived from JCR date; not stored; pattern `dd/MM/yy HH:mm`, zone `Asia/Kolkata`.

## Success Criteria *(mandatory)*

- **SC-001**: 100% of ticket date fields on dashboard and detail match `DD/MM/YY HH:MM` IST in acceptance testing.
- **SC-002**: 100% of comment timestamps match the same format.
- **SC-003**: List and search JSON date fields match UI display format in sample API checks.

## Assumptions

- Applies to Support Ticket Management UI and ticket JSON APIs (`001-support-tickets`, `002-keyword-search` dashboard).
- Two-digit year (`yy`) per specified pattern.
- No locale-specific month names; numeric date only.

## Dependencies

- `001-support-tickets`: ticket DTOs, dashboard, detail, comments components.
- `002-keyword-search`: dashboard async search re-renders `lastModified` from JSON.
