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
import com.ttn.ai.core.services.TicketNotFoundException;
import com.ttn.ai.core.services.TicketService;

/**
 * Gets a single ticket (GET .../{ticket-id}.json).
 */
@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = TicketConstants.RT_TICKET,
        methods = HttpConstants.METHOD_GET,
        extensions = "json")
@ServiceDescription("Support Ticket - Get")
public class GetTicketServlet extends SlingSafeMethodsServlet {

    @Reference
    private transient TicketService ticketService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        if (!ServletAuth.isAuthenticated(request)) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED",
                    "Authentication required");
            return;
        }
        String ticketId = request.getResource().getValueMap().get(TicketConstants.PN_TICKET_ID, String.class);
        try {
            TicketApiJson.writeSuccess(response, SlingHttpServletResponse.SC_OK,
                    TicketJsonMapper.toJson(ticketService.getTicket(ticketId), true));
        } catch (TicketNotFoundException e) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", e.getMessage());
        } catch (JSONException e) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                    "Response serialization failed");
        }
    }
}
