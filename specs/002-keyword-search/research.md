# Research: Ticket Dashboard Keyword Search

**Feature**: 002-keyword-search | **Date**: 2026-08-27

## 1. Search Servlet Registration

**Decision**: Register `SearchTicketsServlet` as a **path-based** Sling servlet at `/bin/ai-practical-assessment/search` with `GET` + `json` extension (`/bin/ai-practical-assessment/search.json`).

**Rationale**: Clarified in spec session 2026-08-27. Path servlets decouple search from the ticket data resource type and dashboard page URL. Matches existing bin-path patterns for ticket writes and keeps the search endpoint stable regardless of `/var` vs `/content` data-root changes.

**Alternatives considered**:
- **Resource-type + selector** on `RT_TICKETS_ROOT` (e.g. `/var/.../tickets.search.json`): Rejected—couples search URL to data root; harder for ClientLib to discover without duplicating path logic.
- **Dashboard page selector** (`dashboard.search.json`): Rejected—search is data operation, not page operation.

## 2. Query Strategy & Field Matching

**Decision**: Use QueryBuilder with bounded predicates on `/var/ai-practical-assessment/tickets`:
- Base: `type=nt:unstructured`, `ticketId` property exists, `orderby=@lastModified desc`, `p.limit` (default 25, max 100).
- **Prefix (starts-with) rule** (clarify session 2026-08-27 d): If keyword starts with `ticket-` → `like` on `ticketId` with value `{keyword}%` (ID begins with keyword). Else → `like` on `title` with value `{keyword}%` (title begins with keyword). No `%keyword%` contains matching.

**Rationale**: Satisfies FR-003/FR-007 and constitution I (bounded queries). Users type partial prefixes (`permis`, `ticket-8fde`) and expect matches only at the **start** of title or ID—not substring matches mid-title. Prefix routing disambiguates ID lookup from title search.

**Alternatives considered**:
- **Contains (`%keyword%`) matching**: Rejected—spec session d; e.g. `permis` must not match "Access permission issue".

## 3. Empty Search / Reset Behavior

**Decision**: Dashboard ClientLib treats **blank keyword + All status** as **reset**: fetch `GET /var/ai-practical-assessment/tickets.list.json`. **Status-only** filter calls search servlet with `status` param (no keyword).

**Rationale**: FR-006/FR-009/FR-011. List API for full reset; search servlet for keyword and/or status.

**Alternatives considered**:
- **Validation error on empty submit with All**: Rejected—poor UX.
- **Full page reload**: Rejected—violates SC-002.
- **Search servlet returns all when both empty**: Rejected—blurs search vs list.

## 4. Frontend Integration

**Decision**: Extend `ticket-dashboard` HTL with search input, **status `<select>`** (All + five statuses), button, `data-search-api-url`, `data-tickets-api-url`. ClientLib `runFilter()` sends keyword + status on Search/Enter/status `change`; reset when both empty/All.

**Rationale**: Aligns with existing async patterns (status, assignee on detail page). Server-rendered first paint preserved; search is progressive enhancement.

**Alternatives considered**:
- **Webpack/ui.frontend module**: Heavier build for single dashboard script; rejected—follow existing ticket ClientLib pattern in `ui.apps`.
- **HTL-only filter form with page reload**: Rejected—violates SC-002.

## 5. Authentication & Response Envelope

**Decision**: Reuse `ServletAuth.isAuthenticated` and `TicketApiJson` / `TicketJsonMapper` from 001. Search returns same `TicketListResult` shape as list (`total`, `offset`, `limit`, `tickets[]`).

**Rationale**: Consistent client parsing; single DTO mapping path. Anonymous requests return 401 (FR-008).

**Alternatives considered**:
- **Custom search-only JSON schema**: Rejected—unnecessary divergence from list API.

## 6. Testing Strategy

**Decision**: Unit tests for keyword/status validation and `buildSearchPredicates()` in `TicketServiceImplTest`. IT `testSearchByStatusOnly` in `SupportTicketServletIT`. Manual quickstart VS-1–VS-7.

**Rationale**: NFR-003; predicate selection (prefix + status index) is primary risk.

**Alternatives considered**:
- **Fulltext index (`fulltext` predicate)**: Broader matching but harder to restrict to title-only vs ID-only; rejected for v1 scoped search.
- **Search both title and ticketId always**: Rejected—user clarified ID-only mode when keyword starts with `ticket-`.
- **JCR-SQL2**: Less idiomatic in AEM services; QueryBuilder reuses list pagination patterns.

## 7. Prefix Matching Semantics (Session 2026-08-27 d)

**Decision**: Both title and ticket ID searches use **starts-with** semantics only. Partial keywords are supported (`permis`, `ticket-8fde`).

**Rationale**: User clarified search intent—find tickets by leading characters of title or ID, not arbitrary substring.

**Implementation note** (plan/tasks): `TicketServiceImpl.buildSearchPredicates()` uses `keyword + "%"` for title and ticketId prefix match.

**Alternatives considered**:
- **Case-insensitive prefix**: Not specified in v1; QueryBuilder `like` is case-sensitive by default—document in quickstart if users report mismatches.

## 8. Status Filter (Session 2026-08-27 e)

**Decision**: Optional `status` query param on search servlet. Allowed values: `open`, `in-progress`, `resolved`, `closed`, `cancelled`. QueryBuilder adds `N_property=status`, `N_property.value={status}` (exact match). At least one of keyword or status required. Dashboard dropdown triggers async refresh on change.

**Rationale**: FR-010–FR-012; combines with prefix keyword predicates without new endpoint.

**Alternatives considered**:
- **Separate status-only list servlet**: Rejected—duplicate of search with one predicate.
- **Client-side filter only**: Rejected—does not scale; violates bounded server query pattern.
