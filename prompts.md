# Prompts
### Prompts (All prompts are /speckit-specify prompts)
1. I am building a Support Ticket Management System. A user can create a ticket via the UI. The user can view all tickets from the JCR on the UI Dashboard. From that dashboard UI, user can open a ticket in detail view. user can update ticket fields and reassign. user can also add comments. User can change status from Open to In Progress, In Progress to Resolved, Resolved to Closed, Open to Cancelled, and In Progress to Cancelled. The project is based on aem, so pages should be divided into components.
2. There are 4 changes required so far, update the required md files
    - /content/ai-practical-assessment is a nt:unstructure node it should be cq:page
    - remove us and en pages under the /content/ai-practical-assessment. I need /content/ai-practical-assessment/support and so on. 
    - components like ticket create, comments, Dashboard, details are not showing up in Drag components here section.
    - UI is not up to the mark, use material UI
3. Fixing the pages,
    - I want only three pages in this hierarchy. I want is /content/ai-practical-assessment/support-tickets/dashboard, .../support-tickets/ticket, support-tickets/create-ticket. remove all other pages.
    - each page should have the corresponding component like dashboard will have ticket-dashboard component, create page will have create and detail page will have detail component.
4. add rep:policy in ui-content for ticker-service user to give permission on /content/ai-practical-assessment hierarchy 
5. getting error because rep:restriction is defined as a property. In Jackrabbit Oak (AEM's repository), rep:restrictions is not allowed to be a property of rep:GrantACE. Instead, it must be declared as a child node of allow node of type rep:Restrictions. 
6. in ticket-dashboard component the tickets are not getting stacked, instead they are queues in same row. 
7. update the spec 001-support-tickets. In ticket detail component I do not want "change status" as a separate field, rather I should get dropdown in the same status field. reponse short and simple 
8. Update the UI requirements for the Ticket Detail component's status dropdown. Currently, it renders a read-only status badge and a separate 'Change status' dropdown below it. I want to replace this with a unified, Jira-style status button. The current status should act as the interactive dropdown button itself (styled as a colored block/badge with a down chevron). Clicking this status badge should open the dropdown menu containing the other allowed status transitions. Please update the HTL structure and CSS to reflect this unified design, and regenerate the frontend code for this component. 
9. Update spec to mandate that changing a ticket's status must NOT trigger a full page refresh. The Ticket Detail component needs a dedicated AEM ClientLib (JavaScript). This script should intercept the status dropdown click, send an asynchronous request (using the Fetch API) to the AEM backend to update the status, and upon success, dynamically update the status button's DOM element to reflect the new state. Please regenerate the frontend HTL and the accompanying ClientLib JavaScript to implement this AJAX-style behavior. 
10. Update the functional requirements to include an 'Assignee' field on the Ticket Detail page. Update with the following technical implementation,
    - **UI/UX**: The Assignee field must use inline editing. It should display as a read-only name by default. On click, it must transform into a text input. 
    - **Backend**: The GET Servlet should return a bulk JSON array of assignable AEM users without requiring a search parameter.
    - **Frontend (ClientLib)**: When the user clicks the Assignee field for the first time, JavaScript must fetch the user list from the Servlet and cache it in the browser as sessionStorage.
    - **Filtering**: As the user types in the input field, the JavaScript must filter the cached array locally rather than making new HTTP requests, and instantly update the dropdown UI with the matching results. Please update the architecture documents and regenerate the ClientLib JS and Servlet code. 
11. Update the data persistence architecture in plan.md and tasks.md.
    - **Tickets**: All newly created tickets must be saved under the base JCR path /var/ai-practical-assessment/tickets. Update the Ticket Creation POST Servlet and related ClientLib fetch calls to use this new path.
    - **Comments**: All comments must be saved as child nodes under their respective ticket (e.g., /var/ai-practical-assessment/tickets/{ticket-id}/comments). Update the Comment GET/POST Servlets to read from and write to this specific child path.
    - **Frontend**: Update the ClientLib JavaScript so that when a user submits a comment, the fetch request targets the new ticket path (e.g., using the comments selector on the new ticket URL).
    Regenerate the Java Servlets and JavaScript ClientLibs to apply these path updates.
12. Create new spec to include a 'Keyword Search' feature on the Ticket Dashboard. Update plan.md and tasks.md with the following technical implementation:
    - **Backend (Servlet)**: Create a new GET Servlet (e.g., bound to /var/ai-practical-assessment/tickets with a search selector). It MUST use the AEM QueryBuilder API. The query parameters should restrict the path to /var/ai-practical-assessment/tickets, use fulltext for the keyword matching, and return the matching tickets as a JSON array.
    - **Frontend (HTL)**: Add a search text input field to the Dashboard component above the ticket list.
    - **Frontend (ClientLib)**: Write JavaScript to listen for the 'Enter' key or a button click on the search field. It should make an asynchronous fetch request to the new search Servlet, passing the keyword, and dynamically re-render the dashboard's ticket list with the JSON results without reloading the page.
Please update the architecture documents and regenerate the Dashboard HTL, ClientLib JS, and the Java Search Servlet.
13. I want to search for ticket that starts with the keyword it could be half word like "permis" or ticket id like "ticket-8fde", it should return all ticket starting with that text or ticket id starting with that.
14. Update the functional requirements to include a 'Filter by Status' dropdown on the Ticket Dashboard. Update plan.md and tasks.md with the following technical implementation:
    - **Frontend (HTL)**: Add a status dropdown `<select>` element (Material UI) next to the keyword search input on the Dashboard. It should include options for all ticket states (Open, In Progress, Resolved, Closed, Cancelled) and an 'All' option.
    - **Backend (Servlet Update)**: Modify the existing QueryBuilder search Servlet to accept an optional status parameter. If a status is provided, add a property predicate (e.g., property=status, property.value=<selected-status>) to the query map alongside the current predicates.
    - **Frontend (ClientLib)**: Update the dashboard JavaScript. When the status dropdown value changes, or the search button is clicked, the fetch request must send both the keyword string and the selected status to the Servlet. Update the DOM dynamically with the filtered results.
    Please update the architecture documents and regenerate the Dashboard HTL, ClientLib JS, and the Java Search Servlet
15. Update the global UI date formatting requirements in spec.md, plan.md, and tasks.md to strictly enforce a standard timestamp display format of 'DD/MM/YY HH:MM' in the 'Asia/Kolkata' (IST) timezone for all dates across the application (ticket creation, last modification, and comment creation).
    - **Backend (Java)**: All Java code preparing data for the frontend (Sling Models and GET Servlets) MUST format JCR Calendar or Date properties as strings using the pattern dd/MM/yy HH:mm. The formatter (SimpleDateFormat or DateTimeFormatter) MUST explicitly have its TimeZone set to 'Asia/Kolkata' before conversion.
    - **Frontend (HTL)**: For any dates formatted directly within Sightly/HTL files, update the expressions to include both the format and timezone parameters: @ format='dd/MM/yy HH:mm', timezone='Asia/Kolkata'.
    Please regenerate the necessary Sling Models, Servlets, HTL, and ClientLib JS to ensure all timestamps display uniformly under this new rule.
