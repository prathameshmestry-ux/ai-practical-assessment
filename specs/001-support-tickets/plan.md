# Implementation Plan: Support Ticket Management System

**Branch**: `001-support-tickets` | **Date**: 2026-08-26 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-support-tickets/spec.md`

## Summary

Deliver a Support Ticket Management System on AEM as a Cloud Service where authenticated
users create tickets, browse a dashboard, open ticket detail views, update fields,
reassign, add comments, and transition status through a enforced workflow. Ticket detail
shows **unified Jira-style status badge** (current status = dropdown trigger with chevron;
menu lists allowed next statuses only) per FR-010a/b. Status changes use **async Fetch** in
`clientlib-ticket-detail` with **in-place DOM update**—no full page reload (FR-010c). Tickets
persist as JCR nodes at `/var/ai-practical-assessment/tickets/{ticket-id}`;
comments are child nodes at `.../comments/{comment-id}`. Support **UI pages** remain under
`/content/ai-practical-assessment/support-tickets/` (dashboard, create-ticket, ticket detail).

**Technical approach**: OSGi services and Sling Models in `core` own ticket/comment
persistence and business rules; four HTL components in `ui.apps` provide create,
dashboard, detail, and comment UI regions; JSON Sling servlets handle write
operations; sample pages and folder structure ship in `ui.content`.

## Technical Context

**Language/Version**: Java 21 (`maven.compiler.release=21`, `.cloudmanager/java-version=21`) + HTL; Node/npm for `ui.frontend` (clientlibs as needed)

**Primary Dependencies**: AEM SDK API (`aem-sdk-api`), Sling Models, OSGi R7 annotations, Apache Sling Query/QueryBuilder, Jackson (if already in project; otherwise manual JSON in servlets)

**Storage**: JCR data nodes under `/var/ai-practical-assessment/tickets/` (tickets) with `comments/` child folders (comments). UI pages under `/content/ai-practical-assessment/support-tickets/` unchanged.

**Testing**: JUnit + AEM Mocks (`core`), AEM Testing Clients (`it.tests`), Cypress (`ui.tests` for primary journeys)

**Target Platform**: AEM as a Cloud Service (Author/Publish) + Dispatcher/CDN

**Project Type**: AEM multi-module Maven project (Cloud Service archetype); product: Support Ticket Management System

**Performance Goals**: Dashboard lists up to 100 tickets within 3 seconds (SC-002); bounded QueryBuilder queries with `p.limit` pagination (default page size 25)

**Constraints**: AEMaaCS-compatible APIs only; no secrets in packages; bounded queries; no full PII in logs; service-user writes to `/var/ai-practical-assessment/tickets`; CSRF token on POST servlets

**Scale/Scope**: 4 custom components, 1 OSGi service layer (~5 classes), 4–5 servlets, 3 AEM pages, unit + integration tests; no new Maven modules

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*
*Source: `.specify/memory/constitution.md` (AI Capability Project Constitution v1.0.2)*

| Principle | Pre-Phase 0 | Post-Phase 1 |
|-----------|-------------|--------------|
| **I. Cloud Service Compliance** | ✅ Pass — Resource API, QueryBuilder with limits, no deprecated APIs | ✅ Pass — design uses standard Sling/OSGi patterns; pagination documented in research |
| **II. Module & Package Discipline** | ✅ Pass — `core`, `ui.apps`, `ui.content`, `ui.config`, tests only | ✅ Pass — no new modules; components in `ui.apps`, logic in `core` |
| **III. Spec-Driven Delivery** | ✅ Pass — spec + clarify complete; plan follows FR/NFR | ✅ Pass — data model and contracts trace to FR-001–FR-010c |
| **IV. Test & Validation Discipline** | ✅ Pass — unit (service, status machine), IT (servlets), UI (optional Cypress) planned | ✅ Pass — quickstart defines validation scenarios per story |
| **V. Security & Secrets Hygiene** | ✅ Pass — service user mapping in `ui.config`; no PII logging | ✅ Pass — servlet auth checks session user; CSRF on writes |
| **VI. Adobe Official AI Agent Skills** | ✅ Pass — use `create-component` skill for HTL components | ✅ Pass — component creation follows archetype + skill patterns |

**Gate result**: PASS — proceed to implementation tasks (`/speckit-tasks`).

## Project Structure

### Documentation (this feature)

```text
specs/001-support-tickets/
├── plan.md              # This file
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1
├── contracts/
│   └── support-ticket-api.md
└── tasks.md             # Phase 2 (/speckit-tasks — not yet created)
```

### Source Code (repository root)

```text
core/src/main/java/com/ttn/ai/core/
├── models/
│   ├── TicketModel.java
│   ├── TicketListModel.java
│   ├── TicketDetailModel.java
│   └── CommentModel.java
├── services/
│   ├── TicketService.java
│   ├── impl/TicketServiceImpl.java
│   └── TicketStatusTransitionValidator.java
├── servlets/
│   ├── CreateTicketServlet.java
│   ├── UpdateTicketServlet.java
│   ├── AddCommentServlet.java
│   ├── UpdateTicketStatusServlet.java
│   └── AssigneeUsersServlet.java
└── constants/
    └── TicketConstants.java

