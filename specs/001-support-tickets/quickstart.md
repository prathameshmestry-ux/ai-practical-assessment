# Quickstart: Support Ticket Management System

**Feature**: 001-support-tickets | **Date**: 2026-08-12

Validation guide for end-to-end feature verification on local AEM SDK.
See [data-model.md](./data-model.md) and [contracts/support-ticket-api.md](./contracts/support-ticket-api.md) for details.

## Prerequisites

- AEM as a Cloud Service SDK running locally (default `http://localhost:4502`)
- Maven 3.x, **JDK 21** (mandatory per constitution and `.cloudmanager/java-version`)
- Project built and deployed:

```bash
mvn clean install -PautoInstallSinglePackage
```

## Pages

| Page | URL |
|------|-----|
| Create | http://localhost:4502/content/ai-practical-assessment/support-tickets/create-ticket.html |
| Dashboard | http://localhost:4502/content/ai-practical-assessment/support-tickets/dashboard.html |
| Detail | http://localhost:4502/content/ai-practical-assessment/support-tickets/ticket.html?ticketId={ticket-id} |

Log in as `admin` / `admin` (or any test user) before testing.

## Validation Scenarios

### VS-1: Create Ticket (US1 / SC-001)

1. Open **Create** page.
2. Enter title, description, select priority; submit.
3. **Expected**: Success message with `ticketId`; new node at
   `/content/ai-practical-assessment/support-tickets/{ticket-id}` in CRXDE Lite
   with `status=open`, `requester` = current user.
4. Submit with empty title → **Expected**: validation error, no node created.

### VS-2: Dashboard List (US2 / SC-002)

1. Create 2–3 tickets.
2. Open **Dashboard** page.
3. **Expected**: All tickets listed with title, status, priority, assignee, last modified.
4. With zero tickets → **Expected**: empty-state message (not an error).
5. Click a ticket row → **Expected**: navigates to detail page with correct `ticketId`.

### VS-3: Update & Reassign (US3)

1. Open a ticket detail page.
2. Change title, description, priority; save.
3. **Expected**: Values persist on reload; `lastModified` updated.
4. Set assignee from dropdown; save.
5. **Expected**: Assignee visible on dashboard and detail.
6. Clear title and save → **Expected**: validation error.

### VS-4: Add Comment (US4 / SC-006)

1. On detail page, enter comment text; submit.
2. **Expected**: Comment appears oldest-first with author and timestamp.
3. Verify node at `.../comments/{comment-id}` in CRXDE Lite.
4. Submit empty comment → **Expected**: validation error.

### VS-5: Status Workflow (US3 / SC-003, SC-004)

For a fresh ticket, execute transitions and verify after each:

| Step | Action | Expected status |
|------|--------|-----------------|
| 1 | Create | `open` |
| 2 | → In Progress | `in-progress` |
| 3 | → Resolved | `resolved` |
| 4 | → Closed | `closed` |

Negative tests:

| Current | Attempt | Expected |
|---------|---------|----------|
| `open` | → `resolved` | Blocked, message shown |
| `closed` | → `open` | Blocked, terminal state message |

Repeat cancel path: `open` → `cancelled` and `in-progress` → `cancelled`.

### VS-6: API Contract Smoke (curl)

Obtain CSRF token from AEM, then:

```bash
# Create (replace TOKEN)
curl -u admin:admin -X POST \
  "http://localhost:4502/content/ai-practical-assessment/support-tickets.ticket.json" \
  -H "Content-Type: application/json" \
  -H "CSRF-Token: TOKEN" \
  -d '{"title":"API test","description":"Via servlet","priority":"low"}'
```

**Expected**: `201` with `ticketId` in JSON body.

## Automated Tests

```bash
# Unit tests
mvn test -pl core

# Integration tests (requires running AEM)
mvn verify -pl it.tests

# Full build
mvn clean install
```

## Troubleshooting

| Symptom | Check |
|---------|-------|
| 401 on POST | User not logged in; missing CSRF token on Author |
| 403 / save fails | Service user mapping in `ui.config`; `rep:policy` on `/content/ai-practical-assessment` in `ui.content` |
| Dashboard empty but nodes exist | Nodes missing `ticketId` property; wrong path |
| Components missing in editor | Redeploy `ui.apps`; confirm `componentGroup` is **AI Capability Project - Content** and `_cq_dialog` exists on ticket components |

## Definition of Done (feature)

- [ ] All VS-1 through VS-6 pass on local SDK
- [ ] `mvn clean install` succeeds (unit tests green)
- [ ] No constitution violations (bounded queries, no secrets, correct modules)
- [ ] Components render on all three support pages
