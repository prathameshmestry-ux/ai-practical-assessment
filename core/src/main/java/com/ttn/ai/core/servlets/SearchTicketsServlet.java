package com.ttn.ai.core.servlets;

import java.io.IOException;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;

import com.ttn.ai.core.services.TicketService;
import com.ttn.ai.core.services.TicketValidationException;
import com.ttn.ai.core.services.dto.TicketListResult;

/**
 * Searches support tickets by keyword (GET /bin/ai-practical-assessment/search.json).
 */
@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.methods=" + HttpConstants.METHOD_GET,
                "sling.servlet.extensions=json"
        })
@SlingServletPaths("/bin/ai-practical-assessment/search")
@ServiceDescription("Support Ticket - Search")
public class SearchTicketsServlet extends SlingSafeMethodsServlet {

    @Reference
    private transient TicketService ticketService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        if (!ServletAuth.requireAuthenticated(request, response)) {
            return;
        }
        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");
        int offset = parseInt(request.getParameter("offset"), 0);
        int limit = parseInt(request.getParameter("limit"), 25);
        try {
            TicketListResult result = ticketService.searchTickets(keyword, status, offset, limit);
            TicketApiJson.writeSuccess(response, SlingHttpServletResponse.SC_OK, TicketJsonMapper.toJson(result));
        } catch (TicketValidationException e) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR",
                    e.getMessage());
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
