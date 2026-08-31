## Selected Project Option

Support Ticket Management System on AEM as a Cloud Service — JCR-backed tickets, component-based UI, Sling servlet API.

## My Understanding

Users log in, create tickets (title, description, priority). Dashboard lists all tickets from JCR. Detail view shows full ticket + comments. Agents update fields, reassign, add comments, change status via strict workflow. Data lives under /content/ai-practical-assessment/support-tickets/{ticket-id}; comments as child nodes. AEM components: create, dashboard, detail, comments.

## Functional Requirements


| ID         | Requirement                                                                                                                    |
| ---------- | ------------------------------------------------------------------------------------------------------------------------------ |
| FR-001     | Create ticket — title, description, priority (required fields validated)                                                       |
| FR-002     | Auto-set status Open, unique ID, requester, timestamps on create                                                               |
| FR-003     | Validate create/update — no partial bad saves                                                                                  |
| FR-004     | Dashboard — list all tickets (ID, title, status, priority, assignee, last updated)                                             |
| FR-005     | Detail view — full ticket data + navigation                                                                                    |
| FR-006     | Update title, description, priority                                                                                            |
| FR-007     | Reassign to valid assignee from config list                                                                                    |
| FR-008     | Add comments with author + timestamp                                                                                           |
| FR-009     | Comments in consistent chronological order                                                                                     |
| FR-010     | Status transitions only: Open→In Progress/Cancelled; In Progress→Resolved/Cancelled; Resolved→Closed                           |
| FR-011     | Reject invalid transitions — status unchanged                                                                                  |
| FR-012     | Closed + Cancelled = terminal — no further status change                                                                       |
| FR-013     | Persist tickets + comments across sessions                                                                                     |
| FR-014     | Dashboard ↔ detail navigation                                                                                                  |
| FR-015–017 | JCR paths + parent-child comment linkage API: 6 endpoints — create, list (paginated), get, update, add comment, update status. |


## Non-Functional Requirements


| ID      | Requirement                                                |
| ------- | ---------------------------------------------------------- |
| NFR-001 | AEM Cloud Service compatible — Java 21, no deprecated APIs |
| NFR-002 | No secrets in code; no PII in logs                         |
| NFR-003 | Unit + integration test coverage for behavior changes      |
| NFR-004 | Modular AEM components (create, list, detail, comments)    |
| NFR-005 | Dashboard ≤3s for ≤100 tickets                             |
| NFR-006 | Material Design UI via shared clientlib                    |
| NFR-007 | CSRF token on Author POST mutations                        |


## Assumptions

- Users authenticated before ticket access
- Single support queue
- Assignee from AEM User list
- Priority: Low / Medium / High (default Medium)
- Pages at /content/ai-practical-assessment/support-tickets/{dashboard|create-ticket|ticket}

## Edge Cases


| Scenario                                 | Expected behavior                              |
| ---------------------------------------- | ---------------------------------------------- |
| Ticket not found                         | 404 + link back to dashboard                   |
| Invalid status jump (e.g. Open→Resolved) | 400, status unchanged, clear error             |
| Empty required fields on update          | Validation error, no save                      |
| Large ticket count                       | Pagination (default 25, max 100)               |
| Concurrent updates                       | Last save wins (or notify conflict — pick one) |
| Empty comment                            | Rejected                                       |
| Closed/Cancelled status change           | Blocked — terminal state                       |
| Ticket node deleted                      | Comments cascade-delete with parent            |
| Invalid assignee                         | Rejected — not in config list                  |
| Anonymous API call                       | 401 Unauthorized                               |


