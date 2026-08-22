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
import com.ttn.ai.core.services.TicketNotFoundException;
import com.ttn.ai.core.services.TicketService;
import com.ttn.ai.core.services.TicketValidationException;
import com.ttn.ai.core.services.dto.TicketDto;

/**
 * Updates ticket fields (POST .../{ticket-id}.update.json).
 */
@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = TicketConstants.RT_TICKET,
        methods = HttpConstants.METHOD_POST,
        selectors = "update",
        extensions = "json")
@ServiceDescription("Support Ticket - Update")
public class UpdateTicketServlet extends SlingAllMethodsServlet {

    @Reference
    private transient TicketService ticketService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        if (!ServletAuth.isAuthenticated(request)) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED",
                    "Authentication required");
            return;
        }
        String ticketId = request.getResource().getValueMap().get(TicketConstants.PN_TICKET_ID, String.class);
        try {
            JSONObject body = ServletAuth.readJsonBody(request);
            TicketDto ticket = ticketService.updateTicket(
                    ticketId,
                    body.has("title") ? body.getString("title") : null,
                    body.has("description") ? body.getString("description") : null,
                    body.has("priority") ? body.getString("priority") : null,
                    body.has("assignee") ? body.getString("assignee") : null);
            TicketApiJson.writeSuccess(response, SlingHttpServletResponse.SC_OK,
                    TicketJsonMapper.toJson(ticket, true));
        } catch (TicketNotFoundException e) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", e.getMessage());
        } catch (TicketValidationException e) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR",
                    e.getMessage());
        } catch (JSONException e) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR",
                    "Invalid JSON body");
        }
    }
}
