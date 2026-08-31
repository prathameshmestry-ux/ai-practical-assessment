# Design Notes

## Architecture Overview (frontend, backend, database)

AEM Cloud Service app — 3 layers. Frontend: HTL components + JS clientlibs call JSON APIs. Backend: OSGi TicketServiceImpl + Sling servlets handle CRUD, comments, status. Storage: JCR under /var/ai-practical-assessment/tickets/{ticket-id} — not separate DB. UI pages live at /content/ai-practical-assessment/support-tickets/. Service user writes ticket nodes; session user = requester/author. 

## Frontend Design

There are 4 AEM components: `ticket-create`, `ticket-dashboard`, `ticket-detail`, `ticket-comments`. Each has own clientlib — JS does fetch to servlet endpoints. Shared `clientlib-ticket-material` — Material Design (Roboto, cards, data table). Detail page: Jira-style status badge dropdown — only allowed next statuses. Status change = async fetch + in-place DOM update, no full reload. Sling Models (TicketDetailModel, etc.) hydrate server-side data where needed. Detail URL: `ticket.html?ticketId={id}`.

## Backend Design

The core module has OSGi services + Sling servlets. TicketServiceImpl owns create/list/get/update/comment/status logic. Servlets: CreateTicketServlet, TicketListServlet, GetTicketServlet, UpdateTicketServlet, AddCommentServlet, UpdateTicketStatusServlet. ServletAuth gates all endpoints — anonymous → 401. TicketStatusTransitionValidator enforces state machine. TicketJsonMapper standardizes JSON envelope { success, data, error }. AssigneeConfig OSGi + AssignableUserServiceImpl for assignee list. Service user ticket-service for JCR writes.

## Database Design

AEM JCR repository is used as DB. Ticket node: nt:unstructured at /var/ai-practical-assessment/tickets/{ticket-id}. Fields: ticketId, title, description, priority, status, requester, assignee, created, lastModified. Comments: child nodes at .../comments/{comment-id} — commentId, text, author, created. Parent-child linkage only — no cross-ref property. Dashboard query: QueryBuilder on ticket root, filter by ticketId exists, order by lastModified desc. Delete ticket → cascade delete comments.

## Validation Strategy

Client-side: HTML5 + JS — required fields, length limits before submit. Server-side (authoritative): TicketServiceImpl validates all input — blank title/description rejected, title ≤200, description ≤5000, comment ≤4000. Priority enum: low|medium|high. Status changes only via UpdateTicketStatusServlet — not general update endpoint. TicketStatusTransitionValidator blocks invalid transitions. Throws TicketValidationException → servlet returns 400 VALIDATION_ERROR. No partial saves on fail.

## Error Handling Strategy

All APIs return JSON envelope: { success, data, error }. HTTP codes: 401 anonymous, 404 ticket missing, 400 validation or INVALID_STATUS_TRANSITION, 500 unexpected (no PII in message). ServletAuth centralizes auth check — all servlets call it first. UI shows inline error messages on API fail — no silent fail. Ticket not found on detail page → user-friendly message + link back to dashboard. Logs: error level only — no full ticket content or PII.

## Testing Strategy Link

Testing strategy focuses on keeping the ticketing portal fast, secure, and reliable across all user workflows. We test the dashboard to ensure search results and status updates refresh instantly without reloading the page. Security checks confirm that only logged-in employees can view ticket details, while unauthenticated users are properly blocked. We also verify our caching rules so pages load quickly without ever exposing outdated or private ticket data. Finally, AI tools help us quickly create test scenarios and check edge cases before deploying any updates. The `it.tests` module runs automated integration tests directly against a live AEM instance to validate real-world JCR persistence, OSGi services, and HTTP API endpoints.