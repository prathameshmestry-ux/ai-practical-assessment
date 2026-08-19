# Implementation Plan: Support Ticket Management System

**Branch**: `001-support-tickets` | **Date**: 2026-08-12 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-support-tickets/spec.md`

## Summary

Deliver a Support Ticket Management System on AEM as a Cloud Service where authenticated
users create tickets, browse a dashboard, open ticket detail views, update fields,
reassign, add comments, and transition status through a enforced workflow. Tickets
persist as JCR nodes at `/content/ai-practical-assessment/support-tickets/{ticket-id}`;
comments are child nodes at `.../comments/{comment-id}`.

**Technical approach**: OSGi services and Sling Models in `core` own ticket/comment
persistence and business rules; four HTL components in `ui.apps` provide create,
dashboard, detail, and comment UI regions; JSON Sling servlets handle write
operations; sample pages and folder structure ship in `ui.content`.

## Technical Context

**Language/Version**: Java 21 (`maven.compiler.release=21`, `.cloudmanager/java-version=21`) + HTL; Node/npm for `ui.frontend` (clientlibs as needed)

**Primary Dependencies**: AEM SDK API (`aem-sdk-api`), Sling Models, OSGi R7 annotations, Apache Sling Query/QueryBuilder, Jackson (if already in project; otherwise manual JSON in servlets)

**Storage**: JCR content nodes under `/content/ai-practical-assessment/support-tickets/` (tickets) with `comments/` child folders (comments)

**Testing**: JUnit + AEM Mocks (`core`), AEM Testing Clients (`it.tests`), Cypress (`ui.tests` for primary journeys)

**Target Platform**: AEM as a Cloud Service (Author/Publish) + Dispatcher/CDN

**Project Type**: AEM multi-module Maven project (Cloud Service archetype); product: Support Ticket Management System

**Performance Goals**: Dashboard lists up to 100 tickets within 3 seconds (SC-002); bounded QueryBuilder queries with `p.limit` pagination (default page size 25)

**Constraints**: AEMaaCS-compatible APIs only; no secrets in packages; bounded queries; no full PII in logs; service-user writes to ticket content path; CSRF token on POST servlets

**Scale/Scope**: 4 custom components, 1 OSGi service layer (~5 classes), 4–5 servlets, 3 AEM pages, unit + integration tests; no new Maven modules

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*
*Source: `.specify/memory/constitution.md` (AI Capability Project Constitution v1.0.1)*

| Principle | Pre-Phase 0 | Post-Phase 1 |
|-----------|-------------|--------------|
| **I. Cloud Service Compliance** | ✅ Pass — Resource API, QueryBuilder with limits, no deprecated APIs | ✅ Pass — design uses standard Sling/OSGi patterns; pagination documented in research |
| **II. Module & Package Discipline** | ✅ Pass — `core`, `ui.apps`, `ui.content`, `ui.config`, tests only | ✅ Pass — no new modules; components in `ui.apps`, logic in `core` |
| **III. Spec-Driven Delivery** | ✅ Pass — spec + clarify complete; plan follows FR/NFR | ✅ Pass — data model and contracts trace to FR-001–FR-017 |
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
│   └── UpdateTicketStatusServlet.java
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
└── org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended-ai-practical-assessment.cfg.json

ui.content/src/main/content/jcr_root/content/ai-practical-assessment/
<<<<<<< Updated upstream
├── support-tickets/                    # ticket data root (empty on deploy)
└── support/
    ├── create/                         # page with ticket-create component
    ├── dashboard/                      # page with ticket-dashboard component
    └── ticket/                         # page with ticket-detail + ticket-comments
=======
└── support-tickets/                    # tickets-root (API + data)
    ├── dashboard/                      # ticket-dashboard
    ├── create-ticket/                  # ticket-create
    └── ticket/                         # ticket-detail + ticket-comments
>>>>>>> Stashed changes

it.tests/src/main/java/com/ttn/ai/it/tests/
└── SupportTicketServletIT.java

ui.frontend/                            # optional: clientlibs for AJAX form POST
ui.tests/                               # optional Cypress journeys
```

**Structure Decision**: Business logic and JCR access stay in `core` (constitution II).
Each user-facing concern is a separate AEM component (NFR-004 / spec assumption).
Write operations use dedicated servlets (JSON) invoked from component clientlibs or
form POST with redirect. Ticket **data** lives under `support-tickets/`; **pages**
<<<<<<< Updated upstream
live under `support/` directly under the site root (no `us/en` locale segment).
=======
live under `support-tickets/` as sibling pages to ticket data nodes.
>>>>>>> Stashed changes

## Component & Page Map

| Page | Path | Components | User Story |
|------|------|------------|------------|
<<<<<<< Updated upstream
| Create Ticket | `/content/ai-practical-assessment/support/create` | `ticket-create` | US1 |
| Dashboard | `/content/ai-practical-assessment/support/dashboard` | `ticket-dashboard` | US2 |
| Ticket Detail | `/content/ai-practical-assessment/support/ticket` | `ticket-detail`, `ticket-comments` | US3–US5 |
=======
| Dashboard | `/content/ai-practical-assessment/support-tickets/dashboard` | `ticket-dashboard` | US2 |
| Create Ticket | `/content/ai-practical-assessment/support-tickets/create-ticket` | `ticket-create` | US1 |
| Ticket Detail | `/content/ai-practical-assessment/support-tickets/ticket` | `ticket-detail`, `ticket-comments` | US3–US5 |
>>>>>>> Stashed changes

Detail page reads `ticketId` from query parameter (`?ticketId={ticket-id}`).

All four ticket components include `_cq_dialog` (optional heading) and use
`componentGroup="AI Capability Project - Content"` so they appear in the page editor.

<<<<<<< Updated upstream
## Build & Java 17 Configuration

All Java build targets MUST align with constitution v1.0.1 (Java 17 mandatory).

| Setting | Location | Value |
|---------|----------|-------|
| Cloud Manager JDK | `.cloudmanager/java-version` | `17` |
| Compiler release | root `pom.xml` `<maven.compiler.release>` | `17` |
| `maven-compiler-plugin` | root `pom.xml` `<release>` | `${maven.compiler.release}` (17) |
| Enforcer `requireJavaVersion` | root `pom.xml` | `17.0.0` minimum |
| IT module compiler | `it.tests/pom.xml` | inherits parent (`17`) |
| OSGi execution environment | root `pom.xml` Bnd `bnd` block | `Bundle-RequiredExecutionEnvironment: JavaSE-17` |

**OSGi bundle targets**: The `core` bundle (and any future Java bundles) compile to
Java 17 bytecode via the parent compiler settings; Bnd emits `JavaSE-17` in bundle
manifests. No module may set `source`/`target` below 17.

**Local prerequisite**: JDK 17 for `mvn clean install` and AEM SDK Quickstart
=======
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
>>>>>>> Stashed changes
(runtime JDK requirements for the SDK itself remain separate from project bytecode).

## Complexity Tracking

> No constitution violations requiring waiver. Standard AEM service-user + servlet pattern.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |
