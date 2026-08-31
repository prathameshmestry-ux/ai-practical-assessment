# PR Description
**PR#1**: Phase 1 Features

## Summary  
Deliver the first phase of an AEM Cloud Service support ticket management system with JCR-backed ticket workflows, modular authoring components.
## Features Implemented  
- Add an authenticated AEM support ticket system for creating, listing, viewing, updating, assigning, commenting on, and progressing tickets through a defined status workflow.
- Provide reusable AEM components, pages, client-side interactions, JSON endpoints, JCR persistence, and service-user configuration for support ticket management.
## Technical Changes
- Created components,pages,templates for dashboard, ticket creation, comments, details. And servlets to create, update ticket and comments.
- Fixed Create ticket issue, fixed pay hierarchy, added components to respective page.
- Added Rep:Policy. Fixed Ticket dashboard table.
- Status incorrect status dropdown fixed and Removed full page refresh on status change
## Database Changes 
None 
## Testing Done 
- Add unit coverage for ticket creation and status transition rules.
- Add an integration smoke test for support ticket API creation and listing.
## AI Usage Summary
Mostly SDD was used to generate the details and architecture of the features, post which the actual implementation
## Screenshots / Demo Notes  

## Known Limitations 
- Header Misaligned
## Future Improvements
- UI Fixes

====================================================
# PR Description
**PR#2**: Added Assignee field with AEM Users
## Summary  
Enable ticket assignment from eligible AEM devs group members through an inline detail-page experience.
## Features Implemented  
- Add inline ticket assignee editing with locally filtered suggestions and session-scoped user caching.
- Expose an authenticated endpoint listing direct, non-system users from the AEM devs group.
## Technical Changes
Assignees are now sourced from direct, non-system members of the AEM devs group through a new authenticated bulk API, with an inline ticket-detail editor that session-caches and locally filters users before asynchronously persisting selections.
- Replaced the static assignee configuration with AEM group-backed user resolution.
- Exposed an authenticated bulk endpoint for assignable users.
- Moved assignee editing to an inline ticket-detail experience.
- Changed ticket updates and specifications to reflect the new assignee source and workflow.
## Database Changes 
None 
## Testing Done  
Performed Manual Testing
## AI Usage Summary
AI was used to write logic to get particular Groups and Users, and populate it Assignee searchable dropdown. 
## Screenshots / Demo Notes  
## Known Limitations  
-  loadAssignees parses the response and chains the promise without checking HTTP status or catching JSON/network failures; a failed assignee request leaves the editor open with no suggestions and produces an unhandled promise rejection instead of showing an update error.
- updateTicket persists any nonblank assignee without checking that it is a direct, non-system member of the devs group, so clients can assign arbitrary user IDs or non-user values despite FR-007c.
## Future Improvements
- Server-side assignee check — Today API accepts any assignee ID;

====================================================
# PR Description
**PR#4**: Implemented Login, Integrated search and Filter, and Changed ticket creation location
## Summary  
Relocate ticket data storage to /var, add dashboard search and status filtering, and standardize timestamp presentation.
## Features Implemented  
- Implemented component level Login
- Add authenticated dashboard search with title or ticket-ID prefix matching and optional status filtering.
- Add asynchronous dashboard filtering with reset-to-full-list behavior and status selection.
## Technical Changes
- Updated the support-ticket specification and implementation plan to define the split between /var ticket data and /content UI pages.
- Migrated ticket and comment persistence from the content tree to a dedicated /var data tree.
- Repointed the ticket API and UI integration to the new storage resource path while keeping support pages under /content.
- Provisioned and secured the new mutable ticket data hierarchy through package content and deployment configuration.
## Database Changes 
None 
## Testing Done
Extend unit and integration coverage for ticket search, status filtering, path migration, and IST date formatting.  
## AI Usage Summary
AI was used to implement the component level login, along with Search and Filter.
## Screenshots / Demo Notes  
## Known Limitations  
## Future Improvements
Dedicated Login Page
