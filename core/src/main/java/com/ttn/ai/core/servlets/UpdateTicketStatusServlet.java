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
import com.ttn.ai.core.services.InvalidStatusTransitionException;
import com.ttn.ai.core.services.TicketNotFoundException;
import com.ttn.ai.core.services.TicketService;
import com.ttn.ai.core.services.dto.TicketDto;

/**
 * Updates ticket status (POST .../{ticket-id}.status.json).
 */
@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = TicketConstants.RT_TICKET,
        methods = HttpConstants.METHOD_POST,
        selectors = "status",
        extensions = "json")
@ServiceDescription("Support Ticket - Update Status")
public class UpdateTicketStatusServlet extends SlingAllMethodsServlet {

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
            String status = body.optString("status", null);
            TicketDto ticket = ticketService.updateStatus(ticketId, status);
            TicketApiJson.writeSuccess(response, SlingHttpServletResponse.SC_OK,
                    TicketJsonMapper.toJson(ticket, true));
        } catch (TicketNotFoundException e) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", e.getMessage());
        } catch (InvalidStatusTransitionException e) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_BAD_REQUEST,
                    "INVALID_STATUS_TRANSITION", e.getMessage());
        } catch (JSONException e) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR",
                    "Invalid JSON body");
        }
    }
}
