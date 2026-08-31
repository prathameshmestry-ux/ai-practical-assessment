# Test Strategy

## Test Scope

**In scope**: ticket create/list/get/update, comments, status workflow, keyword+status search, assignee list, auth gate, JCR persistence under /var/ai-practical-assessment/tickets.

**Layers**: core business logic + servlets, Sling Models, clientlib JS behavior, AEM Author integration.

**Out of scope v1**: Publish replication, email notifications, ticket delete, bulk ops, role-based permissions, concurrent-edit conflict UI, performance/load at scale.

**Manual validation**: specs/001-support-tickets/quickstart.md + specs/002-keyword-search/quickstart.md.

## Unit Tests


| Area      | Class                                                                   | What it covers                                                                                                                                              |
| --------- | ----------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Service   | `TicketServiceImplTest`                                                 | Create ticket → `open` status; search requires keyword or status; QueryBuilder predicate building (title prefix, ticket-id prefix, status filter, combined) |
| Validator | `TicketStatusTransitionValidatorTest`                                   | Allowed transitions; invalid jumps rejected; terminal states (`closed`, `cancelled`) have no targets                                                        |
| Auth      | `ServletAuthTest`                                                       | Anonymous = not authenticated; `401` returns `{}`; profile full-name resolution                                                                             |
| JSON      | `TicketApiJsonTest`                                                     | Unauthorized response shape                                                                                                                                 |
| Models    | `TicketCreateModelTest`, `TicketListModelTest`, `TicketDetailModelTest` | Anonymous user → `loggedIn=false`; service not called when unauthenticated                                                                                  |
| Util      | `TicketDateFormatterTest`                                               | Date formatting for API output                                                                                                                              |




## Component Tests

- No isolated HTL or clientlib JS unit tests.
- Sling Models tested via AEM Mocks — proxy for component server-side logic (auth gate, API path exposure).
- HTL rendering, Material Design styling, empty-state UI — manual only.



## API / Integration Tests


| Test               | File                                              | Coverage                                                                    |
| ------------------ | ------------------------------------------------- | --------------------------------------------------------------------------- |
| Create + list      | `SupportTicketServletIT.testCreateAndListTickets` | `POST .../tickets.ticket.json` → `201`; `GET .../tickets.list.json` → `200` |
| Search by title    | `testSearchTicketsByTitlePrefix`                  | Create unique title → `GET /bin/.../search.json?keyword=Permis` finds it    |
| Search validation  | `testSearchRejectsBlankKeyword`                   | Blank keyword alone → `400`                                                 |
| Status-only search | `testSearchByStatusOnly`                          | `?status=open` → `200`                                                      |




## Edge Case Tests


| Edge case                                       | How tested                                                       |
| ----------------------------------------------- | ---------------------------------------------------------------- |
| Invalid status transition (`open` → `resolved`) | Unit — `TicketStatusTransitionValidatorTest`                     |
| Terminal status block                           | Unit — validator `isTerminal` + manual VS-5                      |
| Search with no keyword and no status            | Unit — `TicketServiceImplTest`                                   |
| Blank keyword only on search API                | IT — `testSearchRejectsBlankKeyword`                             |
| Anonymous access                                | Unit — model + `ServletAuthTest`; manual — login prompt on pages |
| Empty comment / empty title                     | Manual — VS-1, VS-3, VS-4                                        |
| Ticket not found                                | Manual quickstart; no automated test                             |
| Invalid assignee                                | Not tested                                                       |
| Pagination (`offset`/`limit`)                   | Not tested                                                       |
| CSRF on Author POST                             | Not tested                                                       |
| Comments on closed ticket                       | Manual assumption; no test                                       |
| Concurrent two-user edit                        | Not tested — last-write-wins undocumented                        |
| Large list (100+ tickets)                       | Not tested — SC-002 manual/perf only                             |




## Tests Not Covered (and why)

- **Cypress E2E for ticket journeys** — `ui.tests` has generic `basic.cy.js`, `login.cy.js` only. No ticket create→dashboard→detail→status flow.   
**Reason**: v1 prioritized backend + IT; UI tests need stable test data + login fixture. 
- **Per-servlet unit tests** — servlets thin; logic in `TicketServiceImpl`.   
**Reason**: service-layer tests deemed sufficient for v1.
- **Full lifecycle IT** — no single IT for create → update → comment → status → close.   
**Reason**: partial IT added for search feature; full flow deferred to reduce AEM IT runtime.
- **Assignee API +** `devs` **group membership** — needs real AEM users/groups.   
**Reason**: env-dependent; manual VS-3 covers happy path.
- **CSRF token enforcement** — POST without token on Author.   
**Reason**: AEM platform concern; not servlet-specific test added.
- **Role-based access** — all authenticated users = agents.   
**Reason**: roles not in spec v1.
- **Performance (SC-002: 100 tickets in 3s)** — no automated perf test.   
**Reason**: needs load fixture + benchmark harness.

