package com.ttn.ai.core.models;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.ttn.ai.core.constants.TicketConstants;
import com.ttn.ai.core.servlets.ServletAuth;
import com.ttn.ai.core.services.TicketService;
import com.ttn.ai.core.services.dto.TicketDto;
import com.ttn.ai.core.services.dto.TicketListResult;

/**
 * Sling model for the ticket dashboard list.
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class TicketListModel {

    @Self
    private SlingHttpServletRequest request;

    @OSGiService
    private TicketService ticketService;

    private TicketListResult listResult;
    private boolean empty;
    private boolean loggedIn;

    @PostConstruct
    void init() {
        loggedIn = ServletAuth.isAuthenticated(request);
        if (!loggedIn) {
            empty = true;
            return;
        }
        int offset = parseInt(request.getParameter("offset"), 0);
        int limit = parseInt(request.getParameter("limit"), 25);
        listResult = ticketService.listTickets(offset, limit);
        empty = listResult.getTickets().isEmpty();
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public List<TicketDto> getTickets() {
        return listResult != null ? listResult.getTickets() : Collections.emptyList();
    }

    public boolean isEmpty() {
        return empty;
    }

    public long getTotal() {
        return listResult != null ? listResult.getTotal() : 0;
    }

    public String getTicketsApiPath() {
        return TicketConstants.TICKET_ROOT_PATH;
    }

    public String getSearchApiPath() {
        return TicketConstants.TICKET_SEARCH_SERVLET_PATH;
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
