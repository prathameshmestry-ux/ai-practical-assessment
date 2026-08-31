# Data Model: Ticket Dashboard Keyword Search

**Feature**: 002-keyword-search | **Date**: 2026-08-27

Read-only search and status filter over existing ticket nodes in [001-support-tickets/data-model.md](../001-support-tickets/data-model.md). No new persisted entities.

## Ticket Data Root (unchanged)

```text
/var/ai-practical-assessment/tickets/
└── {ticket-id}/                    # nt:unstructured
    ├── ticketId    (String)
    ├── title       (String)
    ├── status      (String)        # open | in-progress | resolved | closed | cancelled
    ├── priority    (String)
    ├── assignee    (String, optional)
    ├── lastModified (Date)
    └── ...
```

Search queries MUST scope to this path only (SC-003).

## Logical Entities

### Search Query (transient)

Dashboard keyword + optional status; not stored.

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `keyword` | String | No* | Trimmed; prefix match when present |
| `status` | String | No | One of: `open`, `in-progress`, `resolved`, `closed`, `cancelled`; omit/`All` = no filter |
| `offset` | Integer | No | Default `0`; ≥ 0 |
| `limit` | Integer | No | Default `25`; 1–100 |

\*At least one of `keyword` or `status` required for search API.

**Keyword matching**:

| Condition | Query target | Match style |
|-----------|--------------|-------------|
| Keyword starts with `ticket-` | `ticketId` | Prefix — ID **begins with** keyword |
| Otherwise (keyword present) | `title` | Prefix — title **begins with** keyword |

**Status matching**: Exact match on `status` property when param provided.

**Reset rule**: Blank keyword **and** status **All** → list API (not search).

### Search Result (response)

Same envelope as list API.

| Field | Type | Description |
|-------|------|-------------|
| `total` | Long | Total matches |
| `offset` | Integer | Request offset |
| `limit` | Integer | Page size |
| `tickets` | `TicketDto[]` | `ticketId`, `title`, `status`, `priority`, `assignee`, `lastModified` |

### List Reset (transient operation)

| Trigger | API | Outcome |
|---------|-----|---------|
| Empty keyword + All status | `GET .../tickets.list.json` | Full bounded list |
| Status only (no keyword) | Search API with `status` | Tickets matching status |
| Initial page load | `TicketListModel.listTickets()` | Server-rendered rows |

## Dashboard UI State

| State | Visible UI | Data source |
|-------|------------|-------------|
| Initial / reset | Table or default empty | Server render or list JSON |
| Filtered | Table or “no matches” | Search JSON (keyword and/or status) |
| Error | `#ticket-dashboard-search-error` | Failed fetch |

## Relationships

```text
Search Query ──(GET)──► Search API ──QueryBuilder──► Ticket nodes
     │
     ├── status only ──► Search API (status predicate)
     └── blank keyword + All ──► List API

Search Result ──(JSON)──► ClientLib ──DOM──► Dashboard table
```

## Validation Summary

| Rule | Layer |
|------|-------|
| Neither keyword nor status on search API | Service → 400 |
| Invalid status value | Service → 400 |
| Blank keyword + All on dashboard | Client → list API |
| Unauthenticated | Servlet → 401 |
| Limit bounds | Service 1–100 |
| Path scope | Fixed ticket data root |
