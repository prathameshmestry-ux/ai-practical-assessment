package com.ttn.ai.core.models;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.ttn.ai.core.services.TicketNotFoundException;
import com.ttn.ai.core.services.TicketService;
import com.ttn.ai.core.services.dto.CommentDto;
import com.ttn.ai.core.services.dto.TicketDto;

/**
 * Sling model for the ticket comments component.
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class CommentModel {

    @Self
    private SlingHttpServletRequest request;

    @OSGiService
    private TicketService ticketService;

    private List<CommentDto> comments = Collections.emptyList();
    private String ticketApiCommentsPath;
    private boolean notFound;

    @PostConstruct
    void init() {
        String ticketId = request.getParameter("ticketId");
        if (ticketId == null || ticketId.isBlank()) {
            notFound = true;
            return;
        }
        try {
            TicketDto ticket = ticketService.getTicket(ticketId);
            comments = ticket.getComments() != null ? ticket.getComments() : Collections.emptyList();
            ticketApiCommentsPath = ticket.getPath() + "/comments";
        } catch (TicketNotFoundException e) {
            notFound = true;
        }
    }

    public List<CommentDto> getComments() {
        return comments;
    }

    public String getTicketApiCommentsPath() {
        return ticketApiCommentsPath;
    }

    public boolean isNotFound() {
        return notFound;
    }
}
