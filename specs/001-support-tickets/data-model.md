# Data Model: Support Ticket Management System

**Feature**: 001-support-tickets | **Date**: 2026-08-12

## Repository Layout

```text
/content/ai-practical-assessment/support-tickets/
└── {ticket-id}/                          # nt:unstructured (ticket node)
    ├── ticketId        (String, required)
    ├── title           (String, required, max 200)
    ├── description     (String, required, max 5000)
    ├── priority        (String, required: low|medium|high)
    ├── status          (String, required: open|in-progress|resolved|closed|cancelled)
    ├── requester       (String, required — AEM user ID)
    ├── assignee        (String, optional — AEM user ID)
    ├── created           (Date, required — ISO-8601 stored)
    ├── lastModified    (Date, required — ISO-8601 stored)
    └── comments/                         # nt:unstructured (folder)
        └── {comment-id}/                 # nt:unstructured (comment node)
            ├── commentId   (String, required)
            ├── text        (String, required, max 4000)
            ├── author      (String, required — AEM user ID)
            └── created     (Date, required)
```

**Linkage**: Comment `{comment-id}` nodes MUST be children of `{ticket-id}/comments/`.
No `ticketRef` property on comments (FR-017).

## Page Layout

```text
/content/ai-practical-assessment/              # cq:Page (site root)
└── support-tickets/                           # cq:Page (redirects to dashboard)
    ├── dashboard/                             # cq:Page — ticket-dashboard
    ├── create-ticket/                         # cq:Page — ticket-create
    ├── ticket/                                # cq:Page — ticket-detail + ticket-comments
    └── {ticket-id}/                           # nt:unstructured (runtime ticket data)
```

Locale folders (`us/en`) removed. Site content and XF live directly under `/content/ai-practical-assessment`.

## Entities

### Ticket

| Field | Type | Required | Default | Validation |
|-------|------|----------|---------|------------|
| `ticketId` | String | Yes | generated | Matches node name; pattern `ticket-[a-f0-9-]+` |
| `title` | String | Yes | — | Non-blank; ≤200 chars |
| `description` | String | Yes | — | Non-blank; ≤5000 chars |
| `priority` | Enum | Yes | `medium` | One of: `low`, `medium`, `high` |
| `status` | Enum | Yes | `open` | See state machine below; on detail UI: unified Jira-style status badge (FR-010a/b) |
| `requester` | String | Yes | session user | Valid AEM user ID |
| `assignee` | String | No | null | Direct `devs` group member; individual user; path not under `/home/users/system` |
| `created` | DateTime | Yes | now | Set on create only |
| `lastModified` | DateTime | Yes | now | Updated on every mutation |

**JCR path**: `/content/ai-practical-assessment/support-tickets/{ticket-id}`

### Comment

| Field | Type | Required | Default | Validation |
|-------|------|----------|---------|------------|
| `commentId` | String | Yes | generated | Matches node name; pattern `comment-[a-f0-9-]+` |
| `text` | String | Yes | — | Non-blank; ≤4000 chars |
| `author` | String | Yes | session user | Valid AEM user ID |
| `created` | DateTime | Yes | now | Immutable after create |

**JCR path**: `/content/ai-practical-assessment/support-tickets/{ticket-id}/comments/{comment-id}`

### User (reference)

Not persisted as ticket nodes. Referenced by `requester`, `assignee`, and `author`
as AEM user IDs. Display names resolved at render time via `UserManager` (Author) or
stored ID fallback (Publish).

## Status State Machine

```text
                    ┌─────────────┐
                    │    open     │
                    └──────┬──────┘
              ┌────────────┼────────────┐
              ▼            │            ▼
     ┌────────────────┐    │    ┌───────────────┐
     │  in-progress   │    │    │  cancelled    │ (terminal)
     └───────┬────────┘    │    └───────────────┘
       ┌─────┴─────┐       │
       ▼           ▼       │
┌───────────┐ ┌──────────┐ │
│ resolved  │ │cancelled │─┘
└─────┬─────┘ └──────────┘
      ▼
┌──────────┐
│  closed  │ (terminal)
└──────────┘
```

| From | Allowed To |
|------|------------|
| `open` | `in-progress`, `cancelled` |
| `in-progress` | `resolved`, `cancelled` |
| `resolved` | `closed` |
| `closed` | *(none — terminal)* |
| `cancelled` | *(none — terminal)* |

Invalid transitions throw `InvalidStatusTransitionException`; servlet returns HTTP 400
with message (FR-011).

## Relationships

```text
Ticket 1 ──< * Comment
Ticket * ──> 0..1 assignee (User)
Ticket 1 ──> 1 requester (User)
Comment 1 ──> 1 author (User)
```

## Query Model (Dashboard)

| Predicate | Value |
|-----------|-------|
| `path` | `/content/ai-practical-assessment/support-tickets` |
| `type` | `nt:unstructured` |
| `property` | `ticketId` |
| `property.operation` | `exists` |
| `orderby` | `@lastModified` |
| `orderby.sort` | `desc` |
| `p.limit` | `25` (default; max 100) |
| `p.offset` | pagination offset |

Exclude `comments/` folder nodes by requiring `ticketId` property (comments use
`commentId` instead).

## Cascade Rules

- Deleting a ticket node removes all descendant comment nodes (JCR subtree delete).
- Updating ticket fields does not modify comments.
- Comments may be added when status is `closed` or `cancelled` (spec edge case).

## Presentation (ticket-detail)

- **Status (FR-010a/b/c)**: Unified status badge + async DOM update (see above).
- **Assignee (FR-007a/b/c)**: Inline meta edit—read-only display → input + local-filtered suggestions; bulk GET of `devs` direct members cached in `sessionStorage`; save via `update.json` without page reload; server validates group membership + non-system path.
- **Other fields**: Title, description, priority in edit form (assignee not in form).

## OSGi Configuration

| PID | Purpose |
|-----|---------|
| `com.ttn.ai.core.services.impl.TicketServiceImpl` | `ticketRootPath`, `defaultPageSize`, `maxPageSize` |

**Service user ACL** (`repoinit`): `ticket-service` read on `/home/users` and `/home/groups` for assignee resolution.

## Service User

| Mapping | Subservice | ACL |
|---------|------------|-----|
| `ai-practical-assessment:ticket-service` | `ticket-service` | read, write, create, delete on `/content/ai-practical-assessment` hierarchy via `ui.content` `rep:policy` |

Defined in `ui.config` per AEM service user best practices (constitution V).
