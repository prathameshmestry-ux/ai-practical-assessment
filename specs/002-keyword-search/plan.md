# Implementation Plan: Ticket Dashboard Keyword Search

**Branch**: `002-keyword-search` | **Date**: 2026-08-27 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/002-keyword-search/spec.md`

## Summary

Add keyword search and **status filter** to the ticket dashboard with async ClientLib UX. Authenticated GET path servlet at `/bin/ai-practical-assessment/search.json` queries `/var/ai-practical-assessment/tickets` via QueryBuilder with **prefix (starts-with) matching** for title/ticketId and optional **exact status** predicate (`open`, `in-progress`, `resolved`, `closed`, `cancelled`). Empty keyword + **All** status resets via `/var/ai-practical-assessment/tickets.list.json`.

Builds on 001-support-tickets (`TicketService`, list servlet, dashboard component, JSON envelope).

## Technical Context

**Language/Version**: Java 21 + HTL + dashboard ClientLib JS

**Primary Dependencies**: AEM SDK API, QueryBuilder, existing `TicketService` / servlet JSON helpers (`TicketApiJson`, `TicketJsonMapper`, `ServletAuth`)

**Storage**: Read-only QueryBuilder against `/var/ai-practical-assessment/tickets`

**Testing**: JUnit + AEM Mocks (`TicketServiceImplTest`); `it.tests` search/status IT; manual quickstart VS-1–VS-7

**Target Platform**: AEM as a Cloud Service Author/Publish

**Project Type**: AEM multi-module Maven; incremental feature on Support Ticket Management System

**Performance Goals**: Search results visible within standard web UX (< 3s for ≤100 tickets per 001 SC-002 bounds)

**Constraints**: Bounded `p.limit` (default 25, max 100); no keyword/ticket payload logging; authenticated session required

**Scale/Scope**: 1 new servlet, 1 service method, dashboard HTL/JS/CSS updates; no new Maven modules

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*
*Source: `.specify/memory/constitution.md` (v1.0.2)*

| Principle | Pre-Phase 0 | Post-Phase 1 |
|-----------|-------------|--------------|
| **I. Cloud Service Compliance** | ✅ Pass — QueryBuilder with `p.limit`; no unbounded queries | ✅ Pass — research §2 documents bounded predicates |
| **II. Module & Package Discipline** | ✅ Pass — servlet/service in `core`; HTL/ClientLib in `ui.apps` | ✅ Pass — no new modules |
| **III. Spec-Driven Delivery** | ✅ Pass — spec clarifications complete (path, prefix routing, status filter session e) | ✅ Pass — data model and contracts trace to FR-001–FR-012 |
| **IV. Test & Validation Discipline** | ✅ Pass — unit + IT for search/status; quickstart E2E | ✅ Pass — quickstart VS-1–VS-7 map to user stories |
| **V. Security & Secrets Hygiene** | ✅ Pass — auth on servlet; no keyword logging | ✅ Pass — client uses same-origin fetch only |
| **VI. Adobe Official AI Agent Skills** | ✅ Pass — extends existing ticket dashboard patterns | ✅ Pass — no custom framework introduced |

**Gate result**: PASS — proceed to `/speckit-tasks` / implementation verification.

## Project Structure

### Documentation (this feature)

```text
specs/002-keyword-search/
├── plan.md              # This file
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1
├── contracts/
│   └── support-ticket-search-api.md
├── checklists/
│   └── requirements.md
└── tasks.md             # Phase 2 (/speckit-tasks)
```

### Source Code (repository root)

```text
core/src/main/java/com/ttn/ai/core/
├── constants/TicketConstants.java           # TICKET_SEARCH_SERVLET_PATH
├── services/TicketService.java              # + searchTickets(keyword, status, offset, limit)
├── services/impl/TicketServiceImpl.java     # buildSearchPredicates(), searchTickets()
├── servlets/SearchTicketsServlet.java       # GET /bin/ai-practical-assessment/search.json
└── models/TicketListModel.java              # getSearchApiPath(), getTicketsApiPath()

core/src/test/java/.../TicketServiceImplTest.java

ui.apps/.../components/ticket-dashboard/ticket-dashboard.html
ui.apps/.../clientlibs/clientlib-ticket-dashboard/
    ├── js/ticket-dashboard.js
    ├── js.txt
    └── css/ticket-dashboard.css
```

**Structure Decision**: All backend logic in `core`; presentation and async UX in `ui.apps` ClientLib. List reset reuses existing `TicketListServlet` on ticket data root—no new list endpoint.

## Architecture

### Search API

| Item | Value |
|------|-------|
| Method | `GET` |
| URL | `/bin/ai-practical-assessment/search.json?keyword={keyword}&status={status}&limit=25&offset=0` |
| Servlet | `SearchTicketsServlet` |
| Registration | `@SlingServletPaths("/bin/ai-practical-assessment/search")` + `json` extension |

**QueryBuilder predicates** (see [research.md](./research.md)):

- `path` = `/var/ai-practical-assessment/tickets`
- `type` = `nt:unstructured`
- `property` = `ticketId` (exists)
- Prefix `ticket-` → `1_property=ticketId`, `1_property.value={keyword}%`, `like` (ID starts with keyword)
- Else → `1_property=title`, `1_property.value={keyword}%`, `like` (title starts with keyword)
- Optional `status` → `N_property=status`, `N_property.value={status}` (exact; N is 1 or 2 depending on keyword)
- `p.limit`, `p.offset`, `orderby=@lastModified desc`

**Response** (`200 OK`): standard list envelope (`total`, `offset`, `limit`, `tickets[]`).

### List Reset API (existing)

| Item | Value |
|------|-------|
| Method | `GET` |
| URL | `/var/ai-practical-assessment/tickets.list.json` |
| Servlet | `TicketListServlet` (resource-type binding on tickets root) |

Used when dashboard keyword is blank **and** status is **All** (empty after trim).

### Frontend

| Layer | Change |
|-------|--------|
| HTL | Search input + **Filter By Status** `<select>` (All, Open, In Progress, Resolved, Closed, Cancelled) + button; `data-search-api-url`, `data-tickets-api-url` |
| ClientLib | `ticket-dashboard.js` — send `keyword` + `status` on Search/Enter/status change; reset when both empty |
| CSS | Search row + status dropdown styling in `ticket-dashboard.css` |

## Complexity Tracking

No constitution waivers.

## Implementation Note (Session 2026-08-27 d)

**Complete**: `TicketServiceImpl.buildSearchPredicates()` uses `{keyword}%` for title and ticketId prefix matching.

## Implementation Note (Session 2026-08-27 e)

**Complete**: Status filter dropdown + optional `status` query param on search servlet; `searchTickets(keyword, status, offset, limit)`.
