# Implementation Plan: IST Timestamp Display

**Branch**: `003-ist-date-format` | **Date**: 2026-08-27 | **Spec**: [spec.md](./spec.md)

## Summary

Centralize date formatting in `TicketDateFormatter` (`dd/MM/yy HH:mm`, `Asia/Kolkata`). `TicketServiceImpl` maps all ticket/comment dates through it—covers Sling Models and servlet JSON. HTL/ClientLib consume formatted strings.

## Technical Context

**Language**: Java 21, HTL, ClientLib JS

**Primary change**: `core/src/main/java/com/ttn/ai/core/util/TicketDateFormatter.java`

**Consumers**: `TicketServiceImpl`, `TicketJsonMapper` (via DTOs), `TicketListModel`, `TicketDetailModel`, `CommentModel`, dashboard/detail/comments HTL, dashboard/detail ClientLibs

**Testing**: `TicketDateFormatterTest`; existing service tests

## Constitution Check

| Principle | Status |
|-----------|--------|
| I. Cloud Service | ✅ Pass — no query/API change |
| II. Module discipline | ✅ Pass — formatter in `core` |
| III. Spec-driven | ✅ Pass |
| IV. Tests | ✅ Pass — unit test for formatter |
| V. Security | ✅ Pass — no PII logging change |

## Architecture

| Layer | Change |
|-------|--------|
| `TicketDateFormatter` | `DateTimeFormatter.ofPattern("dd/MM/yy HH:mm").withZone(Asia/Kolkata)` |
| `TicketServiceImpl.formatDate()` | Delegate to formatter; comment sort by `Calendar` not string |
| HTL | `${ticket.lastModified}` etc. — strings already formatted |
| ClientLib | Pass-through `lastModified` / `created` from JSON |

## Project Structure

```text
core/src/main/java/com/ttn/ai/core/util/TicketDateFormatter.java
core/src/test/java/com/ttn/ai/core/util/TicketDateFormatterTest.java
core/.../services/impl/TicketServiceImpl.java  (formatDate)
```

## Complexity Tracking

No waivers.