core/src/test/java/com/ttn/ai/core/
├── services/TicketServiceImplTest.java
└── services/TicketStatusTransitionValidatorTest.java

ui.apps/src/main/content/jcr_root/apps/ai-practical-assessment/components/
├── ticket-create/          # componentGroup: AI Capability Project - Content; _cq_dialog
├── ticket-dashboard/
├── ticket-detail/
└── ticket-comments/

ui.config/src/main/content/jcr_root/apps/ai-practical-assessment/osgiconfig/config/
├── org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended-ai-practical-assessment.cfg.json
├── com.ttn.ai.core.services.impl.TicketServiceImpl.cfg.json   # ticketRootPath → /var/ai-practical-assessment/tickets
└── org.apache.sling.jcr.repoinit.RepositoryInitializer~ai-practical-assessment.cfg.json   # create /var path + ACLs

ui.content/src/main/content/jcr_root/content/ai-practical-assessment/
└── support-tickets/                    # UI pages only (no ticket data nodes)
    ├── dashboard/                      # ticket-dashboard
    ├── create-ticket/                  # ticket-create
    └── ticket/                         # ticket-detail + ticket-comments

it.tests/src/main/java/com/ttn/ai/it/tests/
└── SupportTicketServletIT.java

ui.frontend/                            # optional: clientlibs for AJAX form POST
ui.tests/                               # optional Cypress journeys
```

**Structure Decision**: Business logic and JCR access stay in `core` (constitution II).
Each user-facing concern is a separate AEM component (NFR-004 / spec assumption).
Write operations use dedicated servlets (JSON) invoked from component clientlibs or
form POST with redirect. Ticket **data** lives under `/var/ai-practical-assessment/tickets/`; **pages**
live under `/content/ai-practical-assessment/support-tickets/`.

## Component & Page Map

| Page | Path | Components | User Story |
|------|------|------------|------------|
| Dashboard | `/content/ai-practical-assessment/support-tickets/dashboard` | `ticket-dashboard` | US2 |
| Create Ticket | `/content/ai-practical-assessment/support-tickets/create-ticket` | `ticket-create` | US1 |
| Ticket Detail | `/content/ai-practical-assessment/support-tickets/ticket` | `ticket-detail`, `ticket-comments` | US3–US5 |

Detail page reads `ticketId` from query parameter (`?ticketId={ticket-id}`).

### Ticket detail — status control (FR-010a / FR-010b / FR-010c)

| State | UI | Save behavior |
|-------|-----|---------------|
| Non-terminal | **Unified status badge button** (`#ticket-status-trigger`): colored block showing current status + down chevron; click opens **dropdown menu** (`#ticket-status-menu`) with **only** `TicketDetailModel.allowedStatusTargets`—**one `<button>` menu item per status** via `<sly data-sly-list>` inside `<ul>` | Menu item click → Fetch POST `.../status.status.json`; **no page reload**; JS updates badge label, color class, menu items, and `#ticket-last-modified` from response `data` (FR-010c) |
| Terminal (`closed`, `cancelled`) | Read-only colored badge (`ticket-mui-status-btn--readonly`)—no chevron, no menu | No status POST; JS may convert interactive dropdown to read-only badge when transition reaches terminal state |

**HTL anchors**: `#ticket-status-root` (`data-current-status`), `#ticket-status-dropdown`, `#ticket-status-trigger`, `#ticket-status-menu`, `#ticket-last-modified`, `#ticket-terminal-notice` (hidden until terminal).

**HTL rule**: Do **not** put `data-sly-list` on `<option>` (Set iteration merges labels). Use `<sly data-sly-list>` inside menu `<ul>` with one `<button data-status>` per target. Do **not** use native `<select>` for status.

**Styling (FR-010b)**: `ticket-mui-status-btn` with status-specific color modifiers (`--open`, `--in-progress`, etc.); elevated `ticket-mui-status-menu`; Material Symbols chevron (`expand_more`). Dashboard list still uses `ticket-mui-chip` for status column.

