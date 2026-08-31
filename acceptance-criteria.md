## Core

1. A user can create a ticket via the UI.
2. A user can view all tickets from the database.
3. A user can open a ticket detail view.
4. A user can update ticket fields and reassign.
5. A user can add comments.
6. Status changes only through valid transitions; invalid ones are rejected.
7. Keyword search and status filter work.
8. Data remains available after restart.
9. Backend validation prevents invalid records.
10.  No secrets committed to the repo.
11. State-machine integration tests pass.

## Validation

- Create reject blank/missing title or description
- Title ≤200 chars; description ≤5000 chars
- Priority must be low | medium | high; default medium if omitted
- Update reject blank title or description if field sent
- Assignee must be in OSGi assignee list; empty string clears assignee
- Comment reject blank text; text ≤4000 chars
- Invalid status transition rejected — current status unchanged
- No partial save on validation fail
- status not updatable via general update endpoint — only status endpoint
- Error Handling
- Anonymous request → 401 Unauthorized
- Ticket not found → 404 + user-friendly message + link to dashboard
- Validation fail → 400 with field-level error in JSON envelope
- Invalid status transition → 400 + code INVALID_STATUS_TRANSITION
- Server error → 500 — no PII in message
- All API responses use { success, data, error } envelope
- Author POST mutations require CSRF token (:cq_csrf_token)
- UI show clear feedback on API errors (not silent fail)

## Testing

- Unit tests: TicketServiceImpl — create, update, list, comment, status
- Unit tests: TicketStatusTransitionValidator — all allowed + disallowed transitions
- Integration test: full servlet flow — create → list → get → update → comment → status
- Test auth gate — anonymous gets 401
- Test terminal states — closed/cancelled status change blocked
- Test pagination — list respects offset/limit (default 25, max 100)
- Test empty dashboard — no crash
- Test not-found ticket — 404 returned

## Documentation

- API contract documented — 6 endpoints, request/response shapes, error codes
- Data model documented — JCR paths, fields, status state machine
- Assignee config documented — OSGi PID + how to add agents
- Page URLs documented — dashboard, create, detail (?ticketId=)
- Component-to-API mapping documented per component
- Edge cases documented — concurrent edits policy, comments on closed tickets, cascade delete
- Quickstart/README cover local build + deploy + manual test steps