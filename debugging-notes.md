# Debugging Notes


## Issue 1
### Problem  
Ticket create/update returned 403 or save failed. Dashboard empty even though user was logged in. Service user could not write JCR nodes.

### How I Investigated
I checked the AEM error logs, and found out that I was getting Access denied / javax.jcr.AccessDeniedException. I checked the permission of the user and 
OSGi service user mapping.

### How AI Helped  
- Pointed to AEM best practice: service user needs explicit rep:GrantACE on data root
- Generated _rep_policy.xml granting ticket-service jcr:read, jcr:write, jcr:addChildNodes, etc.
- Confirmed ui.config mapping + repoinit for /var path after migration

### What I Validated  
- Create and Update API started giving 201 status.
- Ticket node created under correct root.
- Dashboard list returns tickets after create.

### Final Fix
Commit `e538843`: ACL to /var/ai-practical-assessment/_rep_policy.xml.

---

## Issue 2
### Problem
Status dropdown on detail page showed wrong options (invalid transitions visible) and status change triggered full page reload instead of in-place update.  
### How I Investigated
On ticket detail page, i checked the all statuses are showing in one single dropdown option on UI. So, I check the HTL for that component, which was causing the issue.
For page reload on status change, i noticed that form submit action was getting triggered. 
### How AI Helped  
- Jira-style badge dropdown — only allowed next statuses, one row per status
- Rewrote ticket-detail.js: async fetch to .status.json
- Added Material styles in ticket-material.css for badge + menu elevation
### What I Validated  
From open: only in-progress and cancelled in menu
From closed/cancelled: no dropdown, read-only badge
Status change without page reload; dashboard matches on next visit
### Final Fix
Commit e61e5ea: rebuilt status UI in HTL + ticket-detail.js + ticket-material.css; wired to UpdateTicketStatusServlet.