# Implementation Plan: [FEATURE]

**Branch**: `[###-feature-name]` | **Date**: [DATE] | **Spec**: [link]

**Input**: Feature specification from `/specs/[###-feature-name]/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

[Extract from feature spec: primary requirement + technical approach from research]

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: Java 17 (see `.cloudmanager/java-version` and `maven.compiler.release` in root `pom.xml`) + HTL; Node/npm for `ui.frontend`

**Primary Dependencies**: AEM SDK API (`aem-sdk-api`), Sling Models, OSGi, Core Components, Webpack clientlibs

**Storage**: JCR repository (AEM content) / N/A for pure code changes

**Testing**: JUnit (`core`), AEM Testing Clients (`it.tests`), Cypress (`ui.tests`)

**Target Platform**: AEM as a Cloud Service (Author/Publish) + Dispatcher/CDN

**Project Type**: AEM multi-module Maven project (Cloud Service); product: Support Ticket Management System

**Performance Goals**: [domain-specific, e.g., ticket list TTFB, cache hit ratio, or NEEDS CLARIFICATION]

**Constraints**: AEMaaCS-compatible APIs only; no secrets in packages; explicit HTTP timeouts; bounded queries; no full PII in logs

**Scale/Scope**: [domain-specific, e.g., ticket flows/components touched, or NEEDS CLARIFICATION]

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*
*Source: `.specify/memory/constitution.md` (AI Capability Project Constitution v1.0.0)*

- **I. Cloud Service Compliance**: No removed/CS-incompatible APIs; timeouts on
  outbound HTTP; no unbounded queries without pagination plan; analyse/CM gates
  remain viable
- **II. Module & Package Discipline**: Changes map to correct modules (`core`,
  `ui.apps`, `ui.frontend`, `ui.config`, `ui.content`, `dispatcher`, tests);
  no unjustified new modules/frameworks
- **III. Spec-Driven Delivery**: Spec → plan → tasks → implement; ambiguities
  resolved in spec, not silently in code; scope stays Support Ticket Management
- **IV. Test & Validation Discipline**: Unit / IT / UI tests planned at the
  right level; gaps explicitly documented
- **V. Security & Secrets Hygiene**: No secrets, auth bypasses, or insecure
  client patterns; ticket/PII not logged in full
- **Complexity**: Any deviation from smallest viable design listed in
  Complexity Tracking below

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)
<!--
  ACTION REQUIRED: Trim the tree to modules this feature actually touches.
  Keep AEMaaCS package boundaries from the constitution.
-->

```text
core/                   # OSGi bundle: services, Sling Models, servlets
ui.frontend/            # Webpack/TS/Sass → clientlibs
ui.apps/                # Components, HTL, clientlibs, apps content
ui.config/              # OSGi configs
ui.content/             # Mutable/sample content
dispatcher/             # Cloud Dispatcher config
it.tests/               # AEM Testing Clients ITs (*IT.java)
ui.tests/               # Cypress UI tests
all/                    # Aggregate deploy package
```

**Structure Decision**: [Document which modules this feature touches and why]

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
