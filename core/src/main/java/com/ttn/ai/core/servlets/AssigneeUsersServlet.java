package com.ttn.ai.core.servlets;

import java.io.IOException;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;

import com.ttn.ai.core.constants.TicketConstants;
import com.ttn.ai.core.services.AssignableUserService;
import com.ttn.ai.core.services.dto.AssignableUserDto;

/**
 * Returns bulk list of assignable AEM users under /home/users (GET .../tickets.assignees.json).
 */
@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = {
                TicketConstants.RT_TICKETS_ROOT,
                "cq:Page",
                "ai-practical-assessment/components/page"
        },
        methods = HttpConstants.METHOD_GET,
        selectors = "assignees",
        extensions = "json")
@ServiceDescription("Support Ticket - Assignee Users")
public class AssigneeUsersServlet extends SlingSafeMethodsServlet {

    @Reference
    private transient AssignableUserService assignableUserService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        if (!ServletAuth.isAuthenticated(request)) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED",
                    "Authentication required");
            return;
        }
        try {
            ResourceResolver resolver = request.getResourceResolver();
            JSONArray users = new JSONArray();
            for (AssignableUserDto user : assignableUserService.listUsers(resolver)) {
                JSONObject json = new JSONObject();
                json.put("id", user.getId());
                json.put("displayName", user.getDisplayName());
                users.put(json);
            }
            JSONObject data = new JSONObject();
            data.put("users", users);
            TicketApiJson.writeSuccess(response, SlingHttpServletResponse.SC_OK, data);
        } catch (JSONException e) {
            TicketApiJson.writeError(response, SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                    "Response serialization failed");
        }
    }
}
