# Feature Specification: Component-Level Ticket Authentication

**Feature Branch**: `005-component-auth`

**Created**: 2026-08-27

**Status**: Draft

**Input**: Update security architecture to use component-level authentication checks rather than OSGi-level global redirects. AEM pages remain accessible to anonymous users so the Header can show Login. Ticket Dashboard, Ticket Detail, and Create Ticket gate UI in components; ticket data APIs reject anonymous callers.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Anonymous Visitor Sees Public Shell (Priority: P1)

As an anonymous visitor, I want to open support ticket pages and see the site header with a Login option, without being redirected away from the page, so I can sign in when ready.

**Why this priority**: Pages must stay public; global page-level auth would hide the Login entry point in the Header.

**Independent Test**: Open dashboard, create ticket, and ticket detail URLs without logging in; page loads with header Login link; ticket components show a login prompt instead of ticket UI.

**Acceptance Scenarios**:

1. **Given** an anonymous visitor, **When** they open the ticket dashboard page, **Then** the page renders (including Header with Login) and the dashboard component shows **Please log in to view and manage tickets** instead of the ticket list.
2. **Given** an anonymous visitor, **When** they open the create ticket page, **Then** the page renders and the create component shows the same login message instead of the create form.
3. **Given** an anonymous visitor, **When** they open a ticket detail page, **Then** the page renders and the detail component shows the same login message instead of ticket fields.
4. **Given** an anonymous visitor, **When** they call a ticket data endpoint under the ticket repository, **Then** the response is **401 Unauthorized** with an empty JSON body and no ticket payload.

---

### User Story 2 - Authenticated User Uses Ticket Features (Priority: P1)

As a logged-in support user, I want the dashboard, create, and detail components to show full ticket UI when my session is valid, so I can manage tickets normally.

**Why this priority**: Core product value depends on authenticated access unchanged from today.

**Independent Test**: Log in, open dashboard, create, and detail; full UI appears; API calls return data.

**Acceptance Scenarios**:

1. **Given** an authenticated user, **When** they open the dashboard, **Then** the full ticket list, search, and actions render.
2. **Given** an authenticated user, **When** they open the create ticket page, **Then** the full create form renders and can submit a ticket.
3. **Given** an authenticated user, **When** they open a valid ticket detail page, **Then** ticket metadata, edit form, and status controls render.
4. **Given** an authenticated user, **When** they call ticket data endpoints, **Then** responses succeed as before (not 401).

---

### User Story 3 - Defense in Depth for Direct API Access (Priority: P2)

As a security stakeholder, I want ticket APIs to reject anonymous sessions even if someone bypasses the UI, so ticket data cannot leak through direct requests.

**Why this priority**: UI gating alone is insufficient; APIs must enforce the same session rule.

**Independent Test**: Issue GET/POST to ticket JSON endpoints without session; receive 401 and `{}`.

**Acceptance Scenarios**:

1. **Given** an anonymous session, **When** any GET or POST ticket servlet under the ticket repository is invoked, **Then** HTTP status is 401 and body is empty JSON (`{}`).
2. **Given** an authenticated session, **When** the same endpoints are invoked with valid input, **Then** behavior matches existing ticket API contracts (excluding 401 envelope change for anonymous).

---

### Edge Cases

- Anonymous user opens create ticket page → login message only; no create form fields in HTML.
- Anonymous user bookmarks a ticket detail URL with `ticketId` → page loads; detail component shows login message; no ticket fields in HTML.
- Session expires while dashboard is open → subsequent API calls return 401; client-side refresh shows login message on next full page load.
- Authenticated user with missing ticket id on detail page → existing “not found” behavior applies only when logged in.
- Search endpoint used by dashboard client → same anonymous 401 rule applies so keyword search cannot leak data.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Support ticket **pages** MUST remain accessible to anonymous users (no page-level or OSGi global redirect to login).
- **FR-002**: Ticket Dashboard component model MUST expose `isLoggedIn` (true when current user is not anonymous).
- **FR-003**: Ticket Detail component model MUST expose `isLoggedIn` using the same rule as FR-002.
- **FR-004**: Create Ticket component model MUST expose `isLoggedIn` using the same rule as FR-002.
- **FR-005**: When `isLoggedIn` is false, Dashboard, Create, and Detail HTL MUST render only a fallback block with the message **Please log in to view and manage tickets** (no ticket list, create form, edit forms, or ticket field values).
- **FR-006**: When `isLoggedIn` is true, Dashboard, Create, and Detail HTL MUST render the full ticket interface as today.
- **FR-006a**: Ticket Dashboard and Detail models MUST NOT load ticket list or ticket detail data when the user is anonymous.
- **FR-007**: All GET and POST ticket data servlets bound to the ticket repository (`/var/ai-practical-assessment/tickets`) MUST reject anonymous users immediately with HTTP **401** and response body **`{}`**.
- **FR-008**: The dashboard search servlet used for ticket keyword search MUST apply the same anonymous rejection rule as FR-007.
- **FR-009**: Header component MUST continue to show Login for anonymous users and Welcome/Logout for authenticated users (unchanged from header feature).

### Non-Functional Requirements *(constitution-aligned)*

- **NFR-001**: Solution MUST remain deployable on AEM as a Cloud Service with no incompatible auth patterns.
- **NFR-002**: Ticket content and PII MUST NOT appear in server-rendered HTML for anonymous sessions.
- **NFR-003**: Unit tests MUST cover model login gating and servlet 401 empty-body behavior.

### Key Entities

- **Session state**: Whether the current request user is anonymous or authenticated.
- **Component view state**: `isLoggedIn` driving conditional HTL for dashboard, create, and detail.
- **Ticket API access**: Servlet-level authorization aligned with component session rule.

## Success Criteria *(mandatory)*

- **SC-001**: 100% of anonymous dashboard, create, and detail page visits show the login prompt and zero ticket field values in rendered HTML.
- **SC-002**: 100% of anonymous ticket API GET/POST attempts return 401 with empty JSON body in acceptance testing.
- **SC-003**: Authenticated users complete dashboard browse, ticket create, and ticket detail edit flows without regression in manual acceptance.
- **SC-004**: Anonymous users can reach any support ticket page and see Header Login within one navigation step (no global redirect).

## Assumptions

- Login state uses the same rule as existing ticket API auth: user ID not equal to `anonymous`.
- Header component is present on support ticket pages via experience fragment or template.
- Create ticket page uses the same component-level gate as dashboard and detail.
- Empty JSON body means the literal `{}` with `Content-Type: application/json`.

## Dependencies

- Existing Header auth (`004-header-branding`).
- Existing ticket servlets and Sling Models from `001-support-tickets` and related features.
