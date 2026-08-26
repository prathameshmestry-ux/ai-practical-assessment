package com.ttn.ai.core.servlets;

import java.io.IOException;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;

import com.ttn.ai.core.constants.TicketConstants;
import com.ttn.ai.core.services.TicketService;
import com.ttn.ai.core.services.dto.TicketListResult;

/**
 * Lists support tickets (GET .../tickets.list.json under ticket data root).
 */
@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = {
                TicketConstants.RT_TICKETS_ROOT,
                "cq:Page",
                "ai-practical-assessment/components/page"
        },
        methods = HttpConstants.METHOD_GET,
        selectors = "list",
        extensions = "json")
@ServiceDescription("Support Ticket - List")
public class TicketListServlet extends SlingSafeMethodsServlet {

    @Reference
    private transient TicketService ticketService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        if (!ServletAuth.isAuthenticated(request)) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED",
                    "Authentication required");
            return;
        }
        int offset = parseInt(request.getParameter("offset"), 0);
        int limit = parseInt(request.getParameter("limit"), 25);
        try {
            TicketListResult result = ticketService.listTickets(offset, limit);
            TicketApiJson.writeSuccess(response, SlingHttpServletResponse.SC_OK, TicketJsonMapper.toJson(result));
        } catch (JSONException e) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                    "Response serialization failed");
        }
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
