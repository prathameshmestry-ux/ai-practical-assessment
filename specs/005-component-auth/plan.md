# Implementation Plan: Component-Level Ticket Authentication

**Branch**: `005-component-auth` | **Date**: 2026-08-27 | **Spec**: [spec.md](./spec.md)

## Summary

Replace any **page-level / OSGi global redirect** auth pattern with **component-level** and **servlet-level** checks. Support ticket **pages stay public** so anonymous users see the Header Login link. Ticket Dashboard, Ticket Detail, and Create Ticket components expose `isLoggedIn` and gate HTL with `data-sly-test`. Ticket servlets return **401** with **`{}`** for anonymous sessions before business logic runs.

## Security Architecture (updated)

| Layer | Before (avoid) | After (this feature) |
|-------|----------------|----------------------|
| AEM pages | Global/OSGi redirect to login | **Public** — no redirect |
| Header | Login vs Welcome (unchanged) | Same — entry point for auth |
| Dashboard / Detail / Create HTL | Full UI for all visitors | **`isLoggedIn`** → full UI or login message |
| Dashboard / Detail models | Always load ticket data | **Skip load** when anonymous |
| Ticket servlets | 401 with error envelope | **401 + `{}`** immediately |
| Search servlet | 401 with error envelope | **401 + `{}`** (dashboard client) |

```text
Anonymous request
  → Page renders (Header: Login)
  → TicketListModel / TicketDetailModel / TicketCreateModel: isLoggedIn=false, no service calls
  → HTL: "Please log in to view and manage tickets"
  → Direct API call: ServletAuth → 401 {}

Authenticated request
  → Page renders (Header: Welcome + Logout)
  → Models load data; HTL shows full UI
  → API: existing success/error contracts
```

## Technical Context

**Modules**: `core` (models, `ServletAuth`, `TicketApiJson`, servlets), `ui.apps` (HTL)

**Auth rule**: `ResourceResolver.getUserID()` not equal to `anonymous` (shared via `ServletAuth.isAuthenticated`)

**Servlets in scope**:

- Resource types under `/var/ai-practical-assessment/tickets`: list, get, create, update, status, comments, assignees
- `/bin/ai-practical-assessment/search` (dashboard keyword search)

**Testing**: `TicketListModelTest`, `TicketDetailModelTest`, `TicketCreateModelTest`, `TicketApiJsonTest`; `mvn clean test -pl core`

## Constitution Check

| Principle | Status |
|-----------|--------|
| Cloud Service Compliance | Pass — session checks only; no new APIs |
| Module Discipline | Pass — `core` + `ui.apps` |
| Spec-Driven Delivery | Pass — spec → plan → tasks |
| Test Discipline | Pass — unit tests for gate + 401 |
| Security | Pass — defense in depth; no data in anonymous HTML |

**Gate result**: PASS

## Project Structure

```text
specs/005-component-auth/
├── spec.md
├── plan.md
├── tasks.md
└── checklists/requirements.md

core/.../models/TicketCreateModel.java    # isLoggedIn for create form gate
core/.../models/TicketListModel.java      # isLoggedIn, skip list when anonymous
core/.../models/TicketDetailModel.java    # isLoggedIn, skip getTicket when anonymous
core/.../servlets/ServletAuth.java        # requireAuthenticated helper
core/.../servlets/TicketApiJson.java      # writeUnauthorized → {}
core/.../servlets/*Servlet.java           # use requireAuthenticated
ui.apps/.../ticket-create/ticket-create.html
ui.apps/.../ticket-dashboard/ticket-dashboard.html
ui.apps/.../ticket-detail/ticket-detail.html
```