**ClientLib (FR-010c)**: `clientlib-ticket-detail` (`ticket-detail.js`) toggles menu; intercepts menu item click; POSTs via Fetch with CSRF header; on success calls `applyStatusToDom()`—updates badge, rebuilds menu from client transition map (mirrors `TicketStatusTransitionValidator`), or replaces dropdown with read-only badge + shows terminal notice. **Must not** call `window.location.reload()`.

### Ticket detail — inline assignee (FR-007 / FR-007a / FR-007b)

| UI | Behavior |
|----|----------|
| Meta `#ticket-assignee-root` | Read-only `#ticket-assignee-display` (name or "Unassigned"); click shows `#ticket-assignee-editor` with `#ticket-assignee-input` + `#ticket-assignee-suggestions` |
| First edit | Fetch GET `.../support-tickets.assignees.json` → cache `users[]` in `sessionStorage` (`ticket-assignees-cache`) |
| Typing | Filter cached users by `id` / `displayName` locally—no new HTTP calls |
| Selection | POST `.../{ticket-id}.update.json` `{ "assignee": "user-id" }`; update display + `#ticket-last-modified` in place |

**Servlet**: `AssigneeUsersServlet` — bulk list from `AssignableUserService` (`devs` group direct members; UserManager `Group.getMembers()`).

**HTL**: `data-assignees-url` on `.cmp-ticket-detail` from `TicketDetailModel.assigneesApiPath`. Assignee removed from edit form `<select>`.

### Data persistence paths (FR-015–FR-021, Session 2026-08-26)

| Concern | Path | Notes |
|---------|------|-------|
| Ticket data root | `/var/ai-practical-assessment/tickets` | All create/list/update/status operations |
| Ticket node | `/var/ai-practical-assessment/tickets/{ticket-id}` | Created by `CreateTicketServlet` / `TicketServiceImpl` |
| Comment folder | `/var/ai-practical-assessment/tickets/{ticket-id}/comments` | Parent for comment child nodes |
| Comment node | `/var/ai-practical-assessment/tickets/{ticket-id}/comments/{comment-id}` | `AddCommentServlet` read/write |
| Create API | `POST /var/ai-practical-assessment/tickets.ticket.json` | `CreateTicketServlet`; `clientlib-ticket-create` fetch target |
| List API | `GET /var/ai-practical-assessment/tickets.list.json` | `TicketListServlet`; dashboard query root |
| Comment API | `POST /var/ai-practical-assessment/tickets/{ticket-id}/comments.comment.json` | `AddCommentServlet`; `clientlib-ticket-comments` fetch target |
| UI pages | `/content/ai-practical-assessment/support-tickets/{dashboard\|create-ticket\|ticket}` | Unchanged — HTL passes `data-ticket-path` pointing to `/var/.../tickets/{id}` |

**Repoinit**: Creates service user and `/home/users` read ACL only; `/var/ai-practical-assessment` folder tree + `rep:policy` ship in `ui.content`.

**ui.content var tree**:

```text
ui.content/src/main/content/jcr_root/var/ai-practical-assessment/
├── .content.xml              # sling:Folder
├── _rep_policy.xml           # ticket-service ACL (hierarchy)
└── tickets/
    └── .content.xml          # sling:Folder + tickets-root resource type
```

**ClientLibs**: All folders under `ui.apps/.../clientlibs/` MUST set `allowProxy="{Boolean}true"`.

All four ticket components include `_cq_dialog` (optional heading) and use
`componentGroup="AI Capability Project - Content"` so they appear in the page editor.

## Build & Java 21 Configuration

All Java build targets MUST align with constitution v1.0.2 (Java 21 mandatory).

| Setting | Location | Value |
|---------|----------|-------|
| Cloud Manager JDK | `.cloudmanager/java-version` | `21` |
| Compiler release | root `pom.xml` `<maven.compiler.release>` | `21` |
| `maven-compiler-plugin` | root `pom.xml` `<release>` | `${maven.compiler.release}` (21) |
| Enforcer `requireJavaVersion` | root `pom.xml` | `21.0.0` minimum |
| IT module compiler | `it.tests/pom.xml` | inherits parent (`21`) |
| OSGi execution environment | root `pom.xml` Bnd `bnd` block | `Bundle-RequiredExecutionEnvironment: JavaSE-21` |

**OSGi bundle targets**: The `core` bundle (and any future Java bundles) compile to
Java 21 bytecode via the parent compiler settings; Bnd emits `JavaSE-21` in bundle
manifests. No module may set `source`/`target` below 21.

**Local prerequisite**: JDK 21 for `mvn clean install` and AEM SDK Quickstart
(runtime JDK requirements for the SDK itself remain separate from project bytecode).

## Complexity Tracking

> No constitution violations requiring waiver. Standard AEM service-user + servlet pattern.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |
