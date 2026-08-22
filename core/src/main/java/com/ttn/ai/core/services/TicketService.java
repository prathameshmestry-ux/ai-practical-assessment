package com.ttn.ai.core.services;

import com.ttn.ai.core.services.dto.CommentDto;
import com.ttn.ai.core.services.dto.TicketDto;
import com.ttn.ai.core.services.dto.TicketListResult;

/**
 * Support ticket persistence and business operations.
 */
public interface TicketService {

    TicketDto createTicket(String title, String description, String priority, String requester)
            throws TicketValidationException;

    TicketDto getTicket(String ticketId) throws TicketNotFoundException;

    TicketListResult listTickets(int offset, int limit);

    TicketDto updateTicket(String ticketId, String title, String description, String priority, String assignee)
            throws TicketNotFoundException, TicketValidationException;

    CommentDto addComment(String ticketId, String text, String author)
            throws TicketNotFoundException, TicketValidationException;

    TicketDto updateStatus(String ticketId, String newStatus)
            throws TicketNotFoundException, InvalidStatusTransitionException;
}
