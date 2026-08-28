# Implementation Plan: Header Branding and User Profile

**Branch**: `004-header-branding` | **Date**: 2026-08-27 | **Spec**: [spec.md](./spec.md)

## Summary

Header component: authorable logo/name + dynamic **User Profile & Authentication**. `ServletAuth` resolves session (`anonymous` check), `UserManager` reads `profile/givenName` + `profile/familyName`. HTL shows Welcome/Logout or Login.

## Architecture

| Artifact | Role |
|----------|------|
| `ServletAuth` | `isAuthenticated`, `getUserId`, `resolveUserFullName`; login/logout paths |
| `HeaderModel` | Branding props + auth state getters |
| `header.html` | Branding block + `data-sly-test="${model.loggedIn}"` profile block |

## Auth flow

```text
ResourceResolver.getUserID()
  → anonymous? → Login link
  → else UserManager.getAuthorizable(userId)
       → profile givenName + familyName → userFullName
       → Welcome + Logout
```

## HTL

| State | UI |
|-------|-----|
| `loggedIn` | Welcome, {userFullName} + Logout → `/system/sling/logout.html` |
| `!loggedIn` | Login → `/libs/granite/core/content/login.html` |

## Constitution Check

Pass — no new dependencies; auth reuses ticket servlet pattern.
