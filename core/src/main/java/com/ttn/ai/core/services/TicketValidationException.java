package com.ttn.ai.core.services;

/**
 * Thrown when ticket input validation fails.
 */
public class TicketValidationException extends Exception {

    public TicketValidationException(String message) {
        super(message);
    }
}
