# Research: Support Ticket Management System

**Feature**: 001-support-tickets | **Date**: 2026-08-12

## 1. Ticket & Comment Persistence

**Decision**: Store tickets and comments as `nt:unstructured` JCR nodes via the Sling
Resource API; writes performed by a dedicated OSGi `TicketService` using a service
resource resolver mapped to a service user with write access under
`/content/ai-practical-assessment/support-tickets`.

**Rationale**: Matches clarified spec paths (FR-015–FR-017); avoids custom node types
and CND overhead for v1; aligns with AEM archetype patterns and Cloud Service
compatibility.

**Alternatives considered**:
- **Content Fragments**: Better for headless; heavier authoring model and CF APIs for
  simple CRUD; rejected for assessment scope.
- **Oak indexes + custom node types**: Stronger schema enforcement; unnecessary for v1
  ticket volume.
- **External database**: Violates AEM-first content model and adds infrastructure.

## 2. Write Operations (Create, Update, Comment, Status)

**Decision**: Four Sling servlets registered on fixed paths under
`/content/ai-practical-assessment/support-tickets` (see contracts) accepting
`POST` with `application/json` or `application/x-www-form-urlencoded`; return JSON
responses. Authenticated session required; CSRF token validated on Author.

**Rationale**: HTL components remain presentation-focused; servlets centralize
validation, status rules, and service-user writes. Idempotent GET stays on Sling
Models for read paths.

**Alternatives considered**:
- **Sling POST to component resource type**: Tighter coupling to page structure;
  harder to test and reuse.
- **AEM Forms**: Overkill for custom ticket workflow.
- **Workflow-driven status**: Valid for enterprise; rejected as over-engineering for v1.

## 3. Dashboard Query Strategy

**Decision**: Use QueryBuilder with `path=/content/ai-practical-assessment/support-tickets`,
`type=nt:unstructured`, property `ticketId` exists, `p.limit=25` (configurable),
`p.offset` for pagination. Sort by `lastModified` descending.

**Rationale**: Satisfies constitution I (bounded queries) and SC-002 (100 tickets
within 3s via pagination). Flat child listing via `Resource.listChildren()` works
for &lt;50 tickets but does not scale; QueryBuilder is standard for dashboards.

**Alternatives considered**:
- **Unbounded `listChildren`**: Simple but fails constitution at scale; rejected.
- **JCR-SQL2 direct**: Less portable; QueryBuilder is AEM-idiomatic.

## 4. Ticket ID Generation

**Decision**: Generate JCR-safe node names as `ticket-{uuid}` (lowercase, hyphenated);
expose same value as `ticketId` property for queries and URLs.

**Rationale**: UUID avoids collisions; sanitization prevents invalid JCR node names;
human-readable prefix aids support debugging.

**Alternatives considered**:
- **Sequential integers**: Require counter node and cluster-safe locking.
- **Title-based slugs**: Collision risk and renaming complexity.

## 5. Assignee Source

**Decision**: v1 uses an OSGi configuration list of assignee user IDs (editable in
`ui.config`) plus optional enrichment from `UserManager` for display names on
Author/Publish.

**Rationale**: Spec assumes predefined agent list; avoids external LDAP integration
in v1 while remaining Cloud Service compatible.

**Alternatives considered**:
- **Dynamic group query**: Better UX; deferred—can replace config list later without
  API contract change.

## 6. Comment Display Order

**Decision**: Oldest first (ascending `created` timestamp) on detail view.

**Rationale**: Standard audit-trail reading order; resolves spec ambiguity (US4).

**Alternatives considered**:
- **Newest first**: Common in chat UIs; less natural for ticket history.

## 7. Authentication & Authorization

**Decision**: All ticket pages require authenticated AEM users (standard login).
v1: any authenticated user may perform all actions (per spec assumption). Servlet
layer rejects anonymous requests with HTTP 401.

**Rationale**: Matches spec; role-based ACLs deferred to future iteration.

**Alternatives considered**:
- **Requester vs agent roles**: Higher security; out of scope for v1 per spec.

## 8. Component Implementation

**Decision**: Create four components using Adobe `create-component` skill patterns:
`ticket-create`, `ticket-dashboard`, `ticket-detail`, `ticket-comments` under
`apps/ai-practical-assessment/components/`. Each with HTL, Sling Model, `_cq_dialog`,
and clientlib for POST/AJAX. `componentGroup="AI Capability Project - Content"`.

**Rationale**: Satisfies NFR-004 and user requirement for component-based pages;
mirrors existing `helloworld` component structure in the project.

**Alternatives considered**:
- **Single monolithic component**: Violates modularity requirement.

## 10. Page Structure & UI

**Decision**: Support pages at `/content/ai-practical-assessment/support/{create|dashboard|ticket}`.
Site root is `cq:Page`. Ticket components use group **AI Capability Project - Content**,
each with `_cq_dialog` (optional heading), and are allowed in `page-content` container policies. UI uses Material Design tokens via
`clientlib-ticket-material` (Roboto, cards, chips, elevated buttons).

**Rationale**: Flat path avoids unused `us/en` locale segment; dedicated component group
surfaces ticket components in editor; Material styling improves UX without React/MUI bundle.

**Alternatives considered**:
- **Locale path `us/en/support`**: Matches archetype default; rejected per product direction.
- **React MUI in ui.frontend**: Heavier build; rejected for HTL-first assessment scope.

## 9. Testing Strategy

**Decision**:
- **Unit**: `TicketServiceImpl`, `TicketStatusTransitionValidator` with AEM Mocks.
- **Integration**: Servlet POST/GET contracts via `it.tests` against local SDK.
- **UI**: Cypress smoke for create → dashboard → detail → comment (optional v1).

**Rationale**: Constitution IV; status machine and servlet contracts are highest
regression risk.

**Alternatives considered**:
- **UI-only testing**: Misses business rule coverage for FR-010/FR-011.
