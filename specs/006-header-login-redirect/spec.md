# Feature Specification: Header Login Post-Redirect

**Feature Branch**: `006-header-login-redirect`

**Created**: 2026-08-28

**Status**: Draft

**Input**: Update Header Login link to pass current page as `resource` query param so users return to same page after login.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Return to Same Page After Login (Priority: P1)

As anonymous visitor on ticket dashboard (or any support page), I want Login to send me back to that page after auth, not a generic landing page.

**Independent Test**: Open dashboard logged out → click Login → authenticate → land on dashboard again.

**Acceptance Scenarios**:

1. **Given** anonymous user on `/content/.../dashboard.html`, **When** they click **Login**, **Then** login URL includes `resource` pointing to that dashboard `.html` path.
2. **Given** successful login via that link, **When** AEM completes auth, **Then** user lands on the page they started from.
3. **Given** anonymous user on ticket detail or create page, **When** they click **Login**, **Then** `resource` matches that page’s `.html` path.

---

### User Story 2 - Safe Fallback When Page Unknown (Priority: P2)

As visitor, if current page path cannot be resolved, Login still works without broken link.

**Acceptance Scenarios**:

1. **Given** page path cannot be determined, **When** Login renders, **Then** href is standard login page without `resource` param (no error).

---

### Edge Cases

- Page URL already ends with `.html` → do not double suffix.
- Experience Fragment header on content page → `resource` must be **viewing page**, not XF path.
- Query strings on current URL → `resource` uses page path only, not query.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Header Login link MUST append current page path as `resource` query parameter on AEM login URL.
- **FR-002**: `resource` value MUST be the current page’s content path with `.html` suffix (e.g. `/content/.../dashboard.html`).
- **FR-003**: Header model MUST expose current page path to view layer for login URL construction.
- **FR-004**: When current page path unavailable, Login MUST fall back to login URL without `resource`.
- **FR-005**: Logout and authenticated profile block MUST remain unchanged.

### Non-Functional Requirements *(constitution-aligned)*

- **NFR-001**: AEM as a Cloud Service compatible — standard Granite login + `resource` redirect pattern.
- **NFR-002**: No secrets or user PII in login URL beyond public page path.

### Key Entities

- **Current page path**: Public `.html` path of page user is viewing.
- **Login URL**: Granite login endpoint plus optional `resource` query.

## Success Criteria *(mandatory)*

- **SC-001**: 100% of Login clicks from support ticket pages include correct `resource` in acceptance testing.
- **SC-002**: Post-login return to originating page succeeds on dashboard, create, and detail pages.
- **SC-003**: Fallback login link works when page path cannot be resolved.

## Assumptions

- AEM standard login at `/libs/granite/core/content/login.html` with `resource` redirect is enabled (platform default).
- Header renders in page context where current page is discoverable from request or HTL bindings.

## Dependencies

- Header component and `HeaderModel` from `004-header-branding`.
- Public support ticket pages under `/content/ai-practical-assessment/support-tickets/`.
