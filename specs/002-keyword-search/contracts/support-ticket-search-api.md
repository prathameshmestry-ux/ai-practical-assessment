# Contract: Ticket Search API

**Feature**: 002-keyword-search | **Version**: 1.2 | **Date**: 2026-08-27

Base path: `/var/ai-practical-assessment/tickets`

## Search Tickets

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/bin/ai-practical-assessment/search.json` |
| **Servlet** | `SearchTicketsServlet` (path-based) |

**Search matching** (prefix / starts-with only):

| Keyword | Matches |
|---------|---------|
| Does not start with `ticket-` | Ticket **title** begins with keyword (e.g. `permis` → titles starting with `permis`) |
| Starts with `ticket-` | Ticket **ID** begins with keyword (e.g. `ticket-8fde` → IDs starting with `ticket-8fde`); title not searched |

**Status filter** (optional):

| Param value | Behavior |
|-------------|----------|
| omitted or empty | No status filter |
| `open`, `in-progress`, `resolved`, `closed`, `cancelled` | Exact match on ticket `status` property |

**Dashboard client**:

| Condition | Endpoint |
|-----------|----------|
| Blank keyword + blank/All status | `GET {ticketsRoot}.list.json` (reset) |
| Status only, or keyword + status | `GET /bin/.../search.json?status=...&keyword=...` |

**Query parameters**:

| Param | Required | Default | Rules |
|-------|----------|---------|-------|
| `keyword` | No* | — | Prefix match per table above; *at least one of `keyword` or `status` required |
| `status` | No | — | One of: `open`, `in-progress`, `resolved`, `closed`, `cancelled` |
| `offset` | No | `0` | ≥0 |
| `limit` | No | `25` | 1–100 |

**Response** `200 OK` — same envelope as list (`total`, `offset`, `limit`, `tickets[]`).

**Errors**:

| Code | HTTP | When |
|------|------|------|
| `VALIDATION_ERROR` | 400 | Neither keyword nor status provided; invalid status value |
| `UNAUTHORIZED` | 401 | Anonymous session |

## List Reset (Dashboard Client)

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/var/ai-practical-assessment/tickets.list.json` |
| **Servlet** | `TicketListServlet` (001-support-tickets) |

Invoked when keyword and status are both empty/All. See [quickstart.md](../quickstart.md) VS-4.

## Changelog

| Version | Change |
|---------|--------|
| 1.2 | Optional `status` filter; status-only search; dashboard dropdown (session 2026-08-27 e) |
| 1.1 | Prefix (starts-with) matching for title and ticket ID (session 2026-08-27 d) |
| 1.0 | Initial search contract |
