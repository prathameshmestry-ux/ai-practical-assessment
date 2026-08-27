# Quickstart: Ticket Dashboard Keyword Search

**Feature**: 002-keyword-search | **Date**: 2026-08-27

End-to-end validation on local AEM SDK. See [data-model.md](./data-model.md) and [contracts/support-ticket-search-api.md](./contracts/support-ticket-search-api.md).

## Prerequisites

- AEM as a Cloud Service SDK (`http://localhost:4502`)
- Maven 3.x, **JDK 21**
- Project deployed:

```bash
mvn clean install -PautoInstallSinglePackage
```

- Create tickets with titles such as `Permission reset needed` and `Access permission issue` (for prefix vs non-match tests)
- Log in before testing (e.g. `admin` / `admin`)

## Pages & Endpoints

| Resource | URL |
|----------|-----|
| Dashboard | http://localhost:4502/content/ai-practical-assessment/support-tickets/dashboard.html |
| Search API | http://localhost:4502/bin/ai-practical-assessment/search.json?keyword={keyword}&status={status} |
| List API (reset) | http://localhost:4502/var/ai-practical-assessment/tickets.list.json |

## Validation Scenarios

### VS-1: Search by Title Prefix (US1 / SC-001)

1. Open **Dashboard**.
2. Create or locate a ticket titled `Permission reset needed`.
3. Enter `permis` (does **not** start with `ticket-`).
4. Press **Enter** or click **Search**.
5. **Expected**: That ticket appears (title **starts with** `permis`).
6. Search `permis` again with only `Access permission issue` tickets in repo → **Expected**: no match (title does not **start** with `permis`).
7. **Expected**: No page reload; ticket ID links work.

### VS-2: Search by Ticket ID Prefix (US1 / FR-003)

1. Copy a ticket ID (e.g. `ticket-8fde-...`).
2. Enter partial ID `ticket-8fde` in search.
3. Submit search.
4. **Expected**: Matching ticket(s) whose ID **starts with** `ticket-8fde`—not title-only matches.
5. Enter `ticket-nonexistent` → **Expected**: empty-state “no matches” message.

### VS-3: Async UX (US2 / SC-002)

1. Submit any search.
2. **Expected**: Browser URL unchanged; no full document reload.
3. **Expected**: Only `#ticket-dashboard-body` rows change (or empty state toggles).

### VS-4: Empty Search Reset (US3 / SC-004)

1. Set status dropdown to **All**.
2. Run a search that filters the table.
3. Clear the search input completely.
4. Press **Enter** or click **Search**.
5. **Expected**: Full ticket list restored; no “keyword required” error.
6. With search empty and status **All**, submit again → **Expected**: same full list, no error.

### VS-5: Filter by Status (US4 / SC-005)

1. Ensure tickets exist in multiple statuses (e.g. Open and Resolved).
2. Set status dropdown to **Open** (leave keyword empty).
3. **Expected**: Only open tickets shown; no page reload.
4. Enter a title prefix in keyword + keep **Open** selected → **Expected**: Open tickets whose title starts with keyword.
5. Change status to **All** with keyword empty → **Expected**: full list restored.

### VS-6: No Prefix Matches

1. Search for a gibberish prefix with no title starting with that text (non-`ticket-` keyword).
2. **Expected**: Empty state indicating no matches; table hidden.

### VS-7: Auth & API Contract

1. Log out or use incognito without session.
2. `GET /bin/ai-practical-assessment/search.json?keyword=test` → **Expected**: 401 JSON error.
3. `GET .../search.json` without `keyword` or `status` → **Expected**: 400 validation error (direct API only).
4. `GET .../search.json?status=open` → **Expected**: 200 with open tickets only.

## Unit Tests

```bash
mvn clean test -pl core
```

**Expected**: `TicketServiceImplTest` passes (keyword/status validation and predicate tests).

## Success Criteria Checklist

| ID | Verify via |
|----|------------|
| SC-001 | VS-1 + VS-2 — prefix match in < 10s |
| SC-002 | VS-3 — no reload |
| SC-003 | Results only from ticket data root (CRXDE: nodes under `/var/.../tickets`) |
| SC-004 | VS-4 — reset without error |
| SC-005 | VS-5 — status filter alone or with keyword |
