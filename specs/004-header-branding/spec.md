# Feature Specification: Header Logo and Website Name

**Feature Branch**: `004-header-branding`

**Created**: 2026-08-27

**Status**: Draft

**Input**: Authorable Header component with logo, website name, and dynamic user profile / authentication block.

## Clarifications

### Session 2026-08-27 (b)

- Q: How is login state determined? → A: User is logged in when resource resolver user ID is not `anonymous` (same rule as ticket API auth).
- Q: How is display name resolved? → A: Concatenate `profile/givenName` and `profile/familyName` from the logged-in user via UserManager; fall back to user ID if profile names absent.
- Q: Where do Login/Logout links go? → A: Login → AEM login page; Logout → `/system/sling/logout.html`.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Author Configures Header Branding (Priority: P1)

As a content author, I want to set a logo and website name on the Header component so visitors see consistent site branding.

**Independent Test**: Edit Header dialog, set logo path and name, publish—header shows both.

**Acceptance Scenarios**:

1. **Given** the Header component on a page, **When** the author sets **Logo Image Path** (under `/content/dam`) and **Website Name**, **Then** both values persist and display on publish.
2. **Given** only logo is set, **When** the page renders, **Then** only the logo appears (no empty name element).
3. **Given** only website name is set, **When** the page renders, **Then** only the name appears (no broken image).
4. **Given** neither value is set, **When** the page renders, **Then** the branding block does not output empty markup (profile section still renders).

---

### User Story 2 - User Profile and Authentication (Priority: P1)

As a site visitor, I want to see my name when logged in and clear Login/Logout actions so I know my session state.

**Independent Test**: Visit header logged out → Login link; log in as named user → “Welcome, {full name}” + Logout.

**Acceptance Scenarios**:

1. **Given** an anonymous visitor, **When** the header renders, **Then** a **Login** link is shown and no welcome message.
2. **Given** an authenticated user with profile given/family name, **When** the header renders, **Then** it shows **Welcome, {userFullName}** and a **Logout** link.
3. **Given** an authenticated user without profile names, **When** the header renders, **Then** welcome text uses the user ID.
4. **Given** a logged-in user, **When** they click **Logout**, **Then** they are directed to the standard AEM logout endpoint.

---

### Edge Cases

- Invalid or missing DAM asset path → logo `<img>` omitted if path blank; no author-facing error on publish view.
- Very long website name → displays as authored text (existing site CSS applies).
- Anonymous visitor → Login link only.
- Authenticated user missing profile properties → welcome uses user ID.
- UserManager unavailable → welcome falls back to user ID; still logged in.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Header component MUST provide an author dialog with **Logo Image Path** (DAM pathfield, root `/content/dam`) and **Website Name** (text field).
- **FR-002**: Header MUST expose `logoPath` and `websiteName` to the view layer for rendering.
- **FR-003**: When `logoPath` is configured, the UI MUST render a logo image using that path.
- **FR-004**: When `websiteName` is configured, the UI MUST render visible website name text.
- **FR-005**: Logo and name elements MUST render only when their respective values are provided—no placeholder markup for empty fields.
- **FR-006**: Header branding block MUST not render when both logo and name are empty.
- **FR-007**: Header MUST expose whether the current user is logged in (`isLoggedIn` false when user is `anonymous`).
- **FR-008**: When logged in, Header MUST expose `userFullName` from the user's profile given and family names, and `userId`.
- **FR-009**: When logged in, Header MUST display **Welcome, {userFullName}** and a **Logout** link to `/system/sling/logout.html`.
- **FR-010**: When not logged in, Header MUST display a **Login** link to the AEM login page.
- **FR-011**: Profile block MUST use conditional rendering—authenticated vs anonymous UI MUST NOT both appear.

### Key Entities

- **Header Branding**: Authorable `logoPath` and `websiteName`.
- **User Profile State**: Transient `isLoggedIn`, `userFullName`, `userId` derived from current session and user profile.

## Success Criteria *(mandatory)*

- **SC-001**: Authors can configure logo and name in under 2 minutes using the Header dialog.
- **SC-002**: 100% of configured fields appear on published pages in acceptance testing.
- **SC-003**: Empty branding configuration produces no visible logo/name markup.
- **SC-004**: Anonymous users see Login; authenticated users see Welcome + Logout in 100% of acceptance tests.

## Assumptions

- Header component resource type: `ai-practical-assessment/components/header`.
- Logo path is a DAM asset path suitable for use as an image source URL.
- Component placed in site header experience fragment or pages by authors.

## Dependencies

- AEM authoring dialog (Granite UI pathfield/textfield).
- Existing `ui.apps` / `core` module layout.
