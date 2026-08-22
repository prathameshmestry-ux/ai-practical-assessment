# Contract: Support Ticket API (Sling Servlets)

**Feature**: 001-support-tickets | **Version**: 1.0 | **Date**: 2026-08-12

Base path: `/content/ai-practical-assessment/support-tickets`

All endpoints require an authenticated AEM session. Anonymous requests receive
`401 Unauthorized`. Invalid input receives `400 Bad Request` with JSON error body.
Successful mutations return `200 OK` or `201 Created` with JSON body.

**CSRF**: POST requests on Author MUST include `:cq_csrf_token` header or parameter
(AEM granite CSRF).

## Common Response Envelope

```json
{
  "success": true,
  "data": { },
  "error": null
}
```

Error:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "INVALID_STATUS_TRANSITION",
    "message": "Cannot transition from open to resolved"
  }
}
```

## Endpoints

### 1. Create Ticket

| | |
|---|---|
| **Method** | `POST` |
| **Path** | `/content/ai-practical-assessment/support-tickets.ticket.json` |
| **Servlet** | `CreateTicketServlet` |
| **Story** | US1 |

**Request body** (`application/json`):

```json
{
  "title": "Cannot access dashboard",
  "description": "User sees blank page after login.",
  "priority": "high"
}
```

| Field | Required | Rules |
|-------|----------|-------|
| `title` | Yes | Non-blank, ≤200 chars |
| `description` | Yes | Non-blank, ≤5000 chars |
| `priority` | No | `low` \| `medium` \| `high`; default `medium` |

**Response** `201 Created`:

```json
{
  "success": true,
  "data": {
    "ticketId": "ticket-a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "path": "/content/ai-practical-assessment/support-tickets/ticket-a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "open",
    "requester": "admin",
    "created": "2026-08-12T12:00:00Z"
  },
  "error": null
}
```

---

### 2. List Tickets (Dashboard)

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/content/ai-practical-assessment/support-tickets.list.json` |
| **Servlet** | `TicketListServlet` (or Sling Model JSON exporter) |
| **Story** | US2 |

**Query parameters**:

| Param | Default | Rules |
|-------|---------|-------|
| `offset` | `0` | ≥0 |
| `limit` | `25` | 1–100 |

**Response** `200 OK`:

```json
{
  "success": true,
  "data": {
    "total": 42,
    "offset": 0,
    "limit": 25,
    "tickets": [
      {
        "ticketId": "ticket-a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        "title": "Cannot access dashboard",
        "status": "open",
        "priority": "high",
        "assignee": null,
        "requester": "admin",
        "lastModified": "2026-08-12T12:00:00Z"
      }
    ]
  },
  "error": null
}
```

---

### 3. Get Ticket Detail

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/content/ai-practical-assessment/support-tickets/{ticket-id}.json` |
| **Story** | US3 |

**Response** `200 OK`: full ticket object including all fields.

**Response** `404`: ticket not found.

---

### 4. Update Ticket

| | |
|---|---|
| **Method** | `POST` |
| **Path** | `/content/ai-practical-assessment/support-tickets/{ticket-id}.update.json` |
| **Servlet** | `UpdateTicketServlet` |
| **Story** | US3 |

**Request body** (partial update allowed):

```json
{
  "title": "Updated title",
  "description": "Updated description",
  "priority": "medium",
  "assignee": "support-agent-1"
}
```

| Field | Required | Rules |
|-------|----------|-------|
| `title` | No | If present: non-blank, ≤200 |
| `description` | No | If present: non-blank, ≤5000 |
| `priority` | No | Valid enum |
| `assignee` | No | Must be in assignee config list; empty string clears assignee |

Cannot update `status` via this endpoint (use status endpoint).

**Response** `200 OK`: updated ticket object.

---

### 5. Add Comment

| | |
|---|---|
| **Method** | `POST` |
| **Path** | `/content/ai-practical-assessment/support-tickets/{ticket-id}/comments.comment.json` |
| **Servlet** | `AddCommentServlet` |
| **Story** | US4 |

**Request body**:

```json
{
  "text": "Investigating the issue now."
}
```

**Response** `201 Created`:

```json
{
  "success": true,
  "data": {
    "commentId": "comment-f9e8d7c6-b5a4-3210-fedc-ba9876543210",
    "text": "Investigating the issue now.",
    "author": "support-agent-1",
    "created": "2026-08-12T12:30:00Z"
  },
  "error": null
}
```

---

### 6. Update Ticket Status

| | |
|---|---|
| **Method** | `POST` |
| **Path** | `/content/ai-practical-assessment/support-tickets/{ticket-id}/status.status.json` |
| **Servlet** | `UpdateTicketStatusServlet` |
| **Story** | US5 |

**Request body**:

```json
{
  "status": "in-progress"
}
```

| Value | Allowed when current status is |
|-------|-------------------------------|
| `in-progress` | `open` |
| `cancelled` | `open`, `in-progress` |
| `resolved` | `in-progress` |
| `closed` | `resolved` |

**Response** `200 OK`: envelope `{ success: true, data: { ticketId, status, lastModified, ... } }`. Client uses `data.status` and `data.lastModified` for in-place DOM update (FR-010c)—no page reload required.

**Response** `400` + `INVALID_STATUS_TRANSITION`: disallowed transition.

---

## Error Codes

| Code | HTTP | Description |
|------|------|-------------|
| `UNAUTHORIZED` | 401 | No authenticated session |
| `NOT_FOUND` | 404 | Ticket or comment not found |
| `VALIDATION_ERROR` | 400 | Field validation failed |
| `INVALID_STATUS_TRANSITION` | 400 | Status change not allowed (FR-011) |
| `INTERNAL_ERROR` | 500 | Unexpected server error (no PII in message) |

## HTL / Component Integration

| Component | Read | Write |
|-----------|------|-------|
| `ticket-create` | — | POST Create Ticket |
| `ticket-dashboard` | GET List (or Sling Model) | — |
| `ticket-detail` | GET Detail + Sling Model | POST Update (fields), POST Status (Jira-style badge menu → Fetch + in-place DOM update, FR-010c) |
| `ticket-comments` | GET Detail comments[] | POST Add Comment |

Detail page URL: `/content/ai-practical-assessment/support-tickets/ticket.html?ticketId={ticket-id}`
