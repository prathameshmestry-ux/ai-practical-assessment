# API Contract

## Endpoint: Create Ticket

**Method**: POST 
**Path**: /var/ai-practical-assessment/tickets.ticket.json
**Purpose**: Create new ticket

#### Request

```
{ "title": "Cannot access dashboard", "description": "User sees blank page.", "priority": "high" }
```

#### Response { `201` }

```
{ "success": true, "data": { "ticketId": "ticket-uuid", "path": "/var/.../ticket-uuid", "title": "...", "status": "open", "priority": "high", "requester": "admin", "assignee": null, "created": "...", "lastModified": "..." }, "error": null }
```

#### Validation Rules

- title required, non-blank, ≤200
- description required, non-blank, ≤5000 
- priority optional — low|medium|high, default medium 
- requester = session user (server-set)

#### Error Responses
- 401 — {}
- 400 VALIDATION_ERROR — bad/missing fields, invalid JSON 

---

## Endpoint: List Tickets

**Method**: GET
**Path**: /var/ai-practical-assessment/tickets.list.json
**Purpose**: Dashboard Ticket list

#### Request

```
Query: ?offset=0&limit=25
```

#### Response { `200` }

```
{ "success": true, "data": { "total": 42, "offset": 0, "limit": 25, "tickets": [{ "ticketId": "...", "title": "...", "status": "open", "priority": "high", "assignee": null, "requester": "admin", "lastModified": "..." }] }, "error": null }
```

#### Validation Rules
- offset ≥0, default 0
- limit 1–100, default 25

#### Error Responses
- 401 — {}
- 500 INTERNAL_ERROR — serialization fail

---

## Endpoint: Get Ticket

**Method**: `GET` 
**Path**: /var/ai-practical-assessment/tickets/{ticket-id}.json
**Purpose**: Full ticket + comments

#### Request
```
/var/ai-practical-assessment/tickets/{ticket-id}.json
```

#### Response { `200` }
```
{ "success": true, "data": { "ticketId": "...", "title": "...", "description": "...", "priority": "medium", "status": "open", "requester": "admin", "assignee": null, "created": "...", "lastModified": "...", "comments": [{ "commentId": "...", "text": "...", "author": "admin", "created": "..." }] }, "error": null }
```
#### Validation Rules
`{ticket-id}` must exist under ticket root
#### Error Responses
- 401 — {}
- 404 NOT_FOUND — ticket missing
---

## Endpoint: Update Ticket

**Method**: POST
**Path**: /var/ai-practical-assessment/tickets/{ticket-id}.update.json
**Purpose**: Update fields / reassign

#### Request
```
{ "title": "Updated", "description": "Updated desc", "priority": "medium", "assignee": "support-agent-1" }
```
#### Response { `200` }
```
{ "success": true, "data": { "ticketId": "...", "title": "Updated", "status": "open", "assignee": "support-agent-1", "comments": [], "lastModified": "..." }, "error": null }
```

#### Validation Rules
- `title` if sent: non-blank, ≤200
- `description` if sent: non-blank, ≤5000
- `priority` if sent: valid enum
- `assignee` if sent: user ID; "" clears assignee

#### Error Responses
- 401 — {}
- 404 NOT_FOUND
- 400 VALIDATION_ERROR
---
 
## Endpoint: Add Comment

**Method**: POST
**Path**: /var/ai-practical-assessment/tickets/{ticket-id}/comments.comment.json
**Purpose**: Add comment to ticket

#### Request
```
{ "text": "Investigating now." }
```
#### Response { `200` }
```
{ "success": true, "data": { "commentId": "comment-uuid", "text": "Investigating now.", "author": "admin", "created": "..." }, "error": null }
```
#### Validation Rules
- `text` required, non-blank, ≤4000
- `author` = session user (server-set)

#### Error Responses
- 401 — {}
- 404 NOT_FOUND
- 400 VALIDATION_ERROR

---
 
## Endpoint: Update Status

**Method**: POST
**Path**: /var/ai-practical-assessment/tickets/{ticket-id}.status.json 
**Purpose**: Ticket Status transition

#### Request
```
{ "status": "in-progress" }
```
#### Response { `200` }
```
{ "success": true, "data": { "ticketId": "...", "status": "in-progress", "lastModified": "..." }, "error": null }
```
#### Validation Rules
- open → in-progress, cancelled
- in-progress → resolved, cancelled
- resolved → closed
- closed / cancelled → none (terminal)

#### Error Responses
- 401 — {}
- 404 NOT_FOUND
- 400 INVALID_STATUS_TRANSITION
- 400 VALIDATION_ERROR — bad JSON
---
 
## Endpoint: List Assignees

**Method**: GET 
**Path**: /var/ai-practical-assessment/tickets.assignees.json
**Purpose**: Assignee Field

#### Request
```

```
#### Response { `200` }
```
{ "success": true, "data": { "users": [{ "id": "admin", "displayName": "Admin" }] }, "error": null }
```
#### Validation Rules
Returns `devs` group members only

#### Error Responses
- 401 — {}
- 500 INTERNAL_ERROR

---
 
## Endpoint: Search Tickets

**Method**: GET 
**Path**: /bin/ai-practical-assessment/search.json
**Purpose**: Search & Status Filter

#### Request
```
Query: ?keyword=permis&status=open&offset=0&limit=25
```
#### Response { `200` }
```
{ total, offset, limit, tickets[] }
```
#### Validation Rules
- Need at least one of keyword or status
- keyword: prefix match on title; if starts ticket- → prefix match on ID
- status: open|in-progress|resolved|closed|cancelled
- offset/limit same as list

#### Error Responses
- 401 — {}
- 400 VALIDATION_ERROR — no keyword+status, invalid status


## Shared error shape:
```
{ "success": false, "data": null, "error": { "code": "VALIDATION_ERROR", "message": "Title is required" } }
```