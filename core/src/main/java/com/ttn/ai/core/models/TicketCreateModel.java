package com.ttn.ai.core.models;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.ttn.ai.core.constants.TicketConstants;
import com.ttn.ai.core.servlets.ServletAuth;

/**
 * Sling model for the create ticket component.
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class TicketCreateModel {

    @Self
    private SlingHttpServletRequest request;

    private boolean loggedIn;

    @PostConstruct
    void init() {
        loggedIn = ServletAuth.isAuthenticated(request);
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getTicketsApiPath() {
        return TicketConstants.TICKET_ROOT_PATH;
    }
}
