# Implementation Plan: Header Login Post-Redirect

**Branch**: `006-header-login-redirect` | **Date**: 2026-08-28 | **Spec**: [spec.md](./spec.md)

## Summary

Header **Login** href becomes `/libs/granite/core/content/login.html?resource={currentPage}.html`. `HeaderModel` resolves current page from `currentPage` script variable or request path (XF-safe). `ServletAuth.buildLoginPath()` encodes query param.

## Flow

```text
Anonymous on /content/.../dashboard.html
  → HeaderModel.getCurrentPagePath() → /content/.../dashboard.html
  → getLoginPath() → /libs/granite/core/content/login.html?resource=%2Fcontent%2F...%2Fdashboard.html
  → User logs in → AEM redirects to resource URL
```

## Changes

| File | Change |
|------|--------|
| `ServletAuth.java` | `buildLoginPath(String resourcePath)` |
| `HeaderModel.java` | `getCurrentPagePath()`, `getLoginPath()` uses builder; request + `currentPage` resolution |
| `header.html` | Login `href="${model.loginPath @ context='uri'}"` (unchanged binding, new value) |
| `HeaderModelTest.java` | Assert login URL includes encoded `resource` |

## Constitution Check

Pass — no new modules; uses platform login redirect.
