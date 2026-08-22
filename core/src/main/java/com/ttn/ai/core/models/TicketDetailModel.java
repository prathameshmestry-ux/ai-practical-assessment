package com.ttn.ai.core.models;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.ttn.ai.core.config.AssigneeConfig;
import com.ttn.ai.core.services.TicketNotFoundException;
import com.ttn.ai.core.services.TicketService;
import com.ttn.ai.core.services.TicketStatusTransitionValidator;
import com.ttn.ai.core.services.dto.CommentDto;
import com.ttn.ai.core.services.dto.TicketDto;

/**
 * Sling model for ticket detail, comments, and status actions.
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class TicketDetailModel {

    @Self
    private SlingHttpServletRequest request;

    @OSGiService
    private TicketService ticketService;

    @OSGiService
    private TicketStatusTransitionValidator statusValidator;

    @OSGiService
    private AssigneeConfig assigneeConfig;

    private TicketDto ticket;
    private boolean notFound;
    private String errorMessage;

    @PostConstruct
    void init() {
        String ticketId = request.getParameter("ticketId");
        if (ticketId == null || ticketId.isBlank()) {
            notFound = true;
            return;
        }
        try {
            ticket = ticketService.getTicket(ticketId);
        } catch (TicketNotFoundException e) {
            notFound = true;
        }
    }

    public boolean isNotFound() {
        return notFound;
    }

    public TicketDto getTicket() {
        return ticket;
    }

    public List<CommentDto> getComments() {
        if (ticket == null || ticket.getComments() == null) {
            return Collections.emptyList();
        }
        return ticket.getComments();
    }

    public List<String> getAssignees() {
        return assigneeConfig != null ? assigneeConfig.getAssignees() : Collections.emptyList();
    }

    public Set<String> getAllowedStatusTargets() {
        if (ticket == null || statusValidator == null) {
            return Collections.emptySet();
        }
        return statusValidator.getAllowedTargets(ticket.getStatus());
    }

    public boolean isTerminal() {
        return ticket != null && statusValidator != null && statusValidator.isTerminal(ticket.getStatus());
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getTicketApiBasePath() {
        return ticket != null ? ticket.getPath() : "";
    }
}
