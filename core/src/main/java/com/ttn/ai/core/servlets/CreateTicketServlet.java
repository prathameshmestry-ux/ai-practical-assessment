package com.ttn.ai.core.servlets;

import java.io.IOException;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;

import com.ttn.ai.core.constants.TicketConstants;
import com.ttn.ai.core.services.TicketService;
import com.ttn.ai.core.services.TicketValidationException;
import com.ttn.ai.core.services.dto.TicketDto;

/**
 * Creates support tickets (POST .../tickets.ticket.json under ticket data root).
 */
@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = {
                TicketConstants.RT_TICKETS_ROOT,
                "cq:Page",
                "ai-practical-assessment/components/page"
        },
        methods = HttpConstants.METHOD_POST,
        selectors = "ticket",
        extensions = "json")
@ServiceDescription("Support Ticket - Create")
public class CreateTicketServlet extends SlingAllMethodsServlet {

    @Reference
    private transient TicketService ticketService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws IOException {
        if (!ServletAuth.isAuthenticated(request)) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED",
                    "Authentication required");
            return;
        }
        try {
            JSONObject body = ServletAuth.readJsonBody(request);
            String title = body.optString("title", null);
            String description = body.optString("description", null);
            String priority = body.optString("priority", null);
            String requester = request.getResourceResolver().getUserID();
            TicketDto ticket = ticketService.createTicket(title, description, priority, requester);
            TicketApiJson.writeSuccess(response, SlingHttpServletResponse.SC_CREATED,
                    TicketJsonMapper.toJson(ticket, false));
        } catch (TicketValidationException e) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR",
                    e.getMessage());
        } catch (JSONException e) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR",
                    "Invalid JSON body");
        }
    }
}
