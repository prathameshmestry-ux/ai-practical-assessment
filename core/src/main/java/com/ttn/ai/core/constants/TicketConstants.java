package com.ttn.ai.core.constants;

/**
 * Constants for the Support Ticket Management System.
 */
public final class TicketConstants {

    public static final String TICKET_ROOT_PATH = "/content/ai-practical-assessment/support-tickets";
    public static final String HOME_USERS_PATH = "/home/users";
    public static final String HOME_USERS_SYSTEM_PATH = "/home/users/system";
    public static final String ASSIGNEE_GROUP_ID = "devs";
    public static final String COMMENTS_NODE_NAME = "comments";
    public static final String SERVICE_USER_SUBSERVICE = "ticket-service";

    public static final String PN_TICKET_ID = "ticketId";
    public static final String PN_TITLE = "title";
    public static final String PN_DESCRIPTION = "description";
    public static final String PN_PRIORITY = "priority";
    public static final String PN_STATUS = "status";
    public static final String PN_REQUESTER = "requester";
    public static final String PN_ASSIGNEE = "assignee";
    public static final String PN_CREATED = "created";
    public static final String PN_LAST_MODIFIED = "lastModified";

    public static final String PN_COMMENT_ID = "commentId";
    public static final String PN_TEXT = "text";
    public static final String PN_AUTHOR = "author";

    public static final String STATUS_OPEN = "open";
    public static final String STATUS_IN_PROGRESS = "in-progress";
    public static final String STATUS_RESOLVED = "resolved";
    public static final String STATUS_CLOSED = "closed";
    public static final String STATUS_CANCELLED = "cancelled";

    public static final String PRIORITY_LOW = "low";
    public static final String PRIORITY_MEDIUM = "medium";
    public static final String PRIORITY_HIGH = "high";

    public static final String RT_TICKET = "ai-practical-assessment/components/support-ticket";
    public static final String RT_TICKETS_ROOT = "ai-practical-assessment/components/tickets-root";

    private TicketConstants() {
    }
}
