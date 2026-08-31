# AI Capability Project

An [AEM as a Cloud Service](https://experienceleague.adobe.com/en/docs/experience-manager-cloud-service/content/overview/architecture) application built with the Java 21 stack. The project implements a **Support Ticket Management System** with Material Design UI, REST-style Sling servlets, and component-level authentication.

Production deployments run through Adobe Cloud Manager full-stack pipelines. Local development uses Maven against the AEM SDK Quickstart and the Dispatcher Tools for cache/security validation.

## Features

| Feature | Description |
|---------|-------------|
| **Support Tickets** | Create, list, view, update, reassign, comment on, and transition ticket status via the UI and JSON APIs |
| **Keyword Search** | Dashboard prefix search by title or ticket ID, with optional status filter — no page reload |
| **IST Timestamps** | All ticket and comment dates displayed as `DD/MM/YY HH:MM` in `Asia/Kolkata` |
| **Header Branding** | Authorable logo and site name with login/logout and welcome message |
| **Component Auth** | Public pages with login prompt in ticket components; APIs return `401` for anonymous users |
| **Login Redirect** | Login link returns users to the page they started from after authentication |

Detailed specifications live under [`specs/`](specs/).

## Prerequisites

- **JDK 21** (required — see `.cloudmanager/java-version`)
- **Maven 3.x**
- **AEM as a Cloud Service SDK** running locally (default author: `http://localhost:4502`)
- **Node.js** (for `ui.frontend` and `ui.tests` local development)

Configure the [Adobe Maven repository](https://experienceleague.adobe.com/en/docs/experience-manager-cloud-service/content/implementing/developing/aem-as-a-cloud-service-sdk) in your `settings.xml` before the first build.

## Quick Start

Build and deploy the full package to a local author instance:

```bash
mvn clean install -PautoInstallSinglePackage
```

Log in at `http://localhost:4502` (`admin` / `admin`) and open:

| Page | URL |
|------|-----|
| Dashboard | http://localhost:4502/content/ai-practical-assessment/support-tickets/dashboard.html |
| Create Ticket | http://localhost:4502/content/ai-practical-assessment/support-tickets/create-ticket.html |
| Ticket Detail | http://localhost:4502/content/ai-practical-assessment/support-tickets/ticket.html?ticketId={ticket-id} |

See [`specs/001-support-tickets/quickstart.md`](specs/001-support-tickets/quickstart.md) for end-to-end validation scenarios.

### Assignee Users Setup

The ticket detail **Assignee** dropdown loads assignable users from the AEM `devs` user group. A content package is provided with the `devs` group and sample AEM users as direct members.

| Item | Value |
|------|-------|
| Package name | `Assignees Users-Groups.zip` |
| Location | [`./resources/Assignees Users-Groups.zip`](./resources/Assignees%20Users-Groups.zip) |

**Install the package before testing assignee reassignment:**

1. Open Package Manager: http://localhost:4502/crx/packmgr
2. Click **Upload Package** and select `../resources/Assignees Users-Groups.zip`
3. Click **Install**
4. Confirm the `devs` group exists under **Tools → Security → Groups** with the expected users as direct members

After installation, open a ticket detail page, click **Assignee**, and verify the dropdown lists users from the `devs` group (served by `AssigneeUsersServlet`).

## Modules

| Module | Purpose |
|--------|---------|
| `core` | OSGi bundle — services, Sling servlets, Sling Models, filters, schedulers |
| `ui.apps` | Application code — components, templates, client libraries, dialogs |
| `ui.apps.structure` | Repository structure definitions for FileVault |
| `ui.config` | Runmode-specific OSGi configurations |
| `ui.content` | Sample and initial content (pages, templates, assets) |
| `ui.frontend` | Webpack frontend build (TypeScript, Sass) compiled into clientlibs |
| `dispatcher` | Cloud-optimized Dispatcher configuration |
| `it.tests` | Integration tests (AEM Testing Clients) for Cloud Manager functional testing |
| `ui.tests` | Cypress UI tests for Cloud Manager custom UI testing |
| `all` | Aggregator content package embedding all modules for deployment |

## Build Commands

```bash
# Full build
mvn clean install

# Build and deploy all packages to local author
mvn clean install -PautoInstallSinglePackage

# Deploy to publish instance
mvn clean install -PautoInstallSinglePackagePublish

# Deploy only the OSGi bundle
mvn clean install -PautoInstallBundle

# Deploy a single content package (from module directory)
mvn clean install -PautoInstallPackage

# Frontend only
cd ui.frontend && npm ci && npm run prod
```

## Architecture

### Data Storage

Ticket data is stored under `/var/ai-practical-assessment/tickets/{ticket-id}`. Comments are child nodes at `.../comments/{comment-id}`. UI pages live under `/content/ai-practical-assessment/support-tickets/`.

### Backend (core)

| Servlet | Purpose |
|---------|---------|
| `CreateTicketServlet` | POST — create a new ticket |
| `TicketListServlet` | GET — list all tickets |
| `GetTicketServlet` | GET — single ticket with comments |
| `UpdateTicketServlet` | POST — update ticket fields |
| `UpdateTicketStatusServlet` | POST — status transitions with workflow validation |
| `AddCommentServlet` | POST — add a comment |
| `AssigneeUsersServlet` | GET — assignable users from the `devs` group (install [`../resources/Assignees Users-Groups.zip`](../resources/Assignees%20Users-Groups.zip) first) |
| `SearchTicketsServlet` | GET `/bin/ai-practical-assessment/search` — keyword and status search |

API contracts: [`specs/001-support-tickets/contracts/support-ticket-api.md`](specs/001-support-tickets/contracts/support-ticket-api.md)

### UI Components (ui.apps)

| Component | Purpose |
|-----------|---------|
| `ticket-dashboard` | Ticket list, keyword search, status filter |
| `ticket-create` | New ticket form |
| `ticket-detail` | Ticket view — inline edit, status menu, assignee picker |
| `ticket-comments` | Comment thread |
| `header` | Site branding, login/logout, welcome message |

Ticket UI uses Material Design via the `clientlib-ticket-material` client library.

### Status Workflow

```
open → in-progress → resolved → closed
  ↓         ↓
cancelled  cancelled
```

Terminal states (`closed`, `cancelled`) are read-only.

## Testing

### Unit Tests

```bash
mvn clean test
```

JUnit tests for services, models, and validators live in `core/src/test/java`.

### Integration Tests

```bash
mvn clean verify -Plocal
```

Test classes are in `it.tests/src/main/java` and match `*IT.java`. Override AEM endpoints with Maven system properties:

| Property | Default |
|----------|---------|
| `it.author.url` | `http://localhost:4502` |
| `it.author.user` | `admin` |
| `it.author.password` | `admin` |
| `it.publish.url` | `http://localhost:4503` |
| `it.publish.user` | `admin` |
| `it.publish.password` | `admin` |

## Static Analysis

The AEM Analyser plugin runs automatically during `mvn clean install` to validate Cloud Service deployment compatibility. See the [aemanalyser-maven-plugin](https://github.com/adobe/aemanalyser-maven-plugin) documentation for configuration options.

## Project Documentation

| Document | Description |
|----------|-------------|
| [`AGENTS.md`](AGENTS.md) | AI agent and developer reference for the codebase |
| [`specs/001-support-tickets/`](specs/001-support-tickets/) | Support ticket feature spec, plan, and API contracts |
| [`specs/002-keyword-search/`](specs/002-keyword-search/) | Dashboard keyword search specification |
| [`specs/003-ist-date-format/`](specs/003-ist-date-format/) | IST timestamp formatting specification |
| [`specs/004-header-branding/`](specs/004-header-branding/) | Header component specification |
| [`specs/005-component-auth/`](specs/005-component-auth/) | Component-level authentication specification |
| [`specs/006-header-login-redirect/`](specs/006-header-login-redirect/) | Post-login redirect specification |

## Client Libraries

The `ui.frontend` module compiles TypeScript and Sass via Webpack. Output is transformed into AEM ClientLibs by [`aem-clientlib-generator`](https://github.com/wcm-io-frontend/aem-clientlib-generator) during the Maven build. Ticket-specific clientlibs (`clientlib-ticket-dashboard`, `clientlib-ticket-detail`, `clientlib-ticket-material`, etc.) live in `ui.apps`.

For frontend development with live reload:

```bash
cd ui.frontend && npm start
```

See [`ui.frontend/README.md`](ui.frontend/README.md) for the full frontend workflow.

## License

Copyright 2015 Adobe Systems Incorporated. Licensed under the Apache License, Version 2.0.
