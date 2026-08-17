package com.ttn.ai.core.services;

/**
 * Thrown when a ticket cannot be found.
 */
public class TicketNotFoundException extends Exception {

    public TicketNotFoundException(String ticketId) {
        super("Ticket not found: " + ticketId);
    }
}
