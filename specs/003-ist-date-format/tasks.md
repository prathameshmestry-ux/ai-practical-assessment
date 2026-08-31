# Tasks: IST Timestamp Display

**Input**: `/specs/003-ist-date-format/`

## Phase 1: Core Formatter

- [x] T001 Create `TicketDateFormatter` in `core/src/main/java/com/ttn/ai/core/util/TicketDateFormatter.java`
- [x] T002 [P] Add `TicketDateFormatterTest` in `core/src/test/java/com/ttn/ai/core/util/TicketDateFormatterTest.java`

## Phase 2: Service Integration (US1–US3)

- [x] T003 [US1] Wire `TicketServiceImpl.formatDate()` to `TicketDateFormatter` in `core/src/main/java/com/ttn/ai/core/services/impl/TicketServiceImpl.java`
- [x] T004 [US2] Sort comments by `Calendar` before formatting in `TicketServiceImpl.toTicketDto()`

## Phase 3: Validation

- [x] T005 Run `mvn clean test -pl core`
- [ ] T006 Manual verify dashboard, detail, comments timestamps on AEM *(manual)*

## Notes

- HTL/ClientLib unchanged—backend returns IST strings (FR-006/FR-007).
- Search dashboard async rows inherit format via same DTO path.
