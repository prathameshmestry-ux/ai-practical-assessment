package com.ttn.ai.core.servlets;

import java.io.IOException;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
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
import com.ttn.ai.core.services.dto.CommentDto;

/**
 * Adds a comment (POST .../{ticket-id}/comments.comment.json).
 */
@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = "nt:unstructured",
        methods = HttpConstants.METHOD_POST,
        selectors = "comment",
        extensions = "json")
@ServiceDescription("Support Ticket - Add Comment")
public class AddCommentServlet extends SlingAllMethodsServlet {

    @Reference
    private transient TicketService ticketService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        if (!ServletAuth.isAuthenticated(request)) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED",
                    "Authentication required");
            return;
        }
        String ticketId = resolveTicketId(request.getResource());
        if (ticketId == null) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Ticket not found");
            return;
        }
        try {
            JSONObject body = ServletAuth.readJsonBody(request);
            String text = body.optString("text", null);
            String author = request.getResourceResolver().getUserID();
            CommentDto comment = ticketService.addComment(ticketId, text, author);
            TicketApiJson.writeSuccess(response, SlingHttpServletResponse.SC_CREATED,
                    TicketJsonMapper.toJson(comment));
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

    private String resolveTicketId(Resource resource) {
        if (resource == null) {
            return null;
        }
        if (TicketConstants.COMMENTS_NODE_NAME.equals(resource.getName())) {
            Resource parent = resource.getParent();
            if (parent != null) {
                return parent.getValueMap().get(TicketConstants.PN_TICKET_ID, String.class);
            }
            return null;
        }
        return resource.getValueMap().get(TicketConstants.PN_TICKET_ID, String.class);
    }
}
